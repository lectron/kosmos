/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.listeners;

import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.event.MessageEvent;
import com.minecraftly.core.eventbus.EventHandler;
import com.minecraftly.core.eventbus.Listener;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class MessageListener implements Listener {

	private final MinecraftlyBungeeCore core;

	@EventHandler
	public final void onPubSubMessage( MessageEvent event ) {

		RedisKeys key = RedisKeys.keyFromString( event.getChannel() );
		if( key == null ) return;

		switch ( key ) {

			case IDENTIFY:
				doIdentify( event.getMessage() );
				break;

			default:
				break;

		}

	}

	private void doIdentify( String message ) {

		if( "suicide".equalsIgnoreCase(message)) {
			ProxyServer.getInstance().stop( "Stopped by redis network suicide message!" );
			return;
		}

	}

}
