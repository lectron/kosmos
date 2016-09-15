/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.manager.redis;

import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.JedisPubSub;

/**
 * The front end JedisPubSub listener.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class RedisMessageListener extends JedisPubSub {

	private final MinecraftlyCore core;

	@Override
	public void onMessage( String channel, String message ) {
		core.callEvent( new MessageEvent( channel, message ) );
	}

}
