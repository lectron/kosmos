/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.local;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.userdata.UserData;
import com.minecraftly.bukkit.world.data.local.worlddata.WorldData;
import com.minecraftly.core.util.Callback;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * A class to manage the loading and savin the data of players and worlds.
 *
 * TODO: I'm not happy with this, it will need a cleanup!
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class PlayerHandler implements Listener, Closeable {

	/**
	 * The core instance.
	 */
	private final MinecraftlyBukkitCore core;

	/**
	 * A map of loaded world data and the relevant UUID of the owner of the world.
	 */
	private final ConcurrentHashMap<UUID, WorldData> loadedWorldData = new ConcurrentHashMap<>();

	/**
	 * A map of loaded user data and the relevant user's UUID.
	 */
	private final ConcurrentHashMap<UUID, UserData> loadedUserData = new ConcurrentHashMap<>();

	/**
	 * A queue used for saving data.
	 */
	private final ConcurrentLinkedQueue<AbstractLocalData> dataToBeSaved = new ConcurrentLinkedQueue<>();

	/**
	 * The callback used when a world has finished loading.
	 */
	private final Callback<Callable<Void>, UUID> worldDataLoadedCallback = param -> {
		final World world = Bukkit.getWorld( param.toString() );
		return () -> {
			if( world == null ) return null;
			WorldDimension.getPlayersAllDimensions( world ).forEach( player -> userJoinedWorld( player, world ) );
			return null;
		};
	};

	/**
	 * The save task, used to save data on an interval async.
	 */
	private final BukkitRunnable saveTask = new BukkitRunnable() {
		@Override
		public void run() {

			int saveInInterval = 15;
			for( int i = 0; i < saveInInterval; i++ ) {
				try {
					AbstractLocalData data = dataToBeSaved.poll();
					if( !data.save( core ) ) {
						throw new Exception( "Unable to save data \"" + data.getClass() + "\"." );
					}
				} catch ( Exception ex ) {
					core.getLogger().log( Level.WARNING, "An error occured whilst saving user/world data!", ex );
				}
			}

		}
	};

	public PlayerHandler( MinecraftlyBukkitCore core ) {

		this.core = core;
		this.saveTask.runTaskTimer( core.getOriginObject(), 20, 20 );

	}

	@EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = true )
	public void onPlayerMove( PlayerMoveEvent event ) {

		// Update the player location.
		UserData data = loadedUserData.get( event.getPlayer().getUniqueId() );
		if( data == null ) return;

		data.setLastLocation( event.getTo() );

		// No working here...

	}

	public void onPlayerTeleport( PlayerTeleportEvent event ) {

		// Update the player location.
		UserData data = loadedUserData.get( event.getPlayer().getUniqueId() );
		if( data == null ) return;

		data.setLastLocation( event.getTo() );

	}

	@EventHandler
	public void onWorldLoad( WorldLoadEvent event ) {

		final UUID uuid = WorldDimension.getUUIDOfWorld( event.getWorld() );
		if( uuid == null ) return;

		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
			loadedWorldData.put( uuid, WorldData.load( core, event.getWorld() ) );
			core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), worldDataLoadedCallback.call( uuid ) );
		} );

	}

	@EventHandler
	public void onWorldUnload( WorldUnloadEvent event ) {

		final UUID uuid = WorldDimension.getUUIDOfWorld( event.getWorld() );
		if( uuid == null || !loadedWorldData.containsKey( uuid ) ) return;

		WorldData data = loadedWorldData.get( uuid );
		loadedWorldData.remove( uuid );

		dataToBeSaved.add( data );

	}

	@EventHandler
	public void onPlayerDim( PlayerChangedWorldEvent event ) {

		// TODO implement a cool down of switching between "universes".

		World to = event.getPlayer().getWorld();
		World from = event.getFrom();

		UUID uuid = WorldDimension.getUUIDOfWorld( to );
		UUID uuid1 = WorldDimension.getUUIDOfWorld( from );
		if( Objects.equals( uuid, uuid1 ) ) return;

		if( loadedWorldData.containsKey( uuid1 ) ) {
			userLeftWorld( event.getPlayer(), from );
		}

		userJoinedWorld( event.getPlayer(), to );

	}

	@EventHandler( priority = EventPriority.LOWEST)
	public void onPlayerQuit( PlayerQuitEvent event ) {
		userLeftWorld( event.getPlayer(), event.getPlayer().getWorld() );
	}

	@Override
	public void close() throws IOException {

		// Cancel the task and run all the saving instantly.
		saveTask.cancel();

		// Save existing data.
		dataToBeSaved.addAll( loadedUserData.values() );
		dataToBeSaved.addAll( loadedWorldData.values() );

		// Maybe we should multi-thread this.. It shouldn't be under any major load when proper shutdown is implemented.
		core.getLogger().log( Level.INFO, "Saving user and world data..." );
		dataToBeSaved.forEach( abstractLocalData -> {
			try {
				abstractLocalData.save( core );
			} catch ( IOException e ) {
				core.getLogger().log( Level.WARNING, "An error occurred whilst saving user/world data!", e );
			}
		} );

	}

	/**
	 * Processes the data and loads it on world join.
	 *
	 * @param user The player to process.
	 * @param world THe world which they joined.
	 */
	public void userJoinedWorld( @NonNull Player user, @NonNull World world ) {

		// TODO ASYNC? Could cause issues..
		UserData data = UserData.load( core, world, user.getUniqueId() );
		loadedUserData.put( user.getUniqueId(), data );

		user.setBedSpawnLocation( data.getBedLocation() );
		user.teleport( data.getLastLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN );

	}

	/**
	 * Processes the data and saves it on world leave.
	 *
	 * @param user The player to process.
	 * @param world The world which they left.
	 */
	public void userLeftWorld( @NonNull Player user, @NonNull World world ) {

		// Okay we keep this because it's likely to get removed after this is call finishes.
		final UserData userData = loadedUserData.get( user.getUniqueId() );
		if( userData != null ) dataToBeSaved.add( userData );
		loadedUserData.remove( user.getUniqueId(), userData );

	}

}
