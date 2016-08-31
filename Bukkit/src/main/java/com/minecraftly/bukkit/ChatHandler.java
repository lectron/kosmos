/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit;

import com.minecraftly.bukkit.commands.ShoutCommand;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class ChatHandler implements Runnable {

	public final MinecraftlyBukkitCore core;

	private final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();

	public void queueMessage( String string, UUID playerUuid ) {
		messageQueue.add( new Message( string, playerUuid ) );
	}

	@Override
	public void run() {

		for ( int i = 0; i < 11; i++ ) {

			Message message = messageQueue.poll();
			if ( message == null ) continue;

			Player player = core.getOriginObject().getServer().getPlayer( message.getUuid() );
			if ( player == null ) continue;

			ShoutCommand.doChat( core, player, message.getString(), null );

		}

	}

	@Data
	public static class Message {

		private final String string;
		private final UUID uuid;

	}

}
