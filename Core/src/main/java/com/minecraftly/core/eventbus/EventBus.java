/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class EventBus {

	private final Map<Class<? extends Event>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority = new HashMap<>();
	private final Map<Class<? extends Event>, EventHandlerMethod[]> byEventBaked = new ConcurrentHashMap<>();
	private final Lock lock = new ReentrantLock();
	private final Logger logger;

	public EventBus( Logger logger ) {
		this.logger = (logger == null) ? Logger.getLogger( Logger.GLOBAL_LOGGER_NAME ) : logger;
	}

	public void post( Event event ) {
		EventHandlerMethod[] handlers = byEventBaked.get( event.getClass() );

		if ( handlers != null ) {
			for ( EventHandlerMethod method : handlers ) {
				try {
					method.invoke( event );
				} catch ( IllegalAccessException ex ) {
					throw new Error( "Method became inaccessible: " + event, ex );
				} catch ( IllegalArgumentException ex ) {
					throw new Error( "Method rejected target/argument: " + event, ex );
				} catch ( InvocationTargetException ex ) {
					logger.log( Level.WARNING, MessageFormat.format( "Error dispatching event {0} to listener {1}", event, method.getListener() ), ex.getCause() );
				}
			}
		}
	}

	private Map<Class<? extends Event>, Map<Byte, Set<Method>>> findHandlers( Listener listener ) {
		Map<Class<? extends Event>, Map<Byte, Set<Method>>> handler = new HashMap<>();
		for ( Method m : listener.getClass().getDeclaredMethods() ) {
			EventHandler annotation = m.getAnnotation( EventHandler.class );
			if ( annotation != null ) {
				Class<?>[] params = m.getParameterTypes();
				if ( params.length != 1 || !Event.class.isAssignableFrom( params[0] ) ) {
					logger.log( Level.INFO, "Method {0} in class {1} annotated with {2} does not have single Event argument", new Object[]
							{
									m, listener.getClass(), annotation
							} );
					continue;
				}

				Class<? extends Event> eventClass = (Class<? extends Event>) params[0];

				Map<Byte, Set<Method>> prioritiesMap = handler.get( eventClass );
				if ( prioritiesMap == null ) {
					prioritiesMap = new HashMap<>();
					handler.put( eventClass, prioritiesMap );
				}
				Set<Method> priority = prioritiesMap.get( annotation.priority() );
				if ( priority == null ) {
					priority = new HashSet<>();
					prioritiesMap.put( annotation.priority(), priority );
				}
				priority.add( m );
			}
		}
		return handler;
	}

	public void register( Listener listener ) {
		Map<Class<? extends Event>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
		lock.lock();
		try {
			for ( Map.Entry<Class<? extends Event>, Map<Byte, Set<Method>>> e : handler.entrySet() ) {
				Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.get( e.getKey() );
				if ( prioritiesMap == null ) {
					prioritiesMap = new HashMap<>();
					byListenerAndPriority.put( e.getKey(), prioritiesMap );
				}
				for ( Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet() ) {
					Map<Object, Method[]> currentPriorityMap = prioritiesMap.get( entry.getKey() );
					if ( currentPriorityMap == null ) {
						currentPriorityMap = new HashMap<>();
						prioritiesMap.put( entry.getKey(), currentPriorityMap );
					}
					Method[] baked = new Method[entry.getValue().size()];
					currentPriorityMap.put( listener, entry.getValue().toArray( baked ) );
				}
				bakeHandlers( e.getKey() );
			}
		} finally {
			lock.unlock();
		}
	}

	public void unregister( Listener listener ) {
		Map<Class<? extends Event>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
		lock.lock();
		try {
			for ( Map.Entry<Class<? extends Event>, Map<Byte, Set<Method>>> e : handler.entrySet() ) {
				Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.get( e.getKey() );
				if ( prioritiesMap != null ) {
					for ( Byte priority : e.getValue().keySet() ) {
						Map<Object, Method[]> currentPriority = prioritiesMap.get( priority );
						if ( currentPriority != null ) {
							currentPriority.remove( listener );
							if ( currentPriority.isEmpty() ) {
								prioritiesMap.remove( priority );
							}
						}
					}
					if ( prioritiesMap.isEmpty() ) {
						byListenerAndPriority.remove( e.getKey() );
					}
				}
				bakeHandlers( e.getKey() );
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Shouldn't be called without first locking the writeLock; intended for use
	 * only inside {@link #register(Listener) register(Object)} or
	 * {@link #unregister(Listener) unregister(Object)}.
	 */
	private void bakeHandlers( Class<? extends Event> eventClass ) {
		Map<Byte, Map<Object, Method[]>> handlersByPriority = byListenerAndPriority.get( eventClass );
		if ( handlersByPriority != null ) {
			List<EventHandlerMethod> handlersList = new ArrayList<>( handlersByPriority.size() * 2 );

			// Either I'm really tired, or the only way we can iterate between Byte.MIN_VALUE and Byte.MAX_VALUE inclusively,
			// with only a byte on the stack is by using a do {} while() format loop.
			byte value = Byte.MIN_VALUE;
			do {
				Map<Object, Method[]> handlersByListener = handlersByPriority.get( value );
				if ( handlersByListener != null ) {
					for ( Map.Entry<Object, Method[]> listenerHandlers : handlersByListener.entrySet() ) {
						for ( Method method : listenerHandlers.getValue() ) {
							EventHandlerMethod ehm = new EventHandlerMethod( listenerHandlers.getKey(), method );
							handlersList.add( ehm );
						}
					}
				}
			} while ( value++ < Byte.MAX_VALUE );
			byEventBaked.put( eventClass, handlersList.toArray( new EventHandlerMethod[handlersList.size()] ) );
		} else {
			byEventBaked.remove( eventClass );
		}
	}
}
