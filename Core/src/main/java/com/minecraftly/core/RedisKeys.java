/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public enum RedisKeys {

	/**
	 * IDENTIFY key is used to identify and communicate between servers.
	 */
	IDENTIFY,

	/**
	 * SERVER_REPO is a repository of servers, with their ID's and their addresses.
	 */
	SERVER_REPO,

	/**
	 * WORLD_REPO is a repository of worlds, with their owner's UUID and the server ID that they're loaded on (if any).
	 */
	WORLD_REPO,

	/**
	 * UUID_REPO is a repository of username-uuid links. Mostly used for the domain name.
	 */
	UUID_REPO,

	/**
	 * PLAYER_REPO is a repository or UUID-ServerID links, to track players across the network.
	 */
	PLAYER_REPO;

	private static Collection<String> valueList;

	public static Collection<String> valuesAsStringCollection() {
		return valueList != null ? valueList :
				(valueList = Arrays.asList( RedisKeys.values() ).stream().map( Object::toString ).collect( Collectors.toList() ));
	}

	/**
	 * We prepend <pre>minecraftly:</pre> to the key for redis, to ensure we're not breaking anything else.
	 *
	 * @return The name of the key prepended with <pre>minecraftly:</pre>
	 */
	@Override
	public String toString() {
		return ("minecraftly:" + super.toString()).toLowerCase();
	}


}
