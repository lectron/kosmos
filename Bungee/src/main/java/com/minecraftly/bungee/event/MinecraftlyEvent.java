/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.event;

import com.minecraftly.core.event.MCLYEvent;
import com.minecraftly.core.event.WrappedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Event;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
public class MinecraftlyEvent<E extends MCLYEvent> extends Event implements WrappedEvent {

	@Getter
	private final E event;

	@Override
	public void postCall() {
		getEvent().postCall();
		super.postCall();
	}

}
