/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.local;

import com.google.common.base.Joiner;
import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.userdata.UserData;
import com.minecraftly.bukkit.world.data.local.worlddata.PunishEntry;
import com.minecraftly.bukkit.world.data.local.worlddata.WorldData;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.util.Callback;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
			WorldDimension.getPlayersAllDimensions( world ).forEach( player -> userJoinedWorld( player, world, getWorldData( player.getUniqueId() ) ) );
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
					if( data == null ) continue;
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

	}

	public void load() {
		this.saveTask.runTaskTimer( core.getOriginObject(), 20, 20 );
	}

	@EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = true )
	private void onPlayerMove( PlayerMoveEvent event ) {

		// Update the player location.
		UserData data = loadedUserData.get( event.getPlayer().getUniqueId() );
		if( data == null ) return;

		data.setLastLocation( event.getTo() );

		// No working here...

	}

	@EventHandler
	private void onWorldLoad( WorldLoadEvent event ) {

		final UUID uuid = WorldDimension.getUUIDOfWorld( event.getWorld() );
		if( uuid == null ) return;

		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
			loadedWorldData.put( uuid, WorldData.load( core, event.getWorld() ) );
			core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), worldDataLoadedCallback.call( uuid ) );
		} );

	}

	@EventHandler
	private void onWorldUnload( WorldUnloadEvent event ) {

		final UUID uuid = WorldDimension.getUUIDOfWorld( event.getWorld() );
		if( uuid == null || !loadedWorldData.containsKey( uuid ) ) return;

		WorldData data = loadedWorldData.get( uuid );
		loadedWorldData.remove( uuid );

		dataToBeSaved.add( data );

	}

	@EventHandler( priority = EventPriority.MONITOR )
	private void onPlayerDim( PlayerChangedWorldEvent event ) {

		// TODO implement a cool down of switching between "universes".

		World to = event.getPlayer().getWorld();
		World from = event.getFrom();

		UUID uuid = WorldDimension.getUUIDOfWorld( to );
		UUID uuid1 = WorldDimension.getUUIDOfWorld( from );
		if( Objects.equals( uuid, uuid1 ) ) return;

		if( loadedWorldData.containsKey( uuid1 ) ) {
			userLeftWorld( event.getPlayer(), from );
		}

		userJoinedWorld( event.getPlayer(), to, getWorldData( uuid ) );

	}

	/**
	 * Called from {@see PlayerListener}
	 *
	 * @param event
	 */
	public void onPlayerJoin( PlayerJoinEvent event ) {
		WorldData worldData = getWorldData( WorldDimension.getUUIDOfWorld( event.getPlayer().getWorld() ) );
		userJoinedWorld( event.getPlayer(), event.getPlayer().getWorld(), worldData );
	}

	@EventHandler( priority = EventPriority.LOWEST)
	private void onPlayerQuit( PlayerQuitEvent event ) {
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
	private void userJoinedWorld( @NonNull Player user, @NonNull World world, WorldData worldData ) {

		final UUID uuid = user.getUniqueId();

		// TODO ASYNC? Could cause issues..
		UserData data = UserData.load( core, world, uuid );
		loadedUserData.put( uuid, data );

		boolean isOwner = user.getUniqueId().equals( WorldDimension.getUUIDOfWorld( world ) );

		if( worldData != null && !isOwner ) {

			if( worldData.getBannedUsers().containsKey( uuid ) ) {

				PunishEntry banEntry = worldData.getBannedUsers().get( uuid );
				if( banEntry != null && banEntry.isBanned() ) {

					List<String> messages = new ArrayList<>();
					messages.add( ChatColor.RED + "You are banned from this server." );
					messages.add( ChatColor.RED + "Reason:" );
					messages.addAll(
							Arrays.asList( banEntry.getReason().split( "\n" ) ).stream()
									.map( s -> ChatColor.translateAlternateColorCodes( '&', s ) )
									.collect( Collectors.toList() )
					);
					if( banEntry.getTime() > 0 )
						messages.add( ChatColor.RED + "You will be unbanned in " + MinecraftlyUtil.getTimeString( banEntry.getRemainingBanTime() ) + "." );

					messages.stream().forEach( user::sendMessage );
					user.kickPlayer( "$$$" + Joiner.on( "\n" ).join( messages ) );
					// TODO Send to own world.
					return;

				}

			}

			if( worldData.isWhiteListed() && !worldData.getWhiteListedUsers().contains( uuid ) ) {

				user.sendMessage( ChatColor.RED + "You're not white listed on this server, sorry." );
				// TODO Send to own world.
				user.kickPlayer( "$$$" + ChatColor.RED + "You're not white listed on this server, sorry." );
				return;

			}

			if( worldData.getTrustedUsers().contains( uuid ) ) {
				user.setGameMode( GameMode.SURVIVAL );
			} else {
				user.setGameMode( GameMode.ADVENTURE );
			}

		} else if( isOwner ) {
			user.setGameMode( GameMode.SURVIVAL );
		}

		user.setBedSpawnLocation( data.getBedLocation() );

		Location lastLocation = data.getLastLocation() != null ? data.getLastLocation() : world.getSpawnLocation();
		user.teleport( lastLocation, PlayerTeleportEvent.TeleportCause.PLUGIN );

	}

	/**
	 * Processes the data and saves it on world leave.
	 *
	 * @param user The player to process.
	 * @param world The world which they left.
	 */
	private void userLeftWorld( @NonNull Player user, @NonNull World world ) {

		// Okay we keep this because it's likely to get removed after this is call finishes.
		final UserData userData = loadedUserData.get( user.getUniqueId() );
		if( userData != null ) dataToBeSaved.add( userData );
		loadedUserData.remove( user.getUniqueId(), userData );

	}

	public void save( AbstractLocalData abstractLocalData ) {
		dataToBeSaved.add( abstractLocalData );
	}

	public UserData getUserData( @NonNull UUID uuid ) {
		return loadedUserData.get( uuid );
	}

	public WorldData getWorldData( @NonNull World world ) {

		UUID uuid = WorldDimension.getUUIDOfWorld( world );
		if( uuid == null ) return null;

		return getWorldData( uuid );

	}

	public WorldData getWorldData( @NonNull UUID uuid ) {
		return loadedWorldData.get( uuid );
	}

}
