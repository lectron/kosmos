/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.logging.*;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class MinecraftlyLogger extends Logger {

	private final MinecraftlyCore core;

	/**
	 * debugging
	 */
	@Getter
	private boolean debug = false;

	/**
	 * Method to construct a logger for Minecraftly's Core.
	 *
	 * @param core         Minecraftly instance this shall belong to.
	 * @param parentLogger The parent logger.
	 * @throws MissingResourceException if the resourceBundleName is non-null and
	 *                                  no corresponding resource can be found.
	 */
	public MinecraftlyLogger( MinecraftlyCore core, Logger parentLogger ) throws MissingResourceException {
		super( "Core " + parentLogger.getName(), parentLogger.getResourceBundleName() );
		this.core = core;
		this.debug = new File( core.getMinecraftlyDataFolder(), ".debugging" ).exists();
		setParent( parentLogger );
		setUseParentHandlers( true );
	}

	/**
	 * Toggle on and off debugging.
	 *
	 * @param debug the state.
	 * @return true if the setting was applied successfully.
	 */
	public boolean setDebug( boolean debug ) {
		this.debug = debug;

		File debugFile = new File( core.getMinecraftlyDataFolder(), ".debugging" );
		if ( debug ) {
			try {
				return debugFile.createNewFile();
			} catch ( IOException e ) {
				return false;
			}
		} else {
			return debugFile.delete();
		}
	}

	@Override
	public boolean isLoggable( Level level ) {
		return isDebug() || super.isLoggable( level );
	}

	/**
	 * Make debug messages print to info.
	 *
	 * @param record the record to log.
	 */
	@Override
	public void log( LogRecord record ) {

		if ( isDebug() && record.getLevel().intValue() < 800 ) {
			record.setMessage( "[D|" + record.getLevel() + "] " + record.getMessage() );
			record.setLevel( Level.INFO );
		}

		if ( !isLoggable( record.getLevel() ) ) {
			return;
		}

		Filter theFilter = getFilter();
		if ( theFilter != null && !theFilter.isLoggable( record ) ) {
			return;
		}

		Logger logger = this;
		while ( logger != null ) {

			for ( Handler handler : logger.getHandlers() ) {
				handler.publish( record );
			}


			if ( !logger.getUseParentHandlers() ) {
				break;
			}

			logger = logger.getParent();

		}

	}

}
