package io.github.willqi.pizzamc.claims;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Utility {

    public static final ChatColor HEADER_COLOR = ChatColor.BLUE;

    public static final ChatColor TEXT_COLOR = ChatColor.GRAY;

    public static String formatResponse (final String header, final String text) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, TEXT_COLOR, text);
    }

    public static String formatResponse (final String header, final String text, final ChatColor color) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, color.toString(), text);
    }

    public static NBTTagCompound getNBTCompound (final org.bukkit.inventory.ItemStack itemStack) {
        final net.minecraft.server.v1_12_R1.ItemStack nmsCopyItem = CraftItemStack.asNMSCopy(itemStack);
        final net.minecraft.server.v1_12_R1.NBTTagCompound nbtTag = nmsCopyItem.hasTag() ? nmsCopyItem.getTag() : new net.minecraft.server.v1_12_R1.NBTTagCompound();
        return nbtTag;
    }

    public static ItemStack applyNBTTagCompound (final org.bukkit.inventory.ItemStack itemStack, final NBTTagCompound compound) {
        final net.minecraft.server.v1_12_R1.ItemStack nmsCopyItem = CraftItemStack.asNMSCopy(itemStack);
        nmsCopyItem.setTag(compound);
        return CraftItemStack.asBukkitCopy(nmsCopyItem);
    }

}
