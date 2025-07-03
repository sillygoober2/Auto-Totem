package github.sillygoober2;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {
    public static final String MAIN = "Main";
    public static final String DEBUG = "Debug";

    @Entry(category = MAIN) public static boolean modEnabled = true;
    @Entry(category = MAIN) public static boolean switchToShield = true;
    @Entry(category = MAIN, min=0,max=120) public static int equipCooldown = 1;

    @Entry(category = DEBUG) public static boolean sendAlerts = false;
}
