/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.connection;

import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.bungee.MinecraftlyBungeePlugin;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.util.DnsHelper;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Handshake;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handle reconnections of players.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
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
		UUID uuidToJoin = player.getUniqueId();

		try ( Jedis jedis = core.getJedis() ) {

			// First of all, get the information we need! TODO make this last.
			try {
				core.getUUIDManager().setUuid( jedis, player.getName(), player.getUniqueId() );
			} catch ( ProcessingException e ) {
				e.printStackTrace();
			}

			String joinUsername = null;

			// If the domain name isn't ours, resolve the cname.
			if ( !isMinecraftly( virtualHostAddress.getHostString() ) ) {
				joinUsername = DnsHelper.getCname( virtualHostAddress.getHostString() );
			}

			// If the cname is null, return the host string.
			if ( joinUsername == null ) {
				joinUsername = virtualHostAddress.getHostString();
			}

			// Split the hostString/cname to get the first section.
			joinUsername = joinUsername.split( "\\.", 2 )[0];

			boolean uuidSet = false;
			try {
				if ( joinUsername.length() > 16 ) {
					uuidToJoin = MinecraftlyUtil.convertFromNoDashes( joinUsername );
					uuidSet = true;
				}
			} catch ( IllegalArgumentException ignored ) {
			}

			// Get the UUID from the name if it exists.
			try {
				if ( !uuidSet && core.getUUIDManager().hasUuid( jedis, joinUsername ) ) {
					uuidToJoin = core.getUUIDManager().getUuid( jedis, joinUsername );
				}
			} catch ( ProcessingException e ) {
				// TODO translations?
				core.getLogger().log( Level.SEVERE, "Error getting the server for \"" + joinUsername + "\".", e );
			}

			// Check the world isn't already loaded, and if it is, return the server where it's loaded.
			try {
				if ( core.getWorldManager().hasServer( jedis, uuidToJoin ) ) {

					String serverId = core.getWorldManager().getServer( jedis, uuidToJoin );
					setHandshake( player, uuidToJoin );
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
				setHandshake( player, uuidToJoin );
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

	private void setHandshake( ProxiedPlayer player, UUID uuidToJoin ) {
		Handshake hs = MinecraftlyBungeeCore.ReflectionUtil.getHandshake( player.getPendingConnection() );
		if ( hs != null ) {
			hs.setHost( uuidToJoin.toString() + ".m.ly" );
		}
	}

	private boolean isMinecraftly( String h ) {
		h = h.toLowerCase();
		return h.endsWith( "m.ly" ) || h.endsWith( "minecraft.ly" ) || h.endsWith( "minecraftly.com" ) || h.endsWith( "minecraftly.net" ) || h.endsWith( "minecraftly.org" );
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
