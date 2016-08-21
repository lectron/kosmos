/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.runnables;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A task for future use.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Deprecated
@RequiredArgsConstructor
public class CloseTask extends RunnableData {

	private final Callable<Void> callable;

	@Override
	public TimeUnit getTimeUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	public long getRepeatTime() {
		return 0;
	}

	@Override
	public long getStartTime() {
		return 30;
	}

	@Override
	public Collection<TaskType> getTaskTypes() {
		return Arrays.asList( TaskType.SYNC );
	}

	@Override
	public void run() {

		try {
			if ( callable != null ) callable.call();
		} catch ( Exception e ) {
			e.printStackTrace();
			// Shouldn't happen?
		}

	}

}
