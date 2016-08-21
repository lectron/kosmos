/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
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
	PLAYER_REPO,

	/**
	 * TRANSPORT is to be used strictly for messaging and sending users through the network.
	 */
	TRANSPORT;

	private static Collection<String> valueList;

	public static Collection<String> valuesAsStringCollection() {
		return valueList != null ? valueList :
				(valueList = Arrays.asList( RedisKeys.values() ).stream().map( Object::toString ).collect( Collectors.toList() ));
	}

	public static RedisKeys keyFromString( @NonNull String name ) {

		name = name.toUpperCase().replace( "MINECRAFTLY:", "" );

		for( RedisKeys key : values() ) {
			if ( key.name().toUpperCase().equals( name ) )
				return key;
		}

		return null;

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
