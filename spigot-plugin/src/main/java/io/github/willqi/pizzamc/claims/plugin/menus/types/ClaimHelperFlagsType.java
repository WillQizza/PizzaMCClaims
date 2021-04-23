package io.github.willqi.pizzamc.claims.plugin.menus.types;


import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ClaimHelperFlagsType implements MenuType {

    public static final String ID = "claim_helper_flags";

    private final ClaimsPlugin plugin;
    private final Map<UUID, Map<String, Object>> openInventories;

    public ClaimHelperFlagsType(ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        UUID uuid = (UUID)params.get("uuid");
        Claim claim = (Claim)params.get("claim");
        ClaimHelper helper = this.getClaimHelper(claim, uuid);

        Inventory flagsInventory = Bukkit.createInventory(player, 9, "Claim Helper - Flags");
        flagsInventory.setItem(0, getHeadItemStack(uuid));
        flagsInventory.setItem(2, getPermissionItemStack(ClaimHelper.Permission.BUILD, new ItemStack(Material.GRASS, 1), helper.hasPermission(ClaimHelper.Permission.BUILD)));
        flagsInventory.setItem(3, getPermissionItemStack(ClaimHelper.Permission.INTERACT, new ItemStack(Material.WOOD_DOOR, 1), helper.hasPermission(ClaimHelper.Permission.INTERACT)));

        player.openInventory(flagsInventory);
        this.openInventories.put(player.getUniqueId(), params);
    }

    @Override
    public void onClose(Player player) {
        player.closeInventory();
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            Player player = (Player) event.getInventory().getHolder();
            if (this.openInventories.containsKey(player.getUniqueId()) && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                Claim claim = (Claim)this.openInventories.get(player.getUniqueId()).get("claim");
                UUID uuid = (UUID)this.openInventories.get(player.getUniqueId()).get("uuid");
                ClaimHelper helper = this.getClaimHelper(claim, uuid);

                NBTTagCompound tag = Utility.getNMSTag(event.getCurrentItem());
                if (tag.hasKey("permission")) {
                    ClaimHelper.Permission permission = ClaimHelper.Permission.getPermisionByValue(tag.getInt("permission"));
                    if (helper.hasPermission(permission)) {
                        helper.removePermission(permission);
                    } else {
                        helper.addPermission(permission);
                    }
                    this.plugin.getClaimsManager().saveClaimHelper(claim.getCoordinates(), helper).whenComplete((v, exception) -> {
                        if (exception != null) {
                            this.plugin.getLogger().log(Level.SEVERE, "Failed to save claim helper", exception);
                            player.sendMessage(Utility.formatResponse("Claims", "An exception has occurred while trying to save the claim helper.", ChatColor.RED));
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                            this.plugin.getMenuManager().showMenu(player, ID, this.openInventories.get(player.getUniqueId()));
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onAnotherInventoryOpen(InventoryOpenEvent event) {
        // In case another plugin opens the inventory when we already have our inventory open
        this.openInventories.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.openInventories.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.openInventories.remove(event.getPlayer().getUniqueId());
    }


    private ClaimHelper getClaimHelper(Claim claim, UUID uuid) {
        return this.plugin.getClaimsManager().getClaimHelper(claim.getCoordinates(), uuid).orElseGet(() -> new ClaimHelper(uuid, 0));
    }

    private static ItemStack getHeadItemStack(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta)skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.YELLOW + player.getName());
        skull.setItemMeta(meta);
        return skull;
    }

    private static ItemStack getPermissionItemStack(ClaimHelper.Permission permission, ItemStack item, boolean enabled) {
        ItemStack permissionItem = new ItemStack(item);
        ItemMeta meta = permissionItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        if (enabled) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.setDisplayName("" + ChatColor.RESET + ChatColor.GREEN + permission.getTitle());
            meta.setLore(Arrays.asList(permission.getDescription(), "", ChatColor.GREEN + "Enabled"));
        } else {
            meta.setDisplayName("" + ChatColor.RESET + ChatColor.RED + permission.getTitle());
            meta.setLore(Arrays.asList(permission.getDescription(), "", ChatColor.RED + "Disabled"));
        }
        permissionItem.setItemMeta(meta);

        NBTTagCompound tag = io.github.willqi.pizzamc.claims.plugin.Utility.getNMSTag(permissionItem);
        tag.setInt("permission", permission.getValue());
        permissionItem = Utility.applyNMSTag(permissionItem, tag);

        return permissionItem;
    }

}
