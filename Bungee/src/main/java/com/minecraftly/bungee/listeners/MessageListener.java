/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.listeners;

import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.bungee.commands.tpa.TpaCommand;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.event.MessageEvent;
import com.minecraftly.core.eventbus.EventHandler;
import com.minecraftly.core.eventbus.Listener;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class MessageListener implements Listener {

	private final MinecraftlyBungeeCore core;

	@EventHandler
	public final void onPubSubMessage( MessageEvent event ) {

		RedisKeys key = RedisKeys.keyFromString( event.getChannel() );
		if ( key == null ) return;

		switch ( key ) {

			case TRANSPORT:
				doTransport( event.getMessage() );
				break;

			case IDENTIFY:
				doIdentify( event.getMessage() );
				break;

			default:
				break;

		}

	}

	private void doTransport( String message ) {

		String[] messageParts = message.split( "\\000" );

		if ( messageParts.length == 3 && messageParts[0].equalsIgnoreCase( "TPDECLINE" ) ) {

			UUID requesterUuid = UUID.fromString( messageParts[1] );
			ProxiedPlayer player;
			if ( (player = core.getOriginObject().getProxy().getPlayer( requesterUuid )) != null ) {
				player.sendMessage( ChatColor.DARK_PURPLE + messageParts[2] + " has declined your teleport request." );
			}

		} else if ( messageParts.length == 3 && messageParts[0].equalsIgnoreCase( "TPACCEPT" ) ) {

			UUID requesterUuid = UUID.fromString( messageParts[1] );
			ProxiedPlayer player = core.getOriginObject().getProxy().getPlayer( requesterUuid );
			if ( player == null ) return;

			UUID playerUuid = UUID.fromString( messageParts[2] );

			try ( Jedis jedis = core.getJedis() ) {

				try {
					UUID playerServer = core.getPlayerManager().getServer( jedis, playerUuid );
					if ( playerServer != null )
						core.sendToServer( requesterUuid, playerServer, true, true );
				} catch ( ProcessingException e ) {
					e.printStackTrace();
				}

			} catch ( NoJedisException e ) {
				e.printStackTrace();
			}

		} else if ( messageParts.length == 4 && messageParts[0].equalsIgnoreCase( "TPA" ) ) {

			UUID playerUuid = UUID.fromString( messageParts[3] );
			ProxiedPlayer player = core.getOriginObject().getProxy().getPlayer( playerUuid );
			if ( player == null ) return;

			UUID requesterUuid = UUID.fromString( messageParts[1] );
			String requesterName = messageParts[2];

			TpaCommand.sendTpaRequest( player, requesterUuid, requesterName );

		} else if ( ( messageParts.length == 3 ) && messageParts[0].equalsIgnoreCase( "SENDP" ) ) {

			UUID playerUuid = UUID.fromString( messageParts[1] );
			UUID worldUuid = UUID.fromString( messageParts[2] );

			core.sendToServer( playerUuid, worldUuid, true, false );

		}

	}

	private void doIdentify( String message ) {

		if ( "suicide".equalsIgnoreCase( message ) ) {
			ProxyServer.getInstance().stop( "Stopped by redis network suicide message!" );
			return;
		}

	}

}
