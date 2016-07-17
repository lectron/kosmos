/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit;

import com.minecraftly.bukkit.commands.*;
import com.minecraftly.bukkit.listeners.DebugListener;
import com.minecraftly.bukkit.listeners.PlayerListener;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.NonNull;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
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

		getServer().getMessenger().registerIncomingPluginChannel( this, "KMCLY", new DebugListener( core ) );

		getServer().getPluginManager().registerEvents( new PlayerListener( core ), this );
		getServer().getPluginManager().registerEvents( core.getPlayerHandler(), this );
		core.getPlayerHandler().load();

		setupCommands();

	}

	private void setupCommands() {

		// Debug command.
		setExecutors( "mdebug", new DebugCommand( core ) );

		// Whitelist command.
		setExecutors( "whitelist", new WhiteListCommands( core ) );

		// Home commands.
		HomeCommands homeCommands = new HomeCommands( core );
		setExecutors( "home", homeCommands );
		setExecutors( "delhome", homeCommands );
		setExecutors( "sethome", homeCommands );

		// Spawn commands.
		SpawnCommands spawnCommands = new SpawnCommands( core );
		setExecutors( "spawn", spawnCommands );
		setExecutors( "setspawn", spawnCommands );

		// Ban commands.
		BanCommands banCommands = new BanCommands( core );
		setExecutors( "ban", banCommands );
		setExecutors( "unban", banCommands );

		// Mute commands.
		MuteCommands muteCommands = new MuteCommands( core );
		setExecutors( "mute", muteCommands );
		setExecutors( "unmute", muteCommands );
		setExecutors( "muted", muteCommands );

		// Trust commands.
		TrustCommands trustCommands = new TrustCommands( core );
		setExecutors( "trust", trustCommands );
		setExecutors( "untrust", trustCommands );
		setExecutors( "trusted", trustCommands );

		// List
		setExecutors( "list", new ListCommand( core ) );

		// More??

	}

	private boolean setExecutors( @NonNull String commandName, @NonNull Object executor ) {

		PluginCommand command = getCommand( commandName );
		if( command == null ) return false;

		boolean ret = false;

		if( executor instanceof CommandExecutor ) {
			command.setExecutor( (CommandExecutor) executor );
			ret = true;
		}

		if( executor instanceof TabCompleter ) {
			command.setTabCompleter( (TabCompleter) executor );
			ret = true;
		}

		core.getLogger().log( Level.FINE, "Registered command \"" + commandName + "\" with executor \"" + executor.getClass() + "\". Was successful: " + ret  );

		return ret;

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
