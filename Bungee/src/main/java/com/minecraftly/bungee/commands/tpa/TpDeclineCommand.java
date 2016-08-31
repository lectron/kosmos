/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.commands.tpa;

import com.google.common.collect.ImmutableSet;
import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.NoJedisException;
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
public class TpDeclineCommand extends Command implements TabExecutor {

	private final MinecraftlyBungeeCore core;

	public TpDeclineCommand( MinecraftlyBungeeCore core ) {
		super( "tpno", null, "tpnu", "tpdecline" );
		this.core = core;
	}

	@Override
	public void execute( CommandSender sender, String[] args ) {

		if ( !(sender instanceof ProxiedPlayer) ) {
			sender.sendMessage( ChatColor.RED + "Only players can tpdecline." );
			return;
		}

		if ( args.length != 0 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			sender.sendMessage( ChatColor.YELLOW + " /tpdecline" );
			return;
		}

		final ProxiedPlayer player = ((ProxiedPlayer) sender);

		core.getOriginObject().getProxy().getScheduler().runAsync( core.getOriginObject(), () -> {

			try ( Jedis jedis = core.getJedis() ) {

				try {
					if ( core.getTransportManager().hasPendingReq( jedis, player.getUniqueId() ) ) {

						UUID uuid = core.getTransportManager().getRequester( jedis, player.getUniqueId() );
						core.getTransportManager().setRequester( jedis, player.getUniqueId(), null );

						jedis.publish( RedisKeys.TRANSPORT.toString(), "TPDECLINE\00" + uuid + "\00" + player.getName() + "\00" );

						// TODO uuid -> name.
						player.sendMessage( ChatColor.RED + "You declined a request from " + uuid );
					}
				} catch ( Exception ignored ) {
				}

			} catch ( NoJedisException e ) {
				core.getLogger().log( Level.SEVERE, "There was an error fetching jedis!", e );
			}

			player.sendMessages( ChatColor.RED + "We can't find/decline any pending requests!" );

		} );

	}

	@Override
	public Iterable<String> onTabComplete( CommandSender sender, String[] args ) {

		if ( args.length != 0 ) {
			return ImmutableSet.of();
		}

		String search = args[0].toLowerCase();
		return ProxyServer.getInstance().getPlayers().stream()
				.filter( player -> player.getName().toLowerCase().startsWith( search ) )
				.map( ProxiedPlayer::getName )
				.collect( Collectors.toList() );

	}
}
