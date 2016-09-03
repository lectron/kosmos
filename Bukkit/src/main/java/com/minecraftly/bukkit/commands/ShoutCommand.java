/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.google.common.base.Joiner;
import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.RedisKeys;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import redis.clients.jedis.Jedis;

import java.util.HashSet;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class ShoutCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;
	private final Joiner joiner = Joiner.on( " " );

	public static void doChat( Jedis jedis, MinecraftlyBukkitCore core, Player player, String chat, String permission ) {

		AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent( true, player, chat, new MinecraftlySet() );
		core.getOriginObject().getServer().getPluginManager().callEvent( chatEvent );

		permission = permission != null ? "\000" + permission : "";
		chat = String.format( chatEvent.getFormat(), chatEvent.getPlayer().getDisplayName(), chatEvent.getMessage() );
		jedis.publish( RedisKeys.CHAT.toString(), "MSG\000" + chat + permission );

	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only players can change worlds." );
			return true;
		}

		if ( args.length == 0 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			sender.sendMessage( ChatColor.YELLOW + " /y <chat message>" );
			return true;
		}

		core.getChatHandler().queueMessage( joiner.join( args ), ((Player) sender).getUniqueId() );
		return true;

	}

	public static class MinecraftlySet extends HashSet<Player> {
	}

}
