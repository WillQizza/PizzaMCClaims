package io.github.willqi.pizzamc.claims.plugin;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Utility {

    public static final ChatColor HEADER_COLOR = ChatColor.BLUE;

    public static final ChatColor TEXT_COLOR = ChatColor.GRAY;

    public static String formatResponse (String header, String text) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, TEXT_COLOR, text);
    }

    public static String formatResponse (String header, String text, ChatColor color) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, color.toString(), text);
    }

    public static NBTTagCompound getNBTCompound (org.bukkit.inventory.ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack nmsCopyItem = CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.server.v1_12_R1.NBTTagCompound nbtTag = nmsCopyItem.hasTag() ? nmsCopyItem.getTag() : new net.minecraft.server.v1_12_R1.NBTTagCompound();
        return nbtTag;
    }

    public static ItemStack applyNBTTagCompound (org.bukkit.inventory.ItemStack itemStack, NBTTagCompound compound) {
        net.minecraft.server.v1_12_R1.ItemStack nmsCopyItem = CraftItemStack.asNMSCopy(itemStack);
        nmsCopyItem.setTag(compound);
        return CraftItemStack.asBukkitCopy(nmsCopyItem);
    }

}
