/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.manager.redis;

import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * A redis repsitory to track player's movement across the network.
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@AllArgsConstructor
public class PlayerManager {

	@NonNull
	private final MinecraftlyCore core;

	/**
	 * Checks if the player (playerUuid) has a server..
	 *
	 * @param playerUuid The uuid of the player.
	 * @return true if the playerUuid has a server.
	 * @throws ProcessingException if an exception occurs.
	 */
	public boolean hasServer( @NonNull Jedis jedis, @NonNull UUID playerUuid ) throws ProcessingException {
		try {
			return jedis.hexists( RedisKeys.WORLD_REPO.toString(), playerUuid.toString() );
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error checking if \"" + playerUuid + "\" has an owner!", ex );
		}
	}

	/**
	 * Gets the serverId for the playerUuid.
	 *
	 * @param playerUuid The uuid of the player.
	 * @return the serverId of where the player is.
	 * @throws ProcessingException if an exception occurs.
	 */
	public String getServer( @NonNull Jedis jedis, @NonNull UUID playerUuid ) throws ProcessingException {
		try {
			return jedis.hget( RedisKeys.WORLD_REPO.toString(), playerUuid.toString() );
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error getting the owner of \"" + playerUuid + "\"!", ex );
		}
	}

	/**
	 * Set the serverId of the playerUuid.
	 *
	 * @param playerUuid The uuid of the player.
	 * @param serverId   The serverId that has the player.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void setServer( @NonNull Jedis jedis, @NonNull UUID playerUuid, String serverId ) throws ProcessingException {
		try {

			if ( serverId != null ) {
				jedis.hset( RedisKeys.WORLD_REPO.toString(), playerUuid.toString(), serverId );
			} else {
				jedis.hdel( RedisKeys.WORLD_REPO.toString(), playerUuid.toString() );
			}

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error setting the owner of \"" + playerUuid + "\" as server \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Remove all the players loaded on serverId. Used when a server shuts down.
	 *
	 * @param serverId The id of the server to remove.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void removeAll( @NonNull Jedis jedis, @NonNull String serverId ) throws ProcessingException {
		try {

			jedis.hgetAll( RedisKeys.WORLD_REPO.toString() ).entrySet()
					.stream()
					.filter( row -> row.getValue().equals( serverId ) )
					.forEach( row -> jedis.hdel( RedisKeys.WORLD_REPO.toString(), row.getKey() ) );

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error removing server \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Get the total size.
	 */
	public long getSize( @NonNull Jedis jedis ) {
		try {
			return jedis.hlen( RedisKeys.WORLD_REPO.toString() );
		} catch ( Exception ex ) {
			return -1;
		}
	}

}
