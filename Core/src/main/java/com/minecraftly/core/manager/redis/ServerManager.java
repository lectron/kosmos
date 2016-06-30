/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.manager.redis;

import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.NoServerException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import redis.clients.jedis.Jedis;

import java.util.Iterator;

/**
 * A class to repository servers and their player count.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@AllArgsConstructor
public class ServerManager {

	@NonNull
	private final MinecraftlyCore core;

	/**
	 * Checks if the world of player (playerUuid) has a server..
	 *
	 * @param serverId The id of the server to search for.
	 * @return true if the server exists.
	 * @throws ProcessingException if an exception occurs.
	 * @deprecated do the check yourself, if {@link ServerManager#getServer(Jedis, String)} >= 0.
	 */
	@Deprecated
	public boolean hasServer( @NonNull Jedis jedis, @NonNull String serverId ) throws ProcessingException {
		return getServer( jedis, serverId ) >= 0;
	}

	/**
	 * Gets the json server address of serverId.
	 *
	 * @param serverId The uuid of the owner of the world.
	 * @return The json string of the address and port.
	 * @throws ProcessingException if an exception occurs.
	 */
	public int getServer( @NonNull Jedis jedis, @NonNull String serverId ) throws ProcessingException {
		try {
			Double players = jedis.zscore( RedisKeys.SERVER_REPO.toString(), serverId );
			if ( players == null ) return -1;
			return players.intValue();
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error getting the owner of \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Set the serverId of whom owns the world belonging to playerUuid.
	 *
	 * @param serverId    The serverId.
	 * @param playerCount The playercount of the server.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void setServer( @NonNull Jedis jedis, @NonNull String serverId, int playerCount ) throws ProcessingException {
		try {

			if ( playerCount >= 0 ) {
				jedis.zadd( RedisKeys.SERVER_REPO.toString(), playerCount, serverId );
			} else {
				jedis.zrem( RedisKeys.SERVER_REPO.toString(), serverId );
				jedis.publish( RedisKeys.IDENTIFY.toString(), "SERVER\000" + serverId + "\000REMOVED" );
			}

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error setting the playercount of : \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Incremement the player count for serverId by incrAmount.
	 * Use negative numbers to negate players.
	 * @param jedis A jedis instance to use.
	 * @param serverId The serverId of whom's player count you're increasing.
	 * @param incrAmount The amount you want to increase it by.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void incrServerPlayerCount( @NonNull Jedis jedis, @NonNull String serverId, int incrAmount ) throws ProcessingException {
		try {
			jedis.zincrby( RedisKeys.SERVER_REPO.toString(), incrAmount, serverId );
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error incrementing the player count : \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Fetches the server with the least players.
	 *
	 * @param jedis a jedis connection.
	 * @return The serverId with the least players.
	 * @throws ProcessingException occours when an exception is caught.
	 */
	public String fetchServer( @NonNull Jedis jedis ) throws ProcessingException {
		try {

			Iterator<String> lowestServers = jedis.zrangeByScore( RedisKeys.SERVER_REPO.toString(), 0, Double.POSITIVE_INFINITY ).iterator();
			if ( !lowestServers.hasNext() ) throw new NoServerException();

			return lowestServers.next();

		} catch ( Exception ex ) {
			throw new ProcessingException( "Unable to fetch a server?!", ex );
		}
	}

	/**
	 * Remove serverId. Used when a server shuts down.
	 *
	 * @param serverId The id of the server to remove.
	 * @throws ProcessingException if an exception occurs.
	 */
	public void removeAll( @NonNull Jedis jedis, @NonNull String serverId ) throws ProcessingException {
		try {

			jedis.zrem( RedisKeys.SERVER_REPO.toString(), serverId );
			jedis.publish( RedisKeys.IDENTIFY.toString(), "SERVER\000" + serverId + "\000REMOVED" );

		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error removing server \"" + serverId + "\"!", ex );
		}
	}

	/**
	 * Get the total size.
	 */
	public long getSize( @NonNull Jedis jedis ) {
		try {
			return jedis.zcard( RedisKeys.SERVER_REPO.toString() );
		} catch ( Exception ex ) {
			return -1;
		}
	}

}
