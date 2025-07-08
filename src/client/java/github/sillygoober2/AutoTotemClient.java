package github.sillygoober2;

import com.mojang.serialization.DataResult;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import java.util.Arrays;

public class AutoTotemClient implements ClientModInitializer {
	private boolean totemRecentlyPopped = false;
	private static ItemStack previousOffhand = ItemStack.EMPTY;
	private int cooldownTicks = 0;
    public static KeyBinding enableModKeybind;

	@Override
	public void onInitializeClient() {
		MidnightConfig.init("auto-totem", AutoTotemConfig.class);
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        enableModKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-totem.enableDisableMod",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.auto-totem"
        ));
	}

	private void onClientTick(MinecraftClient client){
		if(enableModKeybind.wasPressed()){
			AutoTotemConfig.modEnabled = !AutoTotemConfig.modEnabled;
			cooldownTicks = 0;
			totemRecentlyPopped = false;

			if(AutoTotemConfig.modEnabled){
				sendChatMessage(client,"Auto Totem was ENABLED","#42f551");
			}
			else{
				sendChatMessage(client,"Auto Totem was DISABLED","#f54242");
			}
		}

		if (client.player == null || !AutoTotemConfig.modEnabled) return;

		if (cooldownTicks > 0) {
			cooldownTicks--;
		}
		if (cooldownTicks > 0) return;
		ItemStack current = client.player.getOffHandStack();

		boolean equippedTotem = false;

		if(!previousOffhand.isEmpty() && current.isEmpty() && didTotemPop(client) && !totemRecentlyPopped){
			totemRecentlyPopped = true;
			cooldownTicks = AutoTotemConfig.equipCooldown * 20;
			return;
		}

		if(totemRecentlyPopped && cooldownTicks <= 0){
			totemRecentlyPopped = false;
			var inventory =  MinecraftClient.getInstance().player.getInventory();
			int shieldSlot = -1;
			for(int i = 0; i < inventory.size();i++){
				ItemStack item = inventory.getStack(i);
				if(item.getItem() == Items.SHIELD){
					shieldSlot = i;
				}
				else if(item.getItem() == Items.TOTEM_OF_UNDYING){
					equipOffhand(client, i);
					equippedTotem = true;
					break;
				}
			}
			if(!equippedTotem){
				if(AutoTotemConfig.sendNoTotemAlerts){
					sendChatMessage(client, "No Totem was found!", null);
				}
				if(AutoTotemConfig.switchToShield){
					if(shieldSlot != -1 ){
						equipOffhand(client, shieldSlot);
					}
					else{
						if(AutoTotemConfig.sendNoShieldAlerts){
							sendChatMessage(client, "No Shield was found!", null);
						}
					}
				}

			}
		}

		previousOffhand = current.copy();
	}

	private void equipOffhand(MinecraftClient client, int slot) {
		int screenSlot = slot;

		if (screenSlot == 40) screenSlot = 45;
		else if (screenSlot < 9) screenSlot += 36;

		client.interactionManager.clickSlot(
				client.player.currentScreenHandler.syncId,
				screenSlot,
				40,
				SlotActionType.SWAP,
				client.player
		);
		String itemName = client.player.getOffHandStack().getItem().getName().getString();
		sendChatMessage(
				client,
				itemName+" was automatically equipped after "+ AutoTotemConfig.equipCooldown+" seconds.",
                null
		);
	}

	private boolean didTotemPop(MinecraftClient client) {
		var player = client.player;
		if (player == null) return false;

		var regen = player.getStatusEffect(StatusEffects.REGENERATION);
		var absorb = player.getStatusEffect(StatusEffects.ABSORPTION);
		var fireRes = player.getStatusEffect(StatusEffects.FIRE_RESISTANCE);

		return regen != null && regen.getAmplifier() == 1 && regen.getDuration() >= 880 && regen.getDuration() <= 900; //&&
				//absorb != null && absorb.getAmplifier() == 0 && absorb.getDuration() <= 100 &&
				//fireRes != null && fireRes.getAmplifier() == 0 && fireRes.getDuration() >= 800;
	}

	private void sendChatMessage(MinecraftClient client,String message,String colorHex){
        if(colorHex != null){
            String[] words = message.trim().split("\\s+");
            String lastWord = words[words.length - 1];
            String beforeLast = String.join(" ", Arrays.copyOf(words, words.length - 1));

            int rgb = Integer.parseInt(colorHex.replace("#", ""), 16);
            TextColor color = TextColor.fromRgb(rgb);

            Text uncolored = Text.literal(beforeLast + " ");
            Text colored = Text.literal(lastWord).styled(style -> style.withColor(color));

            Text fullMessage = uncolored.copy().append(colored);

            client.inGameHud.getChatHud().addMessage(fullMessage);
        }
        else{
			colorHex = AutoTotemConfig.alertsChatColor;
			int rgb = Integer.parseInt(colorHex.replace("#",""), 16);
			TextColor color = TextColor.fromRgb(rgb);
			client.inGameHud.getChatHud().addMessage(Text.literal(message).styled(style -> style.withColor(color)));
        }
	}

}