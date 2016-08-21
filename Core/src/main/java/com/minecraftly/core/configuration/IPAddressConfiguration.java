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
import lombok.RequiredArgsConstructor;

/**
 * The IP Address of the machine, defaults to port 25565.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class IPAddressConfiguration {

	/**
	 * The IP address of the server
	 */
	private String ipAddress = null;

	/**
	 * The port of the server.
	 */
	private int port = -1;

}
