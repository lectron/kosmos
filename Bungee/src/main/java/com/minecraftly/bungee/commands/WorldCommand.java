/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.commands;

import com.google.common.collect.ImmutableSet;
import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class WorldCommand extends Command implements TabExecutor {

	private final MinecraftlyBungeeCore core;

	public WorldCommand( MinecraftlyBungeeCore core) {
		super( "world" );
		this.core = core;
	}

	@Override
	public void execute( CommandSender sender, String[] args ) {

		if( !(sender instanceof ProxiedPlayer) ) {
			sender.sendMessage( ChatColor.RED + "Only players can change worlds." );
			return;
		}

		if( args.length != 1 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			sender.sendMessage( ChatColor.YELLOW + " /world <worldname>" );
			return;
		}

		final String worldName = args[0].trim();
		final ProxiedPlayer player = ((ProxiedPlayer) sender);

		core.getOriginObject().getProxy().getScheduler().runAsync( core.getOriginObject(), () -> {

			UUID uuidToJoin = null;

			try ( Jedis jedis = core.getJedis() ) {

				boolean uuidSet = false;
				try {
					if ( worldName.length() > 16 ) {
						uuidToJoin = MinecraftlyUtil.convertFromNoDashes( worldName );
						uuidSet = true;
					}
				} catch ( IllegalArgumentException ignored ) {
				}

				// Get the UUID from the name if it exists.
				try {
					if ( !uuidSet && core.getUUIDManager().hasUuid( jedis, worldName ) ) {
						uuidToJoin = core.getUUIDManager().getUuid( jedis, worldName );
					}
				} catch ( ProcessingException e ) {
					// TODO translations?
					core.getLogger().log( Level.SEVERE, "Error getting the server for \"" + worldName + "\".", e );
				}

			} catch ( NoJedisException e ) {
				core.getLogger().log( Level.SEVERE, "There was an error fetching jedis!", e );
			}

			if( uuidToJoin == null ) {
				player.sendMessages( ChatColor.RED + "We were unable to find a world by that name." );
				return;
			}

			if( core.sendToServer( player.getUniqueId(), uuidToJoin, false ) ) {

				player.chat( "/world " + uuidToJoin.toString() );

			}

		} );

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
