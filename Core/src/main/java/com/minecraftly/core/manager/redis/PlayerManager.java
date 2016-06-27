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
 * A class to repository players and their uuid's.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@AllArgsConstructor
public class PlayerManager {

	@NonNull
	private final MinecraftlyCore core;

	/**
	 * Checks if the world of player (playerUuid) has a server..
	 *
	 * @param playerName The name of the player to search for.
	 * @return true if the server exists.
	 * @throws ProcessingException if an exception occurs.
	 */
	public boolean hasUuid( @NonNull Jedis jedis, @NonNull String playerName ) throws ProcessingException {
		try {
			return jedis.hexists( RedisKeys.PLAYER_REPO.toString(), playerName );
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error checking if \"" + playerName + "\" has a UUID!", ex );
		}
	}

	/**
	 * Gets the uuid of playerName.
	 *
	 * @param playerName The uuid of the owner of the world.
	 * @return The {@link UUID} of the player.
	 * @throws ProcessingException if an exception occurs.
	 */
	public UUID getUuid( @NonNull Jedis jedis, @NonNull String playerName ) throws ProcessingException {
		try {
			return UUID.fromString( jedis.hget( RedisKeys.PLAYER_REPO.toString(), playerName.toLowerCase() ) );
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error getting the UUID of \"" + playerName + "\"!", ex );
		}
	}

	/**
	 * Set the uuid of playerName.
	 *
	 * @param playerName The playerName.
	 * @param uuid       The uuid of the server.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void setUuid( @NonNull Jedis jedis, @NonNull String playerName, UUID uuid ) throws ProcessingException {
		try {

			if ( uuid != null ) {
				jedis.hset( RedisKeys.PLAYER_REPO.toString(), playerName.toLowerCase(), uuid.toString() );
			} else {
				jedis.hdel( RedisKeys.PLAYER_REPO.toString(), playerName.toLowerCase() );
			}

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error setting the UUID of \"" + playerName + "\"!", ex );
		}
	}

	/**
	 * Get the total size.
	 */
	public long getSize( @NonNull Jedis jedis ) {
		try {
			return jedis.hlen( RedisKeys.PLAYER_REPO.toString() );
		} catch ( Exception ex ) {
			return -1;
		}
	}

}
