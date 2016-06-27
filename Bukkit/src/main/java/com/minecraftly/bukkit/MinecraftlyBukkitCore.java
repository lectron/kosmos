/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit;

import com.minecraftly.bukkit.connection.ReconnectionHandler;
import com.minecraftly.bukkit.event.MinecraftlyEvent;
import com.minecraftly.bukkit.world.WorldHandler;
import com.minecraftly.bukkit.world.data.global.inventories.InventoryHandler;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.configuration.MinecraftlyConfiguration;
import com.minecraftly.core.event.MCLYEvent;
import com.minecraftly.core.event.events.MessageEvent;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.runnables.RunnableData;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MinecraftlyBukkitCore extends MinecraftlyCore<MinecraftlyBukkitPlugin> {

	@Getter
	private WorldHandler worldHandler = new WorldHandler( this );

	@Getter
	private ReconnectionHandler reconnectionHandler = new ReconnectionHandler( this );

	@Getter
	private InventoryHandler inventoryHandler = new InventoryHandler( this );

	public MinecraftlyBukkitCore( @NonNull MinecraftlyBukkitPlugin plugin ) {
		super( plugin.getLogger(), plugin.getDataFolder(), plugin, Bukkit.getPort() );
	}

	public MinecraftlyBukkitCore( @NonNull MinecraftlyBukkitPlugin plugin, MinecraftlyConfiguration configuration ) {
		super( plugin.getLogger(), plugin.getDataFolder(), plugin, Bukkit.getPort(), configuration );
	}

	@Override
	public void shutdown() {

		// TODO kick players properly :P

	}

	@Override
	public <T extends MCLYEvent> T callEvent( T event ) {

		MinecraftlyEvent<T> event1 = new MinecraftlyEvent<T>( event );
		getOriginObject().getServer().getPluginManager().callEvent( event1 );
		event1.postCall();

		// TODO load worlds better.
		if ( event instanceof MessageEvent ) {
			MessageEvent messageEvent = ((MessageEvent) event);

			String[] message = messageEvent.getMessage().split( "\\000" );
			String channel = messageEvent.getChannel().trim();

			if ( RedisKeys.WORLD_REPO.toString().equalsIgnoreCase( channel ) && message.length == 4 && message[0].equalsIgnoreCase( "WORLD" ) && message[1].equalsIgnoreCase( "LOAD" ) ) {

				String serverId = message[2];

				if ( serverId.equals( identify() ) ) {

					UUID uuid = UUID.fromString( message[3] );

					getOriginObject().getServer().getScheduler().callSyncMethod( getOriginObject(), () -> {

						getWorldHandler().loadWorld( uuid.toString(), World.Environment.NORMAL );
						return null;

					} );

				}

			}

		}

		return event1.getEvent();

	}

	@Override
	public int getMaxPlayers() {
		return getOriginObject().getServer().getMaxPlayers();
	}

	@Override
	public int getPlayerCount() {
		return getOriginObject().getServer().getOnlinePlayers().size();
	}

	@Override
	public void runTask( RunnableData runnable ) {

		runnable.check();

		Collection<RunnableData.TaskType> types = runnable.getTaskTypes();
		BukkitScheduler scheduler = getOriginObject().getServer().getScheduler();

		if ( types.contains( RunnableData.TaskType.ASYNC ) ) {

			if ( types.contains( RunnableData.TaskType.REPEATING ) ) {
				scheduler.runTaskTimerAsynchronously( getOriginObject(), runnable, runnable.getStartTicks(), runnable.getRepeatTicks() );
			} else if ( types.contains( RunnableData.TaskType.DELAYED ) ) {
				scheduler.runTaskLaterAsynchronously( getOriginObject(), runnable, runnable.getStartTicks() );
			} else {
				scheduler.runTaskAsynchronously( getOriginObject(), runnable );
			}

		} else if ( types.contains( RunnableData.TaskType.SYNC ) ) {

			if ( types.contains( RunnableData.TaskType.REPEATING ) ) {
				scheduler.runTaskTimer( getOriginObject(), runnable, runnable.getStartTicks(), runnable.getRepeatTicks() );
			} else if ( types.contains( RunnableData.TaskType.DELAYED ) ) {
				scheduler.runTaskLater( getOriginObject(), runnable, runnable.getStartTicks() );
			} else {
				scheduler.runTask( getOriginObject(), runnable );
			}

		}

	}

	@Override
	public void playerJoined( UUID uniqueId ) {
		try ( Jedis jedis = getJedis() ) {
			getServerManager().incrServerPlayerCount( jedis, identify(), 1 );
		} catch ( NoJedisException | ProcessingException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void playerExited( UUID uniqueId ) {
		try ( Jedis jedis = getJedis() ) {
			getServerManager().incrServerPlayerCount( jedis, identify(), -1 );
		} catch ( NoJedisException | ProcessingException e ) {
			e.printStackTrace();
		}
	}

}
