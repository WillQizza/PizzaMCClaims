package io.github.willqi.pizzamc.claims.plugin.menus.types;

import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ClaimHelperLookUpType implements MenuType {

    public static final String ID = "claim_helper_lookup";

    private final Map<UUID, Claim> openInventories;
    private final ClaimsPlugin plugin;

    public ClaimHelperLookUpType(ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        int containerId = entityPlayer.nextContainerCounter();

        PacketPlayOutOpenWindow packetPlayOutOpenWindow = new PacketPlayOutOpenWindow();
        PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot();

        ItemStack playerLookupItem = new ItemStack(Item.getById(339));
        NBTTagCompound tag = playerLookupItem.hasTag() ? playerLookupItem.getTag() : new NBTTagCompound();
        NBTTagCompound nameCompound = new NBTTagCompound();
        nameCompound.setString("Name", "Type player name");
        tag.set("display", nameCompound);
        playerLookupItem.setTag(tag);

        try {
            Utility.setReflectionProperty(PacketPlayOutOpenWindow.class, packetPlayOutOpenWindow, "a", containerId);
            Utility.setReflectionProperty(PacketPlayOutOpenWindow.class, packetPlayOutOpenWindow, "b", "minecraft:anvil");

            Utility.setReflectionProperty(PacketPlayOutSetSlot.class, packetPlayOutSetSlot, "a", containerId);
            Utility.setReflectionProperty(PacketPlayOutSetSlot.class, packetPlayOutSetSlot, "b", 0);
            Utility.setReflectionProperty(PacketPlayOutSetSlot.class, packetPlayOutSetSlot, "c", playerLookupItem);
        } catch (NoSuchFieldException exception) {
            player.sendMessage(Utility.formatResponse("Claims", "An exception has occurred.", ChatColor.RED));
            throw new AssertionError("Target field could not be found. Are you on 1.12.2?", exception);
        }

        ContainerAnvil anvilContainer = new ClaimHelperAnvilContainer(player);
        anvilContainer.windowId = containerId;

        anvilContainer.setItem(0, playerLookupItem);
        anvilContainer.addSlotListener(entityPlayer);

        entityPlayer.activeContainer = anvilContainer;
        entityPlayer.playerConnection.sendPacket(packetPlayOutOpenWindow);
        entityPlayer.playerConnection.sendPacket(packetPlayOutSetSlot);

        Claim claim = (Claim)params.get("claim");
        this.openInventories.put(player.getUniqueId(), claim);
        
    }

    @Override
    public void onClose(Player player) {
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player && this.openInventories.containsKey(((Player)event.getInventory().getHolder()).getUniqueId())) {
            event.setCancelled(true);

            Player player = (Player)event.getInventory().getHolder();
            if (event.getSlotType() == InventoryType.SlotType.RESULT && event.getCurrentItem().getType() != Material.AIR) {
                String targetName = event.getCurrentItem().getItemMeta().getDisplayName();

                Claim claim = this.openInventories.get(player.getUniqueId());
                player.closeInventory();

                Map<String, Object> params = new HashMap<>();
                params.put("claim", claim);
                params.put("name", targetName);

                Player targetPlayer = Bukkit.getPlayerExact(targetName);
                if (targetPlayer != null) {
                    params.put("uuid", targetPlayer.getUniqueId());
                    this.plugin.getMenuManager().showMenu(player, ClaimHelperFlagsType.ID, params);
                } else {
                    this.plugin.getUsersManager().fetchUser(targetName).whenComplete((user, exception) -> {
                        if (exception != null) {
                            this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch user record by name of " + targetName, exception);
                            player.sendMessage(Utility.formatResponse("Claims", "An exception has occurred!", ChatColor.RED));
                        } else {
                            if (user.isPresent()) {
                                params.put("uuid", user.get().getUUID());
                                this.plugin.getMenuManager().showMenu(player, ClaimHelperFlagsType.ID, params);
                            } else {
                                player.sendMessage(Utility.formatResponse("Claims", "That player does not exist or has not logged on the server yet!"));
                            }
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

    private static class ClaimHelperAnvilContainer extends ContainerAnvil {

        private final Player player;

        public ClaimHelperAnvilContainer(Player player) {
            super(((CraftPlayer)player).getHandle().inventory, ((CraftPlayer)player).getHandle().world, new BlockPosition(0, 0, 0), ((CraftPlayer)player).getHandle());
            this.checkReachable = false;
            this.player = player;

            // Ensure owner exists on anvil
            try {
                InventorySubcontainer subInventory = (InventorySubcontainer)Utility.getReflectionProperty(ContainerAnvil.class, this, "h");
                Utility.setReflectionProperty(InventorySubcontainer.class, subInventory, "bukkitOwner", player);
            } catch (NoSuchFieldException exception) {
                throw new AssertionError("Failed to find fields. Are you sure you're on v1.12.2?", exception);
            }

        }

        @Override
        public void e() {
            super.e();
            this.levelCost = 0;
        }

        @Override
        public void b(EntityHuman entityhuman) {
            // Prevent player from getting paper when closing inventory
        }
    }

}
