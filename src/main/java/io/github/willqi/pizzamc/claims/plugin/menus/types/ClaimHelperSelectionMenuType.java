package io.github.willqi.pizzamc.claims.plugin.menus.types;

import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Wool;

import java.util.*;
import java.util.stream.Collectors;

public class ClaimHelperSelectionMenuType implements MenuType {

    public static final String ID = "claim_helper_selection_menu";

    private final ClaimsPlugin plugin;

    private final Map<UUID, Map<String, Object>> openInventories;

    public ClaimHelperSelectionMenuType(ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        int currentPage = (int)params.get("page");
        Claim claim = (Claim)params.get("claim");

        List<ClaimHelper> helpers = this.getHelpers(claim);
        Inventory inventory = Bukkit.createInventory(player, 54, "Claim Helpers");

        for (int i = 1; i < 8; i++) {
            inventory.setItem(i, getEmptyGlassPane());
        }

        // Do we need the previous page button?
        if (currentPage > 1) {
            inventory.setItem(0, getPreviousPageItemStack(currentPage));
        } else {
            inventory.setItem(0, getNoPageOptionItemStack());
        }

        // Do we need the next page button?
        int skippedHelpers = (currentPage - 1) * 45;
        if (helpers.size() - skippedHelpers > 45) {
            inventory.setItem(8, getNextPageItemStack(currentPage));
        } else {
            inventory.setItem(8, getNoPageOptionItemStack());
        }

        inventory.setItem(4, getAddNewHelperItemStack());

        // Place claims in inventory
        List<ClaimHelper> shownHelpers = helpers.subList((currentPage - 1) * 45, Math.min(currentPage * 45, helpers.size()));
        int inventoryIndex = 9;
        for (ClaimHelper helper : shownHelpers) {
            inventory.setItem(inventoryIndex, getHelperItemStack(helper));
            inventoryIndex++;
        }

        player.openInventory(inventory);
        this.openInventories.put(player.getUniqueId(), params);
    }

    @Override
    public void onClose(Player player) {
        player.closeInventory();
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            Player player = (Player)event.getInventory().getHolder();
            if (this.openInventories.containsKey(player.getUniqueId()) && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);

                int currentPage = (int)this.openInventories.get(player.getUniqueId()).get("page");
                Claim claim = (Claim)this.openInventories.get(player.getUniqueId()).get("claim");
                switch (event.getSlot()) {
                    case 0:
                        if (event.getCurrentItem().getDurability() != (short)7) {
                            // Go back button
                            Map<String, Object> params = new HashMap<>();
                            params.put("page", currentPage - 1);
                            params.put("claim", claim);
                            this.plugin.getMenuManager().showMenu(player, ID, params);
                        }
                        break;
                    case 4:
                        player.closeInventory();
                        Map<String, Object> findHelperParams = new HashMap<>();
                        findHelperParams.put("claim", claim);
                        this.plugin.getMenuManager().showMenu(player, ClaimHelperLookUpType.ID, findHelperParams);
                        break;
                    case 8:
                        if (event.getCurrentItem().getDurability() != (short)7) {
                            // Go forward button
                            Map<String, Object> params = new HashMap<>();
                            params.put("page", currentPage + 1);
                            params.put("claim", claim);
                            this.plugin.getMenuManager().showMenu(player, ID, params);
                        }
                        break;
                    default:
                        if (event.getSlot() > 8) {
                            int helperIndex = event.getSlot() - 9 + (currentPage - 1) * 45;
                            List<ClaimHelper> helpers = this.getHelpers(claim);
                            if (helpers.size() > helperIndex) {
                                ClaimHelper helper = helpers.get(helperIndex);
                                Map<String, Object> params = new HashMap<>();
                                params.put("uuid", helper.getUuid());
                                params.put("claim", claim);
                                this.plugin.getMenuManager().showMenu(player, ClaimHelperFlagsType.ID, params);
                            }
                        }
                        break;
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



    private List<ClaimHelper> getHelpers(Claim claim) {
        Optional<Set<ClaimHelper>> helpers = this.plugin.getClaimsManager().getClaimHelpers(claim.getCoordinates());
        if (helpers.isPresent()) {
            return new ArrayList<>(helpers.get())
                    .stream()
                    .sorted((helperA, helperB) -> {
                        OfflinePlayer playerA = this.plugin.getServer().getOfflinePlayer(helperA.getUuid());
                        OfflinePlayer playerB = this.plugin.getServer().getOfflinePlayer(helperB.getUuid());
                        return playerA.getName().compareTo(playerB.getName());
                    })
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private static ItemStack getPreviousPageItemStack(int currentPage) {
        ItemStack goBackItem = new Wool(DyeColor.RED).toItemStack(1);
        ItemMeta meta = goBackItem.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "Previous Page (" + (currentPage - 1) + ")");
        goBackItem.setItemMeta(meta);
        return goBackItem;
    }

    private static ItemStack getNextPageItemStack(int currentPage) {
        ItemStack goForwardItem = new Wool(DyeColor.LIME).toItemStack(1);
        ItemMeta meta = goForwardItem.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "Next Page (" + (currentPage + 1) + ")");
        goForwardItem.setItemMeta(meta);
        return goForwardItem;
    }

    private static ItemStack getNoPageOptionItemStack() {
        ItemStack noOptionItem = new Wool(DyeColor.GRAY).toItemStack(1);
        ItemMeta meta = noOptionItem.getItemMeta();
        meta.setDisplayName(" ");
        noOptionItem.setItemMeta(meta);
        return noOptionItem;
    }

    private static ItemStack getAddNewHelperItemStack() {
        ItemStack newHelperBlock = new ItemStack(Material.BOOK_AND_QUILL, 1);
        ItemMeta meta = newHelperBlock.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.GREEN + "Add New Helper +");

        newHelperBlock.setItemMeta(meta);
        return newHelperBlock;
    }

    private static ItemStack getHelperItemStack(ClaimHelper helper) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(helper.getUuid());
        ItemStack helperBlock = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta skullMeta = (SkullMeta)helperBlock.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("" + ChatColor.RESET + ChatColor.YELLOW + player.getName());

        List<String> lore = new ArrayList<>();
        lore.add("Click to modify their permissions");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Permisions:");
        for (ClaimHelper.Permission permission : ClaimHelper.Permission.values()) {
            if (helper.hasPermission(permission)) {
                lore.add("" + ChatColor.GREEN + permission.getTitle());
            }
        }
        skullMeta.setLore(lore);

        helperBlock.setItemMeta(skullMeta);
        return helperBlock;
    }

    private static ItemStack getEmptyGlassPane() {
        ItemStack emptyGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1,  (short)7);
        ItemMeta meta = emptyGlassPane.getItemMeta();
        meta.setDisplayName(" ");
        emptyGlassPane.setItemMeta(meta);
        return emptyGlassPane;
    }

}
