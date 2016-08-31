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

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.worlddata.WorldData;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class KickCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only players can do this.." );
			return true;
		}

		if ( args.length != 1 ) {
			sender.sendMessage( ChatColor.RED + "That's not how you use this command..." );
			sender.sendMessage( ChatColor.YELLOW + "  /kick [player]" );
			return true;
		}

		Player player = ((Player) sender);

		String compare = args[0];
		if ( compare.length() > 16 ) {
			try {
				compare = UUID.fromString( compare ).toString();
			} catch ( IllegalArgumentException ex ) {
				player.sendMessage( ChatColor.YELLOW + "The player \"" + args[0] + "\" could not be found.." );
				return true;
			}
		}

		for ( Player kicker : WorldDimension.getPlayersAllDimensions( player.getWorld() ) ) {

			if ( kicker.getName().equalsIgnoreCase( compare ) || kicker.getUniqueId().toString().equalsIgnoreCase( compare ) ) {
				kicker.kickPlayer( ChatColor.YELLOW + "You have been kicked!" );
				sender.sendMessage( ChatColor.GREEN + kicker.getName() + " has been kicked." );
				return true;
			}

		}

		return true;

	}

	public TextComponent generatePlayerComponent( Player player ) {

		UUID uuid = player.getUniqueId();
		UUID worldUuid = WorldDimension.getUUIDOfWorld( player.getWorld() );
		WorldData worldData = core.getPlayerHandler().getWorldData( worldUuid );

		TextComponent tc = new TextComponent( player.getName() );
		tc.setColor( ChatColor.GOLD );

		List<String> hoverLines = new ArrayList<>();

		hoverLines.add( colour( "&bUUID: &9" ) );
		hoverLines.add( colour( "&9 " + player.getUniqueId() ) );
		hoverLines.add( colour( "&bWorld: &9" + getNiceWorldName( player ) ) );

		if ( worldData != null ) {

			String trusted = worldData.getTrustedUsers().contains( uuid ) ? "&aYes" : "&eNo";
			hoverLines.add( colour( "&bTrusted: " + trusted ) );

			String whiteListed = worldData.getWhiteListedUsers().contains( uuid ) ? "&aYes" : "&eNo";
			hoverLines.add( colour( "&bWhite listed: " + whiteListed ) );

			String muted = worldData.getMutedUsers().containsKey( uuid ) ? "&cYes" : "&eNo";
			hoverLines.add( colour( "&bMuted: " + muted ) );

		}

		TextComponent[] hoverComponents = hoverLines.stream().map( s -> s + "\n" ).map( TextComponent::new ).toArray( TextComponent[]::new );
		tc.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, hoverComponents ) );

		// TODO click event message?

		return tc;

	}

	private String getNiceWorldName( Player player ) {

		WorldDimension wd = WorldDimension.fromEnvironment( player.getWorld().getEnvironment() );
		return wd == null ? "The Overworld" : wd.getNiceName();

	}

	private String colour( String string ) {
		return ChatColor.translateAlternateColorCodes( '&', string );
	}

}
