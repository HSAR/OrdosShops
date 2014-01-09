package org.landofordos.ordosshops;

import org.bukkit.entity.Player;

public class Utils {

    public static String stripName(String name) {
        if (name.length() > 15) {
            return name.substring(0, 15);
        }

        return name;
    }

    public static String stripName(Player player) {
        return stripName(player.getName());
    }

}
