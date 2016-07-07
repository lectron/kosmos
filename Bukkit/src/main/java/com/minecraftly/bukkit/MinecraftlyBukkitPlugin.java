/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit;

import com.minecraftly.bukkit.commands.DebugCommand;
import com.minecraftly.bukkit.listeners.PlayerListener;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MinecraftlyBukkitPlugin extends JavaPlugin {

	MinecraftlyBukkitCore core;

	@Override
	public void onLoad() {
		core = new MinecraftlyBukkitCore( this );
	}

	@Override
	public void onEnable() {

		try {
			core.load();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		try ( Jedis jedis = core.getJedis() ) {
			int playerCount = getServer().getOnlinePlayers().size();
			core.getServerManager().setServer( jedis, core.identify(), playerCount );
			core.getLogger().log( Level.INFO, "Online players set to " + playerCount );
		} catch ( ProcessingException | NoJedisException e ) {
			e.printStackTrace();
		}

		getServer().getPluginManager().registerEvents( new PlayerListener( core ), this );
		getServer().getPluginManager().registerEvents( core.getPlayerHandler(), this );

		getCommand( "mdebug" ).setExecutor( new DebugCommand( core ) );

	}

	@Override
	public void onDisable() {
		try {
			core.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}
