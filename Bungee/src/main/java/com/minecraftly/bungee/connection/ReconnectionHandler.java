/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.connection;

import com.minecraftly.bungee.MinecraftlyBungeePlugin;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * Handle reconnections of players.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
public class ReconnectionHandler extends AbstractReconnectHandler {

	private final MinecraftlyCore<MinecraftlyBungeePlugin> core;

	/**
	 * Get the server linked to the vhost.
	 * This is where all the work happens.
	 *
	 * @param player The player of whom to load.
	 * @return The server to join.
	 */
	@Override
	protected ServerInfo getStoredServer( ProxiedPlayer player ) {

		InetSocketAddress virtualHostAddress = player.getPendingConnection().getVirtualHost();
		Matcher matcher = core.getConfig().getDomainNamePattern().matcher( virtualHostAddress.getHostString() );

		UUID uuidToJoin = player.getUniqueId();

		try ( Jedis jedis = core.getJedis() ) {

			// First of all, get the information we need! TODO make this last.
			try {
				core.getPlayerManager().setUuid( jedis, player.getName(), player.getUniqueId() );
			} catch ( ProcessingException e ) {
				e.printStackTrace();
			}

			// Match the hostname against the regex.
			if ( matcher.find() && matcher.groupCount() >= 2 ) {

				String joinUsername = matcher.group( 1 );

				// Get the UUID from the name if it exists.
				try {
					if ( core.getPlayerManager().hasUuid( jedis, joinUsername ) ) {
						System.out.println( "0.3 | has UUID: " + joinUsername );
						uuidToJoin = core.getPlayerManager().getUuid( jedis, joinUsername );
						System.out.println( "0.4 | UUID: " + uuidToJoin );
					}
				} catch ( ProcessingException e ) {
					// TODO translations?
					core.getLogger().log( Level.SEVERE, "Error getting the server for \"" + joinUsername + "\".", e );
				}

			}

			// Check the world isn't already loaded, and if it is, return the server where it's loaded.
			try {
				if ( core.getWorldManager().hasServer( jedis, uuidToJoin ) ) {

					String serverId = core.getWorldManager().getServer( jedis, uuidToJoin );
					return ProxyServer.getInstance().constructServerInfo( serverId, MinecraftlyUtil.parseAddress( serverId ), "", false );

				}
			} catch ( ProcessingException e ) {
				// TODO translations.
				core.getLogger().log( Level.SEVERE, "Error getting the server for \"" + uuidToJoin + "\".", e );
			}

			// Load the world on the server with least load as it's not already loaded on any others.
			// Also return the server where it's being loaded.
			try {

				String serverId = core.getWorldManager().loadWorld( jedis, uuidToJoin );
				return ProxyServer.getInstance().constructServerInfo( serverId, MinecraftlyUtil.parseAddress( serverId ), "", false );

			} catch ( ProcessingException e ) {
				core.getLogger().log( Level.SEVERE, "Error getting the default server for \"" + uuidToJoin + "\".", e );
			}

		} catch ( NoJedisException e ) {
			e.printStackTrace();
		}

		// fixme some other method?
		// AParrently there's nothing we can do here? Hopefully we'll never end up here.
		return null;

	}

	@Override
	@Deprecated
	public void setServer( ProxiedPlayer proxiedPlayer ) {
	}

	@Override
	@Deprecated
	public void save() {
	}

	@Override
	@Deprecated
	public void close() {
	}

}
