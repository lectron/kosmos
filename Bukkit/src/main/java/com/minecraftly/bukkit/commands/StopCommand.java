/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class StopCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !sender.hasPermission( "bukkit.command.stop" ) ) {
			sender.sendMessage( ChatColor.RED + "You don't have permission to shut the server down.. -_-" );
			naughtyPlayer( sender );
			return true;
		}

		if ( args.length == 1 && args[0].equalsIgnoreCase( "now" ) ) {

			core.getOriginObject().getServer().shutdown();
			core.getLogger().log( Level.WARNING, "Shutdown NOW invoked by " + sender.getName() );
			sender.sendMessage( "Shutting down now!" );

		} else if ( args.length == 0 ) {

			core.getLogger().log( Level.WARNING, "Slow shutdown invoked by " + sender.getName() );
			sender.sendMessage( "Shutting down..." );

			try {
				core.close( false );
			} catch ( IOException e ) {
				core.getLogger().log( Level.SEVERE, "An error occurred with the slow shutdown, shutting down immediately.", e );
				core.getOriginObject().getServer().shutdown();
			}

		} else {

			sender.sendMessage( "Usage: /shutdown [now]" );

		}

		return true;

	}

	public void naughtyPlayer( CommandSender sender ) {

		if ( !(sender instanceof Player) ) return;

		Player player = ((Player) sender);

		player.getWorld().strikeLightningEffect( player.getLocation() );
		player.damage( 0.5 );
		player.teleport( player.getLocation().add( 0, 1, 0 ), PlayerTeleportEvent.TeleportCause.COMMAND );

	}

}
