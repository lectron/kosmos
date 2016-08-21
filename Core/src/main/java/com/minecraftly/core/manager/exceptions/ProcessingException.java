/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.manager.exceptions;

/**
 * Occurs when processing of a redis query throws an exception.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class ProcessingException extends Exception {
	public ProcessingException( String s, Exception ex ) {
		super( s, ex );
	}
}
