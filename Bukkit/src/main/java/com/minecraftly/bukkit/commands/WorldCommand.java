/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.MinecraftlyUtil;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class WorldCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only players can change worlds." );
			return true;
		}

		if( args.length != 1 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			sender.sendMessage( ChatColor.YELLOW + " /world <worldname>" );
			return true;
		}

		UUID uuidToJoin = MinecraftlyUtil.convertFromNoDashes( args[0] );

		World world = Bukkit.getWorld( uuidToJoin.toString() );

		if( world != null ) {
			((Player) sender).teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND );
			return true;
		}

		sender.sendMessage( ChatColor.RED + "We were unable to find a world by that name." );

		return true;

	}

}
