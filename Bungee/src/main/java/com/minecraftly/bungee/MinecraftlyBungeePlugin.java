/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee;

import com.minecraftly.bungee.connection.ReconnectionHandler;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MinecraftlyBungeePlugin extends Plugin {

	private MinecraftlyBungeeCore core;

	@Override
	public void onLoad() {
		this.core = new MinecraftlyBungeeCore( this );
	}

	@Override
	public void onEnable() {

		getProxy().setReconnectHandler( new ReconnectionHandler( core ) );

		try {
			this.core.load();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		try {
			this.core.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}
