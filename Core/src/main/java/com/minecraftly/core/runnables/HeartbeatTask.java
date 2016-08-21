/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.runnables;

import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * The main heartbeat task, the servers know eachother are alive with this.
 * TODO check other servers.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class HeartbeatTask extends RunnableData implements Closeable {

	@NonNull
	private final MinecraftlyCore core;

	/**
	 * A jedis instance to use with the heartbeats.
	 */
	Jedis jedis = null;

	@Override
	public TimeUnit getTimeUnit() {
		return TimeUnit.MILLISECONDS;
	}

	@Override
	public long getRepeatTime() {
		return 1000;
	}

	@Override
	public long getStartTime() {
		return 50;
	}

	@Override
	public Collection<TaskType> getTaskTypes() {
		return Arrays.asList( TaskType.ASYNC, TaskType.REPEATING );
	}

	public Jedis getJedis() throws NoJedisException {
		if ( jedis == null || !jedis.isConnected() )
			jedis = core.getJedis();
		return jedis;
	}

	@Override
	public void run() {

		try {

			Jedis jedis = getJedis();

			if ( core.getServerType() == MinecraftlyCore.ServerType.BUKKIT ) {
				try {
					core.getServerManager().setServer( jedis, core.identify(), core.getPlayerCount() );
				} catch ( ProcessingException e ) {
					core.getLogger().log( Level.WARNING, "Unable to set my playercount for redis!", e );
				}
			}

			jedis.publish( RedisKeys.IDENTIFY.toString(), "HEARTBEAT\000" + core.identify() + "\000" + core.getPlayerCount() + "\000" + core.getMaxPlayers() );

		} catch ( NoJedisException e ) {
			core.getLogger().log( Level.SEVERE, "Unable to send heartbeat!", e );
		}

	}

	@Override
	public void close() {
		if ( jedis != null ) {
			jedis.close();
			jedis = null;
		}
	}
}
