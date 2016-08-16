/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.event;

import com.minecraftly.core.eventbus.Event;
import lombok.Data;

import java.util.UUID;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Data
public class PlayerJoinEvent extends Event {

	private final Object originObject;
	private final UUID uuid;
	private final String name;
	private final UUID world;

}
