/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.runnables;

import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Subscribes to all the redis channels appropriate.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
public class SubscribeTask extends RunnableData {

	@NonNull
	private final MinecraftlyCore core;

	@Override
	public TimeUnit getTimeUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	public long getRepeatTime() {
		return 0;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public Collection<TaskType> getTaskTypes() {
		return Arrays.asList( TaskType.ASYNC );
	}

	@Override
	public void run() {

		String[] channels = RedisKeys.valuesAsStringCollection().toArray( new String[RedisKeys.valuesAsStringCollection().size()] );

		try ( Jedis jedis = core.getJedis() ) {
			jedis.subscribe( core.getMessageListener(), channels );
		} catch ( NoJedisException e ) {
			e.printStackTrace();
		}

	}
}
