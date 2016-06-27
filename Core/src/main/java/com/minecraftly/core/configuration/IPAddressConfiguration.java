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
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class IPAddressConfiguration {

	private final String ipAddress;
	private int port = 25565;

}
