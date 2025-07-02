package github.sillygoober2;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.util.Identifier;

public class ModConfig extends MidnightConfig {
    public static final String MAIN = "Main";
    public static final String DEBUG = "Debug";

    @Entry(category = MAIN) public static boolean modEnabled = true;
    @Entry(category = MAIN) public static boolean switchToShield = true;
    @Entry(category = MAIN, min=0,max=120) public static int equipCooldown = 1;

    @Entry(category = DEBUG) public static boolean sendAlerts = false;

    public static void init() {
        MidnightConfig.init("auto-totem", ModConfig.class);
    }
}
