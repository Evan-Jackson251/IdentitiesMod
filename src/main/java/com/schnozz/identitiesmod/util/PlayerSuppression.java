package com.schnozz.identitiesmod.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class PlayerSuppression {
    public static void shutDown(Minecraft mc){
        var options = mc.options;

        suppress(options.keyAttack);
        suppress(options.keyUse);
        suppress(options.keyPickItem);
        suppress(options.keyDrop);
        suppress(options.keySwapOffhand);
        suppress(options.keyInventory);

        for (KeyMapping key : options.keyHotbarSlots) {
            suppress(key);
        }

        if (mc.screen instanceof AbstractContainerScreen<?>) {
            mc.player.closeContainer();
        }
    }

    public static void suppress(KeyMapping key) {
        key.setDown(false);
        // Discard queued presses as well as the held state.
        while (key.consumeClick()) {}
    }
}
