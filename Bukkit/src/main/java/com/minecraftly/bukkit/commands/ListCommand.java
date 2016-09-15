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
import net.md_5.bungee.api.chat.BaseComponent;
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
public class ListCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only players can do this.." );
			return true;
		}

		Player player = ((Player) sender);

		TextComponent title = new TextComponent( "Currently online in your world:\n" );
		title.setBold( true );
		title.setColor( ChatColor.BLUE );

		TextComponent playerTextComponent = new TextComponent( "" );

		WorldDimension.getPlayersAllDimensions( player.getWorld() )
				.stream()
				.map( this::generatePlayerComponent )
				.forEach( textComponent -> {
					playerTextComponent.addExtra( textComponent );
					playerTextComponent.addExtra( ChatColor.DARK_BLUE + ", " );
				} );

		// Remove the last comma.
		List<BaseComponent> extraComponents = playerTextComponent.getExtra();
		if ( extraComponents != null && !extraComponents.isEmpty() ) {
			extraComponents.remove( extraComponents.size() - 1 );
		}

		player.sendMessage( new TextComponent[]{ title, playerTextComponent } );

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
