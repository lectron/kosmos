/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit;

import com.minecraftly.bukkit.commands.ShoutCommand;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class ChatHandler implements Runnable {

	public final MinecraftlyBukkitCore core;

	private final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();

	private Jedis jedis = null;

	public void queueMessage( String string, UUID playerUuid ) {
		messageQueue.add( new Message( string, playerUuid ) );
	}

	@Override
	public void run() {

		for ( int i = 0; i < 11; i++ ) {

			Jedis jedis = getJedis();
			if ( jedis == null ) return;

			Message message = messageQueue.poll();
			if ( message == null ) continue;

			Player player = core.getOriginObject().getServer().getPlayer( message.getUuid() );
			if ( player == null ) continue;

			ShoutCommand.doChat( jedis, core, player, message.getString(), null );

		}

	}

	public Jedis getJedis() {
		try {
			return jedis == null || !jedis.isConnected() ? (jedis = core.getJedis()) : jedis;
		} catch ( NoJedisException e ) {
			core.getLogger().log( Level.WARNING, "Unable to get Jedis for the chat", e );
			return null;
		}
	}

	@Data
	public static class Message {

		private final String string;
		private final UUID uuid;

	}

}
