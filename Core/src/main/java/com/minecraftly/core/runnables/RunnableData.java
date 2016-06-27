/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.runnables;

import com.minecraftly.core.MinecraftlyUtil;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A class that allows for cross platform support of scheduling tasks.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public abstract class RunnableData implements Runnable {

	/**
	 * @return The TimeUnit the instance uses.
	 */
	public abstract TimeUnit getTimeUnit();

	/**
	 * Potentially this is not used for non repeating tasks.
	 *
	 * @return The repeating time for the task.
	 */
	public abstract long getRepeatTime();

	/**
	 * @return The starting time of the task.
	 */
	public abstract long getStartTime();

	/**
	 * Get the repeat time in a 20th of a second.
	 *
	 * @return The repeat time in Minecraft ticks.
	 */
	public final long getRepeatTicks() {
		return MinecraftlyUtil.convertMillisToTicks( getRepeatTime() );
	}

	/**
	 * Get the start time in a 20th of a second.
	 *
	 * @return The repeat time in Minecraft ticks.
	 */
	public final long getStartTicks() {
		return MinecraftlyUtil.convertMillisToTicks( getStartTime() );
	}

	/**
	 * @return A collection of task types for this task.
	 */
	public abstract Collection<TaskType> getTaskTypes();

	/**
	 * Check the task has valid, non-conflicting types.
	 *
	 * @throws IllegalStateException
	 */
	public final void check() throws IllegalStateException {

		if ( getTaskTypes().size() < 1 ) {
			throw new IllegalStateException( "RunnableData (" + getClass().getName() + ") can not have no task types!" );
		}

		if ( getTaskTypes().contains( TaskType.ASYNC ) && getTaskTypes().contains( TaskType.SYNC ) ) {
			throw new IllegalStateException( "RunnableData (" + getClass().getName() + ") can not be sync and async!" );
		}

		if ( getTaskTypes().contains( TaskType.REPEATING ) && getTaskTypes().contains( TaskType.DELAYED ) ) {
			throw new IllegalStateException( "RunnableData (" + getClass().getName() + ") can not be delayed and repeating!" );
		}

		// TODO check times.

	}

	public enum TaskType {

		DELAYED,
		REPEATING,
		ASYNC,
		SYNC

	}

}
