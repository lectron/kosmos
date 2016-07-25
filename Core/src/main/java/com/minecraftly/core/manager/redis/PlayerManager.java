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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
	public UUID getServer( @NonNull Jedis jedis, @NonNull UUID playerUuid ) throws ProcessingException {
		try {
			return UUID.fromString( jedis.hget( RedisKeys.WORLD_REPO.toString(), playerUuid.toString() ) );
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error getting the owner of \"" + playerUuid + "\"!", ex );
		}
	}

	/**
	 * Gets all the UUID's of the players on a specified world.
	 * @param jedis A jedis instance to use.
	 * @param world The uuid of the world you want information on.
	 * @return The list of UUID's on the server.
	 * @throws ProcessingException if an exception occurs.
	 */
	public List<UUID> getAllForServer( @NonNull Jedis jedis, @NonNull UUID world ) throws ProcessingException {
		try {

			return jedis.hgetAll( RedisKeys.WORLD_REPO.toString() ).entrySet()
					.stream()
					.filter( row -> row.getValue().equals( world.toString() ) )
					.map( row -> UUID.fromString( row.getValue() ) )
					.collect( Collectors.toList() );

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error getting the servers we own.", ex );
		}
	}

	/**
	 * Set the world of the playerUuid.
	 *
	 * @param playerUuid The uuid of the player.
	 * @param world The world that the player is in.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void setServer( @NonNull Jedis jedis, @NonNull UUID playerUuid, UUID world ) throws ProcessingException {
		try {

			if ( world != null ) {
				jedis.hset( RedisKeys.WORLD_REPO.toString(), playerUuid.toString(), world.toString() );
			} else {
				jedis.hdel( RedisKeys.WORLD_REPO.toString(), playerUuid.toString() );
			}

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error setting the owner of \"" + playerUuid + "\" as server \"" + world + "\"!", ex );
		}
	}

	/**
	 * Remove all the players on world. Used when a server shuts down.
	 *
	 * @param world The world UUID to remove.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void removeAll( @NonNull Jedis jedis, @NonNull UUID world ) throws ProcessingException {
		try {

			jedis.hgetAll( RedisKeys.WORLD_REPO.toString() ).entrySet()
					.stream()
					.filter( row -> row.getValue().equals( world.toString() ) )
					.forEach( row -> jedis.hdel( RedisKeys.WORLD_REPO.toString(), row.getKey() ) );

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error removing server \"" + world + "\"!", ex );
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
