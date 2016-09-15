/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.listeners;

import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.util.Callback;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class DebugListener implements Listener {

	private final MinecraftlyBungeeCore core;

	Callback<Callable<Void>, PlayerInputPair> loadedCallback = param -> () -> {

		try {
			ScriptEngine engine = getScriptEngine( param.getPlayer() );
			engine.eval( param.getInput() );
		} catch ( Exception e ) {
			param.getPlayer().sendMessage( ChatColor.YELLOW + "There was an error executing the script!" );
			param.getPlayer().sendMessage( ChatColor.YELLOW + e.getClass().getName() );
			param.getPlayer().sendMessage( ChatColor.YELLOW + e.getMessage() );
		}

		return null;

	};

	@EventHandler( priority = Byte.MAX_VALUE )
	public void onPluginMessageReceived( PluginMessageEvent event ) {

		if ( !(event.getSender() instanceof ProxiedPlayer) ) return;

		final ProxiedPlayer player = ((ProxiedPlayer) event.getSender());
		final String channel = event.getTag();
		final byte[] message = event.getData();

		try {

			if ( !channel.equals( "NMCLY" ) || !player.hasPermission( "minecraftly.debug" ) ) return;

			DataInputStream dis = new DataInputStream( new ByteArrayInputStream( message ) );
			String input = dis.readUTF();

			if ( input.equalsIgnoreCase( "URL" ) ) {

				final String url = dis.readUTF();
				core.getOriginObject().getProxy().getScheduler().runAsync( core.getOriginObject(), () -> {

					try {
						String downloadedInput = MinecraftlyUtil.downloadText( url );
						loadedCallback.call( new PlayerInputPair( player, downloadedInput ) ).call();
					} catch ( Exception e ) {
						player.sendMessage( ChatColor.YELLOW + "There was an error executing the script!" );
						player.sendMessage( ChatColor.YELLOW + e.getClass().getName() );
						player.sendMessage( ChatColor.YELLOW + e.getMessage() );
					}

				} );

			} else {

				core.getOriginObject().getProxy().getScheduler().runAsync( core.getOriginObject(), () -> {
					try {
						loadedCallback.call( new PlayerInputPair( player, input ) ).call();
					} catch ( Exception e ) {
						player.sendMessage( ChatColor.YELLOW + "There was an error executing the script!" );
						player.sendMessage( ChatColor.YELLOW + e.getClass().getName() );
						player.sendMessage( ChatColor.YELLOW + e.getMessage() );
					}
				} );

			}

		} catch ( IOException ex ) {
			ex.printStackTrace();
		}

	}

	private ScriptEngine getScriptEngine( ProxiedPlayer player ) {

		core.getDebugger().put( "server", ProxyServer.getInstance() );

		ScriptEngine engine = core.getDebugger().getEngine();
		engine.put( "me", player );
		try {
			engine.eval( "function print( message ) { me.sendMessage( message ); }" );
		} catch ( ScriptException ignored ) {
		}

		return engine;

	}

	@Data
	private class PlayerInputPair {

		private final ProxiedPlayer player;
		private final String input;

	}

}
