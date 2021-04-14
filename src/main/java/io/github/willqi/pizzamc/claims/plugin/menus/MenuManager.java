package io.github.willqi.pizzamc.claims.plugin.menus;

import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import io.github.willqi.pizzamc.claims.api.homes.Home;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MenuManager implements Listener {

    private final int BACK_ARROW_INDEX = 0;
    private final int FORWARD_ARROW_INDEX = 8;
    private final String NBT_HOME_ID = "io.github.willqi.home_name";
    private final Map<UUID, PlayerMenuData> openMenus = new HashMap<>();
    private final ClaimsPlugin plugin;

    // MUST BE A MULTIPLE OF 9 THAT IS AT LESAT 18
    private final static int HOME_MENU_SIZE = 54;
    private final static int HOME_MENU_ITEMS_PER_PAGE = HOME_MENU_SIZE - 9;

    public MenuManager (final ClaimsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getPlugin(ClaimsPlugin.class));
    }

    /**
     * Send the home selection menu to a player
     * @param player The player you want to send the menu to
     */
    public void sendHomeMenu (final Player player) {
        sendHomeMenu(player, 0);
    }

    /**
     * Send the home selection menu to a player for a specific page given their homes
     * @param player The player you want to send the menu to
     * @param page The page you want to send to the player
     */
    public void sendHomeMenu (final Player player, final int page) {

        final Map<String, Home> sortedPlayerHomes = new TreeMap<>(plugin.getHomesManager().getHomes(player));

        final Inventory menu = Bukkit.createInventory(player, HOME_MENU_SIZE, "Your Homes");
        int homeItemsAllowedOnScreen = HOME_MENU_ITEMS_PER_PAGE - 9;
        int currentIndex = 9;

        int itemsToRemove = page * homeItemsAllowedOnScreen;
        Iterator<String> playerHomeIterator = sortedPlayerHomes.keySet().iterator();
        while (itemsToRemove > 0 && playerHomeIterator.hasNext()) {
            playerHomeIterator.next();
            itemsToRemove--;
            playerHomeIterator.remove();
        }

        if (page > 0) {
            final ItemStack goBackItem = new ItemStack(Material.WOOL, 1, (short)14);
            final ItemMeta goBackItemMeta = goBackItem.getItemMeta();
            goBackItemMeta.setDisplayName(String.format("%sPrevious Page", ChatColor.GREEN));
            goBackItem.setItemMeta(goBackItemMeta);
            menu.setItem(BACK_ARROW_INDEX, goBackItem);
        }

        for (final Map.Entry<String, Home> entry : sortedPlayerHomes.entrySet()) {
            if (homeItemsAllowedOnScreen <= 0) {
                // max homes on screen
                final ItemStack goForwardItem = new ItemStack(Material.WOOL, 1, (short)5);
                final ItemMeta goForwardItemMeta = goForwardItem.getItemMeta();
                goForwardItemMeta.setDisplayName(String.format("%sNext Page", ChatColor.GREEN));
                goForwardItem.setItemMeta(goForwardItemMeta);
                menu.setItem(FORWARD_ARROW_INDEX, goForwardItem);
                break;
            }
            // Add home
            ItemStack homeItem = new ItemStack(Material.BED, 1, (short)14);
            final ItemMeta meta = homeItem.getItemMeta();
            meta.setDisplayName(String.format("%sView details for home: %s", ChatColor.YELLOW, entry.getValue().getName()));
            homeItem.setItemMeta(meta);

            final NBTTagCompound compound = Utility.getNBTCompound(homeItem);
            compound.setString(NBT_HOME_ID, entry.getValue().getName());
            homeItem = Utility.applyNBTTagCompound(homeItem, compound);

            menu.setItem(currentIndex, homeItem);

            homeItemsAllowedOnScreen--;
            currentIndex++;
        }

        player.openInventory(menu);

        final PlayerMenuData data = new PlayerMenuData(MenuType.HOME_SELECTION);
        data.getParams().put("page", page);
        openMenus.put(player.getUniqueId(), data);

    }

    /**
     * Send the home information menu to a player
     * @param player The player you want to send the menu to
     * @param home The home in question
     */
    public void sendHomeInformationMenu (final Player player, final Home home) {
        final Inventory menu = Bukkit.createInventory(player, InventoryType.HOPPER, String.format("Home - %s", home.getName()));

        final ItemStack gotoHomeItem = new ItemStack(Material.BED, 1, (short)14);
        final ItemMeta gotoHomeItemMeta = gotoHomeItem.getItemMeta();
        gotoHomeItemMeta.setDisplayName(String.format("%sTeleport To Home", ChatColor.GREEN));
        gotoHomeItemMeta.setLore(Arrays.asList(
                String.format("%s(%s, %s, %s) (Dimension: %s)", ChatColor.BLUE, home.getX(), home.getY(), home.getZ(), Bukkit.getWorld(home.getLevelUUID()).getName())
        ));
        gotoHomeItem.setItemMeta(gotoHomeItemMeta);

        final ItemStack editNameItem = new ItemStack(Material.WOOL);
        final ItemMeta editNameItemMeta = editNameItem.getItemMeta();
        editNameItemMeta.setDisplayName(String.format("%sName: %s", ChatColor.YELLOW, home.getName()));
        editNameItem.setItemMeta(editNameItemMeta);

        final ItemStack deleteHomeItem = new ItemStack(Material.BARRIER);
        final ItemMeta deleteHomeItemMeta = deleteHomeItem.getItemMeta();
        deleteHomeItemMeta.setDisplayName(String.format("%sDelete Home", ChatColor.RED));
        deleteHomeItem.setItemMeta(deleteHomeItemMeta);

        menu.setItem(0, gotoHomeItem);
        menu.setItem(2, editNameItem);
        menu.setItem(4, deleteHomeItem);
        player.openInventory(menu);
        final PlayerMenuData data = new PlayerMenuData(MenuType.HOME_INFORMATION);
        data.getParams().put("home", home);
        openMenus.put(player.getUniqueId(), data);
    }

    /**
     * Send the remove home confirmation menu
     * @param player the player
     * @param home the home that may be deleted
     */
    public void sendRemoveHomeConfirmationMenu (final Player player, final Home home) {

        final Inventory menu = Bukkit.createInventory(player, InventoryType.HOPPER, String.format("Do you really want to delete the home named %s?", home.getName()));

        final ItemStack noItem = new ItemStack(Material.WOOL, 1, (short)0);
        final ItemMeta noItemMeta = noItem.getItemMeta();
        noItemMeta.setDisplayName(String.format("%sNo", ChatColor.RED));
        noItem.setItemMeta(noItemMeta);

        final ItemStack yesItem = new ItemStack(Material.WOOL, 1, (short)0);
        final ItemMeta yesItemMeta = yesItem.getItemMeta();
        yesItemMeta.setDisplayName(String.format("%sYes", ChatColor.GREEN));
        yesItem.setItemMeta(yesItemMeta);

        final ItemStack promptItem = new ItemStack(Material.BED, 1, (short)14);
        final ItemMeta promptItemMeta = promptItem.getItemMeta();
        promptItemMeta.setDisplayName(String.format("%sAre you sure you want to delete the home named %s?", ChatColor.RED, home.getName()));
        promptItem.setItemMeta(promptItemMeta);

        menu.setItem(0, noItem);
        menu.setItem(2, promptItem);
        menu.setItem(4, yesItem);

        player.openInventory(menu);
        final PlayerMenuData data = new PlayerMenuData(MenuType.CONFIRM_HOME_DELETION);
        data.getParams().put("home", home);
        openMenus.put(player.getUniqueId(), data);

    }

    @EventHandler
    public void onMenuItemChosen (final InventoryClickEvent event) {

        if (openMenus.containsKey(event.getWhoClicked().getUniqueId()) && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {

            final PlayerMenuData data = openMenus.get(event.getWhoClicked().getUniqueId());
            event.setCancelled(true);

            switch (data.getType()) {

                case HOME_SELECTION:
                    switch (event.getSlot()) {

                        case BACK_ARROW_INDEX:
                            sendHomeMenu((Player)event.getWhoClicked(), (Integer)data.getParams().get("page") - 1);
                            break;

                        case FORWARD_ARROW_INDEX:
                            sendHomeMenu((Player)event.getWhoClicked(), (Integer)data.getParams().get("page") + 1);
                            break;

                        default:
                            final Home targetHome = plugin.getHomesManager().getHome(
                                    (Player)event.getWhoClicked(),
                                    Utility.getNBTCompound(event.getCurrentItem())
                                            .getString(NBT_HOME_ID)
                                            .toLowerCase()
                            ).get(); // The home should always exist since we aren't able to delete the home while it's still a option we can select.
                            sendHomeInformationMenu((Player)event.getWhoClicked(), targetHome);
                            break;

                    }
                    break;

                case HOME_INFORMATION:
                    final Home homeInQuestion = (Home)data.getParams().get("home");
                    switch (event.getSlot()) {

                        case 0: // Teleport
                            event.getWhoClicked().closeInventory();
                            event.getWhoClicked().teleport(
                                    new Location(
                                            event.getWhoClicked().getServer().getWorld(homeInQuestion.getLevelUUID()),
                                            homeInQuestion.getX(),
                                            homeInQuestion.getY(),
                                            homeInQuestion.getZ()
                                    ),
                                    PlayerTeleportEvent.TeleportCause.PLUGIN
                            );
                            event.getWhoClicked().sendMessage(Utility.formatResponse("Homes", "Teleported!"));
                            break;

                        case 4: // Delete
                            sendRemoveHomeConfirmationMenu((Player)event.getWhoClicked(), homeInQuestion);
                            break;

                    }
                    break;

                case CONFIRM_HOME_DELETION:
                    final Home homeBeingDeleted = (Home)data.getParams().get("home");
                    switch (event.getSlot()) {
                        case 0:
                        case 4:
                            event.getWhoClicked().closeInventory();
                            clearMenuData((Player)event.getWhoClicked());
                            break;
                    }
                    if (event.getSlot() == 4) { // Confirm
                        homeBeingDeleted.destroy();
                        event.getWhoClicked().sendMessage(Utility.formatResponse("Homes", String.format("Deleted home named %s!", homeBeingDeleted.getName()), ChatColor.GREEN));
                    }
                    break;

            }
        }

    }

    @EventHandler
    public void onMenuClose (final InventoryCloseEvent event) {
        clearMenuData((Player)event.getPlayer());
    }

    private void clearMenuData (final Player player) {
        if (openMenus.containsKey(player.getUniqueId())) {
            openMenus.remove(player.getUniqueId());
        }
    }

    private enum MenuType {
        HOME_SELECTION,
        HOME_INFORMATION,
        CONFIRM_HOME_DELETION
    }

    public static class PlayerMenuData {
        private final MenuType type;
        private final Map<String, Object> params = new HashMap<>();

        public PlayerMenuData (final MenuType type) {
            this.type = type;
        }

        public MenuType getType () {
            return type;
        }

        public Map<String, Object> getParams () {
            return params;
        }

    }

}
