package io.github.willqi.pizzamc.claims.plugin;

import org.bukkit.ChatColor;

public class Utility {

    public static final ChatColor HEADER_COLOR = ChatColor.BLUE;
    public static final ChatColor TEXT_COLOR = ChatColor.GRAY;

    public static final String NO_PERMISSIONS_MESSAGE = formatResponse("Permissions", "You do not have permissions to use this command.", ChatColor.RED);

    public static String formatResponse (String header, String text) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, TEXT_COLOR, text);
    }

    public static String formatResponse (String header, String text, ChatColor color) {
        return String.format("%s%s> %s%s", HEADER_COLOR, header, color.toString(), text);
    }

}
