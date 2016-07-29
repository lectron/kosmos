/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.debugger;

import com.minecraftly.core.MinecraftlyCore;
import lombok.Getter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class DebuggerEngine<P> {

	private final MinecraftlyCore<P> core;

	@Getter
	private final ScriptEngineManager manager;

	public DebuggerEngine( MinecraftlyCore<P> core ) {

		this.core = core;

		this.manager = new ScriptEngineManager();
		this.manager.put( "core", core );

	}

	public void put( String key, Object value ) {
		this.manager.put( key, value );
	}

	public ScriptEngine getEngine() {
		return setupEngine( getManager().getEngineByName( "JavaScript" ) );
	}

	public ScriptEngine setupEngine( ScriptEngine engine ) {

		try {
			this.manager.put( "plugin", core.getOriginObject() );
			engine.eval( "load('nashorn:mozilla_compat.js');" );
			engine.eval( "importPackage( 'com.minecraftly.core' );" );
		} catch ( ScriptException e ) {
			core.getLogger().log( Level.WARNING, "Error setting up the debug script engine!", e );
		}

		return engine;

	}

}
