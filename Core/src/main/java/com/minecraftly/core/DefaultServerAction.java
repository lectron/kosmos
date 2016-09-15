/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public enum DefaultServerAction {

	/**
	 * Create the world, even if it's not theirs.
	 *
	 * @deprecated This may potentially allow for lots and lots of worlds to be created within a short amount of time.
	 */
	@Deprecated
	CREATE,

	/**
	 * If it's not their own, it will go to their own world.
	 */
	OWN,

	/**
	 * If a lobby server is specified, go to it.
	 */
	LOBBY

}
