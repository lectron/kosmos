/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.event.events;

import com.minecraftly.core.event.MCLYEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Called when a message is received by the redis pubsub.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
@Data
@EqualsAndHashCode( callSuper = true )
public class MessageEvent extends MCLYEvent {

	/**
	 * The channel of which the message was sent.
	 */
	public final String channel;

	/**
	 * The message that was sent.
	 */
	public final String message;

}
