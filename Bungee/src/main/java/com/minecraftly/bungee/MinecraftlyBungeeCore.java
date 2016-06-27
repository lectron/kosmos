/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee;

import com.minecraftly.bungee.event.MinecraftlyEvent;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.configuration.MinecraftlyConfiguration;
import com.minecraftly.core.event.MCLYEvent;
import com.minecraftly.core.runnables.RunnableData;
import lombok.NonNull;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.Collection;
import java.util.logging.Level;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MinecraftlyBungeeCore extends MinecraftlyCore<MinecraftlyBungeePlugin> {

	public MinecraftlyBungeeCore( @NonNull MinecraftlyBungeePlugin originObject ) {
		super( originObject.getLogger(), originObject.getDataFolder(), originObject, 0 );
	}

	public MinecraftlyBungeeCore( MinecraftlyBungeePlugin originObject, @NonNull MinecraftlyConfiguration config ) {
		super( originObject.getLogger(), originObject.getDataFolder(), originObject, 0, config );
	}

	@Override
	public void shutdown() {
		// TODO
	}

	@Override
	public <T extends MCLYEvent> T callEvent( T event ) {
		return getOriginObject().getProxy().getPluginManager().callEvent( new MinecraftlyEvent<>( event ) ).getEvent();
	}

	@Override
	public int getMaxPlayers() {
		return getOriginObject().getProxy().getConfig().getPlayerLimit();
	}

	@Override
	public int getPlayerCount() {
		return getOriginObject().getProxy().getOnlineCount();
	}

	@Override
	public void runTask( RunnableData runnable ) {

		runnable.check();

		TaskScheduler scheduler = getOriginObject().getProxy().getScheduler();
		Collection<RunnableData.TaskType> types = runnable.getTaskTypes();

		if ( types.contains( RunnableData.TaskType.DELAYED ) ) {
			scheduler.schedule( getOriginObject(), runnable, runnable.getStartTime(), runnable.getTimeUnit() );
		} else if ( types.contains( RunnableData.TaskType.REPEATING ) ) {
			scheduler.schedule( getOriginObject(), runnable, runnable.getStartTime(), runnable.getRepeatTime(), runnable.getTimeUnit() );
		} else if ( runnable.getTaskTypes().size() == 1 && (types.contains( RunnableData.TaskType.ASYNC ) || types.contains( RunnableData.TaskType.SYNC )) ) {
			scheduler.runAsync( getOriginObject(), runnable );
		} else {
			getLogger().log( Level.WARNING, "There was an error scheduling task of class \"" + runnable.getClass() + "\"." );
		}

	}

}
