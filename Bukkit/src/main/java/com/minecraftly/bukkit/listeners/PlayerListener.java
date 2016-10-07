/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.listeners;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.commands.ShoutCommand;
import com.minecraftly.bukkit.exceptions.WorldDoesNotExistException;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * The listener dealing with most if not all player related events.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class PlayerListener implements Listener, Closeable {

	private final MinecraftlyBukkitCore core;
	private final ConcurrentLinkedQueue<String> worldsToBeRemoved = new ConcurrentLinkedQueue<>();
	private final BukkitRunnable worldUnloader = new BukkitRunnable() {

		@Override
		public void run() {

			String worldName = worldsToBeRemoved.poll();
			World world;
			if ( worldName != null && (world = Bukkit.getWorld( worldName )) != null ) {
				if ( Bukkit.unloadWorld( world, true ) ) {
					core.getLogger().log( Level.INFO, "Unloaded world \"" + worldName + "\"" );
				} else {
					System.out.println( "There was an error removing world " + worldName );
					worldsToBeRemoved.add( worldName );
				}
			}

		}

	};
	private HashMap<UUID, World> playerDeathWorlds = new HashMap<>();

	/**
	 * Deal with player login, storing the Vhost to {@link com.minecraftly.bukkit.connection.ReconnectionHandler}
	 *
	 * @param event PlayerLoginEvent.
	 */
	@EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = true )
	public void onPlayerLogin( PlayerLoginEvent event ) {

		if ( event.getResult() == PlayerLoginEvent.Result.ALLOWED ) {
			core.getReconnectionHandler().setHostOf( event.getPlayer(), event.getHostname() );
		}

	}

	/**
	 * Deal with player chat and make it per world.
	 *
	 * @param event AsyncPlayerChatEvent
	 */
	@EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = true )
	public void onPlayerChat( AsyncPlayerChatEvent event ) {

		if ( event.getRecipients() instanceof ShoutCommand.MinecraftlySet ) return;

		/*if ( event.getMessage().startsWith( "!" ) ) {
			String message = event.getMessage().substring( 1 );
			core.getChatHandler().queueMessage( message, event.getPlayer().getUniqueId() );
			event.setCancelled( true );
			return;
		}*/

		// Yes I'm aware this is naughty on Monitor.
		if ( event.getMessage().equalsIgnoreCase( "worldpls" ) ) {
			event.getPlayer().sendMessage( "Your world is " + event.getPlayer().getWorld().getName() );
			event.setCancelled( true );
		}

		/*World world = WorldDimension.getBaseWorld( event.getPlayer().getWorld() );
		WorldData worldData;
		if ( (worldData = core.getPlayerHandler().getWorldData( world )) != null ) {
			if ( worldData.getMutedUsers().containsKey( event.getPlayer().getUniqueId() ) )
				event.setCancelled( true ); // TODO not monitor.
		}

		// Per world chat.
		Set<Player> recipients = event.getRecipients();
		recipients.clear();
		recipients.addAll( WorldDimension.getPlayersAllDimensions( world ) );*/

	}

	/**
	 * Deal with player deaths.
	 * Allows for respawning in the same world and keepinv.
	 *
	 * @param event PlayerRespawnEvent
	 */
	@EventHandler( priority = EventPriority.LOWEST )
	public void onPlayerDeath( PlayerDeathEvent event ) {

		playerDeathWorlds.put( event.getEntity().getUniqueId(), event.getEntity().getWorld() );

		event.setKeepInventory( true );
		event.setKeepLevel( true );

	}

	/**
	 * Deal with player's death messages.
	 * NAUGHTY!
	 *
	 * @param event The death event.
	 */
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPlayerDeathMon( PlayerDeathEvent event ) {

		String message = event.getDeathMessage();
		event.setDeathMessage( null );

		WorldDimension.getPlayersAllDimensions( event.getEntity().getWorld() )
				.forEach( player -> player.sendMessage( message ) );

	}

	/**
	 * Deals with the player respawn, ensuring they spawn in the same universe they die in.
	 *
	 * @param event PlayerRespawnEvent
	 */
	@EventHandler
	public void onPlayerRespawn( PlayerRespawnEvent event ) {

		World world = playerDeathWorlds.get( event.getPlayer().getUniqueId() );
		playerDeathWorlds.remove( event.getPlayer().getUniqueId() );
		if ( world == null ) return;

		world = WorldDimension.getBaseWorld( world );
		if ( world == null ) return;

		event.setRespawnLocation( world.getSpawnLocation() );

	}

	/**
	 * Deal with player teleport and unloads the world if it's not on the same world.
	 *
	 * @param event PlayerTeleportEvent
	 */
	@EventHandler( ignoreCancelled = true, priority = EventPriority.MONITOR )
	public void onPlayerTeleport( PlayerTeleportEvent event ) {
		final Player player = event.getPlayer();
		World from = WorldDimension.getBaseWorld( event.getFrom().getWorld() );
		World to = WorldDimension.getBaseWorld( event.getTo().getWorld() );

		if ( !from.equals( to ) ) {
			playerLeftWorld( player.getWorld() );
		}

	}

	/**
	 * Handle player joining, server loading and server teleportation.
	 *
	 * @param event PlayerJoinEvent
	 */
	@EventHandler( priority = EventPriority.LOWEST ) // LOWEST to allow playerhandler to use and set location.
	public void onPlayerJoin( PlayerJoinEvent event ) {

		// We really want to clear the join message.. TODO make it per world?
		event.setJoinMessage( null );

		event.getPlayer().setGameMode( GameMode.ADVENTURE );

		try {

			// Async loading of relevant world ID using {@link ReconnectionHandler}
			core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {

				final UUID playerUuid = core.getReconnectionHandler().getWorldOf( event.getPlayer().getUniqueId() );

				// Sync world loading via Bukkit api.
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), () -> {

					//event.getPlayer().sendMessage( "Loading world..." );
					UUID joinUUID = playerUuid;

					// If the world from above isn't available, use the player's world.
					int trys = 0;
					while ( trys < 2 ) {
						try {
							World world = core.getWorldHandler().loadWorld( event.getPlayer().getUniqueId(), joinUUID.toString(), World.Environment.NORMAL );
							if ( world == null ) {
								continue;
							}
							event.getPlayer().teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN );
							System.out.println( "Using world " + world.getName() + " for " + event.getPlayer().getName() + "|| Spawn location: " + world.getSpawnLocation() );
							core.playerJoined( event.getPlayer().getUniqueId(), playerUuid );

							core.getPlayerHandler().onPlayerJoin( event );

							break;
						} catch ( WorldDoesNotExistException ex ) {
							event.getPlayer().sendMessage( "We were unable to load the requested world, we're sending you to yours." );
							joinUUID = event.getPlayer().getUniqueId();
							trys++;
						}
					}

					return null;

				} );

			} );

		} catch ( Exception ex ) {
			// TODO
			ex.printStackTrace();
		}

		//event.getPlayer().getInventory().clear();
		//Bukkit.getScheduler().runTaskLater( core.getOriginObject(), () -> core.getInventoryHandler().doPlayerLoad( event.getPlayer() ), 20 * 3 );

	}

	/**
	 * Deal with player quitting.
	 * Unload world and remove the host.
	 *
	 * @param event PlayerQuitEvent
	 */
	@EventHandler
	public void onPlayerQuit( PlayerQuitEvent event ) {

		// Stop global quit message. TODO per world.
		event.setQuitMessage( null );

		// Remove the player from all the maps and what not.
		playerDeathWorlds.remove( event.getPlayer().getUniqueId() );
		core.getReconnectionHandler().removeHost( event.getPlayer() );

		// Unload the world if needs be.
		final World world = event.getPlayer().getWorld();
		core.getOriginObject().getServer().getScheduler().runTaskLater( core.getOriginObject(), () -> playerLeftWorld( world ), 10L );

		//core.getInventoryHandler().doPlayerUnload( event.getPlayer() );

		// Call core playerExit for whatever core wants it to.
		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> core.playerExited( event.getPlayer().getUniqueId(), WorldDimension.getUUIDOfWorld( world ) ) );

	}

	/**
	 * Handle world protection.
	 *
	 * @param event The interaction event.
	 */
	@EventHandler( ignoreCancelled = true, priority = EventPriority.HIGH )
	public void onInteractEvent( PlayerInteractEvent event ) {

		if ( event.getPlayer().getGameMode() == GameMode.ADVENTURE ) {
			event.setUseItemInHand( Event.Result.DENY );
			event.setUseInteractedBlock( Event.Result.DENY );
			event.setCancelled( true );
		}

	}

	public void playerLeftWorld( @NonNull World world ) {

		List<Player> worldPlayers = WorldDimension.getPlayersAllDimensions( world );

		try {
			UUID uuid = UUID.fromString( WorldDimension.getBaseWorld( world ).getName() );

			if ( worldPlayers == null || worldPlayers.size() < 1 ) {

				for ( WorldDimension dim : WorldDimension.values() ) {
					System.out.println( "Unloading dim " + dim + " for " + uuid );
					World dimWorld = dim.convertTo( world );
					if ( dimWorld != null ) {
						try {
							System.out.println( "Unloading world " + dimWorld.getName() );
							Bukkit.unloadWorld( dimWorld, true );

						} catch ( Exception ex ) {
							core.getLogger().log( Level.WARNING, "Unable to save & unload dimworld: \"" + dimWorld.getName() + "\"", ex );
						}
					}
				}

				try {

					String worldName = world.getName();
					if ( Bukkit.unloadWorld( world, true ) ) {
						core.getLogger().log( Level.INFO, "Unloaded world \"" + worldName + "\"" );
					} else {
						worldsToBeRemoved.add( worldName );
					}

				} catch ( Exception ex ) {
					core.getLogger().log( Level.WARNING, "Unable to save & unload dimworld: \"" + world.getName() + "\"", ex );
				}

				Bukkit.getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
					try ( Jedis jedis = core.getJedis() ) {
						core.getWorldManager().setServer( jedis, uuid, null );
					} catch ( NoJedisException | ProcessingException e ) {
						e.printStackTrace();
					}

				} );

			}

		} catch ( IllegalArgumentException ignored ) {
		}

	}


	public void load() {
		worldUnloader.runTaskTimer( core.getOriginObject(), 20, 
20 );
	}

	@Override
	public void close() throws IOException {
		worldUnloader.cancel();
		worldsToBeRemoved.clear();
	}
}
