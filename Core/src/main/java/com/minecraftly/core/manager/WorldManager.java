/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.manager;

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
 * A class to repository World UUID's and their servers.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@AllArgsConstructor
public class WorldManager {

	@NonNull
	private final MinecraftlyCore core;

	/**
	 * Checks if the world of player (playerUuid) has a server..
	 *
	 * @param playerUuid The uuid of the owner of the world.
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
	 * Gets the serverId for the world of playerUuid.
	 *
	 * @param playerUuid The uuid of the owner of the world.
	 * @return the serverId of whom owns the world.
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
	 * Set the serverId of whom owns the world belonging to playerUuid.
	 *
	 * @param playerUuid The uuid of the owner of the world.
	 * @param serverId   The serverId that has the world loaded.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void setServer( @NonNull Jedis jedis, @NonNull UUID playerUuid, String serverId ) throws ProcessingException {
		try {

			if ( serverId != null ) {
				jedis.hset( RedisKeys.WORLD_REPO.toString(), playerUuid.toString(), serverId );
			} else {
				jedis.hdel( RedisKeys.WORLD_REPO.toString(), playerUuid.toString() );
			}

			jedis.publish( RedisKeys.IDENTIFY.toString(), "WORLD\000" + playerUuid.toString() + "\000UPDATED" );

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error setting the owner of \"" + playerUuid + "\" as server \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Loads the world desired.
	 *
	 * @param jedis      A jedis instance to use.
	 * @param playerUuid THe UUID of the world to load.
	 * @return The serverId of which it will be loaded on.
	 * @throws ProcessingException if an exception occurs.
	 */
	public String loadWorld( @NonNull Jedis jedis, @NonNull UUID playerUuid ) throws ProcessingException {

		String serverId = null;
		try {

			serverId = core.getServerManager().fetchServer( jedis );
			jedis.publish( RedisKeys.WORLD_REPO.toString(), "WORLD\000LOAD\000" + serverId + "\000" + playerUuid.toString() );

			// TODO return a "hasLoaded" callback.
			return serverId;

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error loading the world of \"" + playerUuid + "\" on server \"" + serverId + "\"!", ex );
		}

	}

	/**
	 * Gets all the UUID's of the owners of the worlds loaded on a specified server.
	 *
	 * @param jedis    A jedis instance to use.
	 * @param serverId The id of the server you want information on.
	 * @return The list of UUID's loaded on the server.
	 * @throws ProcessingException if an exception occurs.
	 */
	public List<UUID> getAllForServer( @NonNull Jedis jedis, @NonNull String serverId ) throws ProcessingException {
		try {

			return jedis.hgetAll( RedisKeys.WORLD_REPO.toString() ).entrySet()
					.stream()
					.filter( row -> row.getValue().equals( serverId ) )
					.map( row -> UUID.fromString( row.getValue() ) )
					.collect( Collectors.toList() );

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error getting the servers we own.", ex );
		}
	}

	/**
	 * Remove all the worlds loaded on serverId. Used when a server shuts down.
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

			jedis.publish( RedisKeys.IDENTIFY.toString(), "WORLD\000SERVER\000" + serverId + "\000UPDATED" );

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
