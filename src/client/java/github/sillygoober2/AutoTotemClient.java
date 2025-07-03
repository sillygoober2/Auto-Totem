package github.sillygoober2;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

public class AutoTotemClient implements ClientModInitializer {
	private boolean totemRecentlyPopped = false;
	private static ItemStack previousOffhand = ItemStack.EMPTY;
	private int cooldownTicks = 0;

	@Override
	public void onInitializeClient() {
		MidnightConfig.init("auto-totem", ModConfig.class);
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client){
		if (client.player == null || !ModConfig.modEnabled) return;

		if (cooldownTicks > 0) {
			cooldownTicks--;
		}
		if (cooldownTicks > 0) return;
		ItemStack current = client.player.getOffHandStack();

		boolean equippedTotem = false;

		if(!previousOffhand.isEmpty() && current.isEmpty() && didTotemPop(client) && !totemRecentlyPopped){
			totemRecentlyPopped = true;
			cooldownTicks = ModConfig.equipCooldown * 20;
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
				sendChatMessage(client, "No Totem was found!");
				if(ModConfig.switchToShield){
					if(shieldSlot != -1 ){
						equipOffhand(client, shieldSlot);
					}
					else{
						sendChatMessage(client, "No Shield was found!");
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
		String itemName = client.player.getOffHandStack().getItemName().getString();
		sendChatMessage(
				client,
				itemName+" was automatically equipped after "+ModConfig.equipCooldown+" seconds."
		);
	}

	private boolean didTotemPop(MinecraftClient client) {
		var player = client.player;
		if (player == null) return false;

		var regen = player.getStatusEffect(StatusEffects.REGENERATION);
		var absorb = player.getStatusEffect(StatusEffects.ABSORPTION);
		var fireRes = player.getStatusEffect(StatusEffects.FIRE_RESISTANCE);

		return regen != null && regen.getAmplifier() == 1 && regen.getDuration() >= 800; //&&
				//absorb != null && absorb.getAmplifier() == 0 && absorb.getDuration() <= 100 &&
				//fireRes != null && fireRes.getAmplifier() == 0 && fireRes.getDuration() >= 800;
	}

	private void sendChatMessage(MinecraftClient client,String message){
		if(ModConfig.sendAlerts){
			client.inGameHud.getChatHud().addMessage(Text.literal(message));
		}
	}

}