/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RedisConfiguration {

	/**
	 * The IP/Hostname to connect to.
	 */
	private String ip = null;

	/**
	 * The port to connect to.
	 */
	private int port = -1;

	/**
	 * The password for redis, null for none.
	 */
	private String password = null;

	/**
	 * The timeout for redis.
	 */
	private int timeOut = -1;

	/**
	 * The max redis cons for the pool.
	 */
	private int maxNumPools = -1;

}
