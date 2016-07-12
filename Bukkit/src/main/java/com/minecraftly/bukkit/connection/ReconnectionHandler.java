/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.connection;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * Deals with the vhosts essentially.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
public class ReconnectionHandler {

	private final MinecraftlyBukkitCore core;

	private final ConcurrentHashMap<UUID, String> userHostMap = new ConcurrentHashMap<>();

	/**
	 * Get the vhost of a UUID.
	 *
	 * @param uuid The UUID of a player's host.
	 * @return The vhost string.
	 */
	public String getHostOf( @NonNull UUID uuid ) {
		return userHostMap.get( uuid );
	}

	/**
	 * Get the vhost of a player.
	 *
	 * @param player The Player of whom's host to get.
	 * @return The vhost string.
	 */
	public String getHostOf( @NonNull Player player ) {
		return getHostOf( player.getUniqueId() );
	}

	/**
	 * Sets the vhost string belonging to UUID.
	 *
	 * @param uuid     The UUID of who's using the hostname.
	 * @param hostname The vhost string.
	 */
	public void setHostOf( @NonNull UUID uuid, @NonNull String hostname ) {
		userHostMap.put( uuid, hostname );
	}

	/**
	 * Sets the vhost string belonging to a player.
	 *
	 * @param player   The player of whom is using the host name.
	 * @param hostname The vhost string.
	 */
	public void setHostOf( @NonNull Player player, @NonNull String hostname ) {
		setHostOf( player.getUniqueId(), hostname );
	}

	/**
	 * Remove the stored vhost of the UUID.
	 *
	 * @param uuid The UUID of who's vhost is being removed.
	 */
	public void removeHost( @NonNull UUID uuid ) {
		userHostMap.remove( uuid );
	}

	/**
	 * Removed the stored vhost of the Player.
	 *
	 * @param player The player of whom to remove the vhost.
	 */
	public void removeHost( @NonNull Player player ) {
		removeHost( player.getUniqueId() );
	}

	/**
	 * Clear all stored UUID-vhosts.
	 */
	public void clear() {
		userHostMap.clear();
	}

	/**
	 * Get the world which UUID is using, gotten by the regex matching the vhost.
	 * Should be called async.
	 *
	 * @param uuid The UUID of who's world you want to get.
	 * @return The UUID of the owner of the world.
	 */
	public UUID getWorldOf( @NonNull UUID uuid ) {

		// Default to the uuid of the original person.
		UUID uuidToJoin = uuid;
		String hostName = getHostOf( uuid );

		if ( hostName == null || hostName.trim().isEmpty() ) return uuidToJoin;

		// Remove the port that's appended.
		hostName = hostName.trim().split( ":" )[0].toLowerCase();

		try ( Jedis jedis = core.getJedis() ) {

			Matcher matcher;

			if( core.getConfig().getDomainNameRegex() != null ) {
				matcher = core.getConfig().getDomainNamePattern().matcher( hostName.trim() );
			} else {
				matcher = null;
			}

			if ( matcher == null || matcher.find() && matcher.groupCount() >= 2 ) {

				String joinUsername;

				if( matcher == null ) {
					joinUsername = hostName.split( "\\.", 2 )[0];
				} else {
					joinUsername = matcher.group( 1 );
				}

				try {
					if ( core.getUUIDManager().hasUuid( jedis, joinUsername ) ) {
						uuidToJoin = core.getUUIDManager().getUuid( jedis, joinUsername );
					}
				} catch ( ProcessingException e ) {
					// TODO translations?
					core.getLogger().log( Level.SEVERE, "Error getting the server for \"" + joinUsername + "\".", e );
				}

			}

		} catch ( NoJedisException e ) {
			e.printStackTrace();
		}

		return uuidToJoin;

	}

}
