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

package com.minecraftly.bukkit;

import com.google.gson.GsonBuilder;
import com.minecraftly.bukkit.configuration.LocationAdapter;
import com.minecraftly.bukkit.connection.ReconnectionHandler;
import com.minecraftly.bukkit.listeners.MessageListener;
import com.minecraftly.bukkit.world.WorldHandler;
import com.minecraftly.bukkit.world.data.local.PlayerHandler;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.runnables.RunnableData;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitScheduler;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class MinecraftlyBukkitCore extends MinecraftlyCore<MinecraftlyBukkitPlugin> {

	@Getter
	private WorldHandler worldHandler = new WorldHandler( this );

	@Getter
	private ReconnectionHandler reconnectionHandler = new ReconnectionHandler( this );

	@Getter
	private PlayerHandler playerHandler = new PlayerHandler( this );

	@Getter
	private ChatHandler chatHandler = new ChatHandler( this );

	public MinecraftlyBukkitCore( @NonNull MinecraftlyBukkitPlugin plugin ) {

		super( plugin.getLogger(), plugin.getDataFolder(), plugin, Bukkit.getPort() );
		getEventBus().register( new MessageListener( this ) );

	}

	@Override
	public void shutdown() {

		// TODO kick players properly.

		try {
			playerHandler.close();
		} catch ( IOException e ) {
			getLogger().log( Level.SEVERE, "There was an error closing the player handler!", e );
		}

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
	public void playerJoined( UUID uniqueId, UUID worldUUID ) {
		try ( Jedis jedis = getJedis() ) {
			getServerManager().incrServerPlayerCount( jedis, identify(), 1 );
			getPlayerManager().setServer( jedis, uniqueId, worldUUID );
		} catch ( NoJedisException | ProcessingException e ) {
			getLogger().log( Level.WARNING, "There was an error when " + uniqueId + " joined..", e );
		}
	}

	@Override
	public void playerExited( UUID uniqueId, UUID worldUUID ) {
		try ( Jedis jedis = getJedis() ) {
			getServerManager().incrServerPlayerCount( jedis, identify(), -1 );
			getPlayerManager().setServer( jedis, uniqueId, null );
		} catch ( NoJedisException | ProcessingException e ) {
			getLogger().log( Level.WARNING, "There was an error when " + uniqueId + " joined..", e );
		}
	}

	@Override
	public GsonBuilder processGsonBuilder( @NonNull GsonBuilder gsonBuilder ) {
		return super.processGsonBuilder( gsonBuilder ).registerTypeAdapter( Location.class, LocationAdapter.INSTANCE );
	}

}
