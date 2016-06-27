/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.event;

import com.minecraftly.core.event.MCLYEvent;
import com.minecraftly.core.event.WrappedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@RequiredArgsConstructor
public class MinecraftlyEvent<E extends MCLYEvent> extends Event implements WrappedEvent {

	@Getter
	private static HandlerList handlerList = new HandlerList();

	@Getter
	private final E event;

	public void postCall() {
		getEvent().postCall();
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

}