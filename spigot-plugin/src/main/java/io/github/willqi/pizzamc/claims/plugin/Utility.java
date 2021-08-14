package io.github.willqi.pizzamc.claims.plugin;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class Utility {

    public static final ChatColor HEADER_COLOR = ChatColor.BLUE;
    public static final ChatColor TEXT_COLOR = ChatColor.GRAY;

    public static final String NO_PERMISSIONS_MESSAGE = formatResponse("Permissions", "You do not have permission to use this command.", ChatColor.RED);


    public static String formatResponse(String header, String text) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, TEXT_COLOR, text);
    }

    public static String formatResponse(String header, String text, ChatColor color) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, color.toString(), text);
    }

    public static NBTTagCompound getNMSTag(ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        return nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
    }

    public static ItemStack applyNMSTag(ItemStack itemStack, NBTTagCompound tag) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public static void setReflectionProperty(Class clazz, Object object, String propertyName, Object value) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(propertyName);
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Failed to set reflection property despite it being set to accessible.", exception);
        }
    }

    public static Object getReflectionProperty(Class clazz, Object object, String propertyName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(propertyName);
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Failed to retrieve reflection property despite it being set to accessible.", exception);
        }
    }

}
