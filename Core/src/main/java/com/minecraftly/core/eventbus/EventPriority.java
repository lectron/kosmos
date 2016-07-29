/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.eventbus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@NoArgsConstructor( access = AccessLevel.PRIVATE )
public class EventPriority {

	public static final byte LOWEST = -64;
	public static final byte LOW = -32;
	public static final byte NORMAL = 0;
	public static final byte HIGH = 32;
	public static final byte HIGHEST = 64;

}
