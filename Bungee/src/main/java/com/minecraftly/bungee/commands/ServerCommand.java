/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.commands;

import com.minecraftly.bungee.MinecraftlyBungeeCore;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class ServerCommand extends Command implements TabExecutor {

	private final MinecraftlyBungeeCore core;

	public ServerCommand( MinecraftlyBungeeCore core ) {
		super( "server", "bungeecord.command.server" );
		this.core = core;
	}

	@Override
	public void execute( CommandSender sender, String[] args ) {

		if ( !(sender instanceof ProxiedPlayer) ) return;

		ProxiedPlayer player = (ProxiedPlayer) sender;
		Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();

		if ( args.length == 0 ) {

			player.sendMessage( ProxyServer.getInstance().getTranslation( "current_server", getServerName( player.getServer().getInfo() ) ) );
			TextComponent serverList = new TextComponent( ProxyServer.getInstance().getTranslation( "server_list" ) );
			serverList.setColor( ChatColor.GOLD );
			boolean first = true;

			for ( ServerInfo server : servers.values() ) {

				if ( server.canAccess( player ) ) {

					String serverName = getServerName( server );

					TextComponent serverTextComponent = new TextComponent( first ? serverName : ", " + serverName );
					serverTextComponent.setHoverEvent( createHoverComponent( server.getPlayers().size() ) );
					serverTextComponent.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/server " + server.getName() ) );
					serverList.addExtra( serverTextComponent );

					first = false;

				}

			}

			player.sendMessage( serverList );

		} else {

			ServerInfo server = servers.get( args[0] );

			if ( server == null ) {
				player.sendMessage( ProxyServer.getInstance().getTranslation( "no_server" ) );
			} else if ( !server.canAccess( player ) ) {
				player.sendMessage( ProxyServer.getInstance().getTranslation( "no_server_permission" ) );
			} else {

				if ( !args[0].equalsIgnoreCase( "world" ) ) {
					player.connect( server );
				} else {
					ProxyServer.getInstance().getPluginManager().dispatchCommand( sender, "world " + player.getUniqueId() );
				}

			}

		}

	}

	@Override
	public Iterable<String> onTabComplete( CommandSender sender, String[] args ) {

		if ( args.length > 1 ) return Collections.emptyList();

		final String lower = args.length == 0 ? "" : args[0].toLowerCase();
		return ProxyServer.getInstance().getServers().values().stream()
				.filter( serverInfo -> serverInfo.getName().toLowerCase().startsWith( lower ) && serverInfo.canAccess( sender ) )
				.map( ServerInfo::getName )
				.collect( Collectors.toList() );

	}

	private String getServerName( ServerInfo info ) {

		if ( ProxyServer.getInstance().getServers().values().contains( info ) )
			return info.getName();

		return "world";

	}

	private HoverEvent createHoverComponent( int count ) {
		return new HoverEvent( HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder( count + (count == 1 ? " player" : " players") + "\n" )
						.append( "Click to connect to the server" ).italic( true )
						.create()
		);
	}

}
