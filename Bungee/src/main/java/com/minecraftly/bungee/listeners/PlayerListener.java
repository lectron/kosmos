/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.listeners;

import com.minecraftly.bungee.MinecraftlyBungeeCore;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class PlayerListener implements Listener {

	private final MinecraftlyBungeeCore core;

	@EventHandler
	public void onPlayerKickEvent( ServerKickEvent event ) {

		BaseComponent[] oldKickReason = event.getKickReasonComponent();

		if ( oldKickReason.length > 1 && oldKickReason[0].toPlainText().startsWith( "$$$" ) ) {

			event.setKickReasonComponent( Arrays.copyOfRange( oldKickReason, 1, oldKickReason.length ) );
			event.setCancelled( true );
			core.sendToServer( event.getPlayer().getUniqueId(), event.getPlayer().getUniqueId(), true, false );

		}

	}

}
