/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.minecraftly.bungee.MinecraftlyBungeeCore;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class DebugCommand extends Command implements TabExecutor {

	private final MinecraftlyBungeeCore core;

	public DebugCommand( MinecraftlyBungeeCore core ) {
		super( "mdebug", "minecraftly.debug" );
		this.core = core;
	}

	@Override
	public void execute( CommandSender sender, String[] args ) {

		// TODO bungeecord debug.
		if( sender instanceof ProxiedPlayer ) {
			((ProxiedPlayer) sender).chat( "/mdebug " + Joiner.on( " " ).join( args ) );
		}

	}

	@Override
	public Iterable<String> onTabComplete( CommandSender sender, String[] args ) {

		if ( args.length != 0 )
		{
			return ImmutableSet.of();
		}

		String search = args[0].toLowerCase();
		return ProxyServer.getInstance().getPlayers().stream()
				.filter( player -> player.getName().toLowerCase().startsWith( search ) )
				.map( ProxiedPlayer::getName )
				.collect( Collectors.toList() );

	}

}
