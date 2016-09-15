/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.listeners;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.event.MessageEvent;
import com.minecraftly.core.eventbus.EventHandler;
import com.minecraftly.core.eventbus.Listener;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class MessageListener implements Listener {

	private final MinecraftlyBukkitCore core;

	@EventHandler
	public final void onPubSubMessage( MessageEvent event ) {

		RedisKeys key = RedisKeys.keyFromString( event.getChannel() );
		if ( key == null ) return;

		switch ( key ) {

			case IDENTIFY:
				doIdentify( event.getMessage() );
				break;

			case WORLD_REPO:
				doWorldRepo( event.getMessage().split( "\\000" ) );
				break;

			case CHAT:
				doChat( event.getMessage().split( "\\000" ) );
				break;

			case TRANSPORT:
				doTransport( event.getMessage().split( "\\000" ) );
				break;

			default:
				break;

		}

	}

	private void doTransport( String[] messages ) {

		if ( messages.length == 3 || messages.length == 4 && messages[0].equalsIgnoreCase( "SEND" ) ) {

			Player player = Bukkit.getPlayer( UUID.fromString( messages[1] ) );
			if ( player == null ) return;

			UUID serverUUID = UUID.fromString( messages[2] );

			boolean isTpa = false;
			if ( messages.length == 4 ) isTpa = Boolean.parseBoolean( messages[3] );

			core.sendToServer( player.getUniqueId(), serverUUID, false, isTpa );

		}

	}

	private void doChat( String[] messages ) {

		if ( messages.length == 2 || messages.length == 3 && messages[0].equalsIgnoreCase( "MSG" ) ) {

			String message = ChatColor.translateAlternateColorCodes( '&', messages[1] );
			if ( messages.length == 3 ) {
				Bukkit.broadcast( message, messages[2] );
			} else {
				Bukkit.broadcastMessage( message );
			}

		}

	}

	private void doWorldRepo( String[] messages ) {

		if ( messages.length == 4 && messages[0].equalsIgnoreCase( "WORLD" ) && messages[1].equalsIgnoreCase( "LOAD" ) ) {
			String serverId = messages[2];

			if ( serverId.equals( core.identify() ) ) {

				UUID uuid = UUID.fromString( messages[3] );

				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), () -> {
					core.getWorldHandler().loadWorld( uuid.toString(), World.Environment.NORMAL );
					return null;
				} );

			}
		}

	}

	private void doIdentify( String message ) {

		if ( "suicide".equalsIgnoreCase( message ) ) {
			Bukkit.shutdown();
			return;
		}

	}

}
