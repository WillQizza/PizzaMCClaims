package io.github.willqi.pizzamc.claims.plugin.menus.types;

import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;

import io.github.willqi.pizzamc.claims.plugin.Utility;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import java.util.*;
import java.util.logging.Level;

public class ClaimFlagsType implements MenuType {

    public static final String ID = "claim_flags_menu";

    private final ClaimsPlugin plugin;

    private final Map<UUID, Claim> openInventories;

    public ClaimFlagsType(ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        Claim claim = (Claim)params.get("claim");

        Inventory flagsInventory = Bukkit.createInventory(player, 18, "Flags");
        flagsInventory.setItem(4, getInformationItemStack());

        flagsInventory.setItem(12, getFlagItemStack(Claim.Flag.ALWAYS_DAY, new ItemStack(Material.DAYLIGHT_DETECTOR, 1), claim.hasFlag(Claim.Flag.ALWAYS_DAY)));
        flagsInventory.setItem(13, getFlagItemStack(Claim.Flag.DENY_PVP, new ItemStack(Material.DIAMOND_SWORD, 1), claim.hasFlag(Claim.Flag.DENY_PVP)));
        flagsInventory.setItem(14, getFlagItemStack(Claim.Flag.DISABLE_MOB_SPAWNING, new ItemStack(Material.BONE, 1), claim.hasFlag(Claim.Flag.DISABLE_MOB_SPAWNING)));

        player.openInventory(flagsInventory);
        this.openInventories.put(player.getUniqueId(), claim);
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
                Claim claim = this.openInventories.get(player.getUniqueId());

                NBTTagCompound tag = Utility.getNMSTag(event.getCurrentItem());
                if (tag.hasKey("flag")) {
                    Claim.Flag flag = Claim.Flag.getFlagByValue(tag.getInt("flag"));
                    if (claim.hasFlag(flag)) {
                        claim.removeFlag(flag);
                    } else {
                        claim.addFlag(flag);
                    }
                    this.plugin.getClaimsManager().saveClaim(claim).whenComplete((v, exception) -> {
                        if (exception != null) {
                            this.plugin.getLogger().log(Level.SEVERE, "Failed to save claim flags", exception);
                            player.sendMessage(Utility.formatResponse("Claims", "An exception has occurred while trying to save flags.", ChatColor.RED));
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                            Map<String, Object> params = new HashMap<>();
                            params.put("claim", claim);
                            this.plugin.getMenuManager().showMenu(player, ID, params);
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


    private static ItemStack getFlagItemStack(Claim.Flag flag, ItemStack item, boolean enabled) {
        ItemStack flagItem = new ItemStack(item);
        ItemMeta meta = flagItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        if (enabled) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.setDisplayName("" + ChatColor.RESET + ChatColor.GREEN + flag.getTitle());
            meta.setLore(Arrays.asList(flag.getDescription(), "", ChatColor.GREEN + "Enabled"));
        } else {
            meta.setDisplayName("" + ChatColor.RESET + ChatColor.RED + flag.getTitle());
            meta.setLore(Arrays.asList(flag.getDescription(), "", ChatColor.RED + "Disabled"));
        }
        flagItem.setItemMeta(meta);

        NBTTagCompound tag = Utility.getNMSTag(flagItem);
        tag.setInt("flag", flag.getValue());
        flagItem = Utility.applyNMSTag(flagItem, tag);

        return flagItem;
    }

    private static ItemStack getInformationItemStack() {
        ItemStack item = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Flags");
        meta.setLore(Collections.singletonList("Flags are special modifiers you can use to customize your claim!"));
        item.setItemMeta(meta);
        return item;
    }

}
