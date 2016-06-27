/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.global.inventories;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.util.Callback;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 * @deprecated use symlinks.
 */
@RequiredArgsConstructor
@Deprecated
public class InventoryHandler {

	private final MinecraftlyBukkitCore core;

	// TODO use redis here.

	public YamlConfiguration serializeInventory( @NonNull Player player, @NonNull World world ) {

		YamlConfiguration configuration = new YamlConfiguration();

		// Player inventory
		PlayerInventory inventory = player.getInventory();
		int inventorySize = inventory.getSize() + inventory.getArmorContents().length + inventory.getExtraContents().length;
		ConfigurationSection playerInventorySection = getOrCreateSection( configuration, "inventory" );

		for ( int i = 0; i < inventorySize; i++ ) {
			ItemStack itemStack = inventory.getItem( i );
			playerInventorySection.set( String.valueOf( i ), itemStack );
		}

		// Enderchest.
		Inventory enderInventory = player.getEnderChest();
		int enderInventorySize = enderInventory.getSize();
		ConfigurationSection enderchestSection = getOrCreateSection( configuration, "enderchest" );
		for ( int i = 0; i < enderInventorySize; i++ ) {
			ItemStack itemStack = enderInventory.getItem( i );
			enderchestSection.set( String.valueOf( i ), itemStack );
		}

		return configuration;

	}

	public void saveInventory( @NonNull YamlConfiguration configuration, @NonNull File file ) throws IOException {
		configuration.save( file );
	}

	public YamlConfiguration loadInventory( @NonNull File file ) {
		return YamlConfiguration.loadConfiguration( file );
	}

	public void deserializeInventory( @NonNull Player player, @NonNull YamlConfiguration configuration ) {

		if ( configuration.contains( "playerInventory" ) ) {
			PlayerInventory playerInventory = player.getInventory();
			ConfigurationSection playerInventorySection = configuration.getConfigurationSection( "playerInventory" );
			for ( int i = 0; i < playerInventory.getSize() + playerInventory.getArmorContents().length; i++ ) { // +4 for armor
				String intString = String.valueOf( i );
				ItemStack itemStack = playerInventorySection.contains( intString ) ? playerInventorySection.getItemStack( intString ) : null;
				playerInventory.setItem( i, itemStack );
			}
		}

		if ( configuration.contains( "enderInventory" ) ) {
			Inventory enderInventory = player.getEnderChest();
			ConfigurationSection enderInventorySection = configuration.getConfigurationSection( "enderInventory" );
			for ( int i = 0; i < enderInventory.getSize(); i++ ) {
				String intString = String.valueOf( i );
				ItemStack itemStack = enderInventorySection.contains( intString ) ? enderInventorySection.getItemStack( intString ) : null;
				enderInventory.setItem( i, itemStack );
			}
		}

	}

	public void doPlayerLoad( final @NonNull Player player ) {

		try {

			Callback<Void, String> sendMessage = param -> {
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), () -> {
					player.sendMessage( param );
					return null;
				} );
				return null;
			};

			Callback<Void, YamlConfiguration> deserializeInventory = param -> {
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), () -> {
					deserializeInventory( player, param );
					return null;
				} );
				return null;
			};

			final File playerFile = getDataFolder( player.getWorld(), player.getUniqueId() );


			core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
				try {
					deserializeInventory.call( YamlConfiguration.loadConfiguration( playerFile ) );
				} catch ( Exception ex ) {
					core.getLogger().log( Level.WARNING, "Unable to load (" + player.getUniqueId() + "|" + player.getName() + ")'s inventory!" );
					sendMessage.call( ChatColor.YELLOW + "We were unable to load your inventory! Reason: " + ex.getMessage() );
				}
			} );

		} catch ( Exception ex ) {
			core.getLogger().log( Level.WARNING, "Unable to load (" + player.getUniqueId() + "|" + player.getName() + ")'s inventory!" );
			player.sendMessage( ChatColor.YELLOW + "We were unable to load your inventory!" );
			player.sendMessage( ChatColor.YELLOW + "  Reason: " + ex.getMessage() );
		}

	}

	public void doPlayerUnload( @NonNull Player player ) {

		try {

			Callback<Void, String> sendMessage = param -> {
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), () -> {
					player.sendMessage( param );
					return null;
				} );
				return null;
			};

			YamlConfiguration configuration = serializeInventory( player, player.getWorld() );
			final File playerFile = getDataFolder( player.getWorld(), player.getUniqueId() );

			core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
				try {
					configuration.save( playerFile );
				} catch ( IOException ex ) {
					core.getLogger().log( Level.WARNING, "Unable to save (" + player.getUniqueId() + "|" + player.getName() + ")'s inventory!" );
					sendMessage.call( ChatColor.YELLOW + "We were unable to load your inventory! Reason: " + ex.getMessage() );
				}
			} );

		} catch ( Exception ex ) {
			core.getLogger().log( Level.WARNING, "Unable to load (" + player.getUniqueId() + "|" + player.getName() + ")'s inventory!" );
			player.sendMessage( ChatColor.YELLOW + "We were unable to load your inventory!" );
			player.sendMessage( ChatColor.YELLOW + "  Reason: " + ex.getMessage() );
		}

	}

	public File getDataFolder( @NonNull World world ) {

		/*
		 * For per world inventories use the below line?
		 * File file = new File( WorldDimension.getBaseWorld( world ).getWorldFolder(), "minecraftly_inventories" );
		 */
		File file = new File( Bukkit.getWorldContainer(), "minecraftly_inventories" );
		if ( !file.exists() ) file.mkdirs();

		return file;

	}

	public File getDataFolder( @NonNull World world, @NonNull UUID playerUuid ) {
		return new File( getDataFolder( world ), playerUuid.toString() + ".yml" );
	}

	public ConfigurationSection getOrCreateSection( @NonNull YamlConfiguration configuration, @NonNull String name ) {
		return configuration.contains( name ) ? configuration.getConfigurationSection( name ) : configuration.createSection( name );
	}
}
