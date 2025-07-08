package github.sillygoober2;

import eu.midnightdust.lib.config.MidnightConfig;

public class AutoTotemConfig extends MidnightConfig {
    public static final String MAIN = "Main";

    @Entry(category = MAIN) public static boolean modEnabled = true;
    @Entry(category = MAIN) public static boolean switchToShield = true;
    @Entry(category = MAIN, min=0,max=120) public static int equipCooldown = 1;
    @Comment(category = MAIN) public static Comment spacer1;
    @Entry(category = MAIN) public static boolean sendNoTotemAlerts = false;
    @Entry(category = MAIN) public static boolean sendNoShieldAlerts = false;
    @Comment(category = MAIN) public static Comment spacer2;
    @Entry(category = MAIN, width = 7, min = 7, isColor = true) public static String alertsChatColor = "#FF0000";

}
