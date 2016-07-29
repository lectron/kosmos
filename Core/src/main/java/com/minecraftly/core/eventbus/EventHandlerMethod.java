/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.eventbus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@AllArgsConstructor
public class EventHandlerMethod {

	@Getter
	private final Object listener;
	@Getter
	private final Method method;

	public void invoke( Object event ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.invoke( listener, event );
	}

}
