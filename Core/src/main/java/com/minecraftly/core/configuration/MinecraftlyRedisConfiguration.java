/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MinecraftlyRedisConfiguration {

	/**
	 * The IP/Hostname to connect to.
	 */
	private String ip = "127.0.0.1";

	/**
	 * The port to connect to.
	 */
	private int port = 6379;

	/**
	 * The password for redis, null for none.
	 */
	private String password = null;

	/**
	 * The timeout for redis.
	 */
	private int timeOut = 100;

	/**
	 * The max redis cons for the pool.
	 */
	private int maxNumPools = 6;

}
