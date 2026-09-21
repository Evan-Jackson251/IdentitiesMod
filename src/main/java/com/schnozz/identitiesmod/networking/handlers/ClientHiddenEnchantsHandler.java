package com.schnozz.identitiesmod.networking.handlers;

import com.schnozz.identitiesmod.client_data.ClientSeerData;
import com.schnozz.identitiesmod.networking.payloads.HiddenEnchantsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientHiddenEnchantsHandler {
    public static void handle(HiddenEnchantsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;

            if (player == null
                    || !(player.containerMenu instanceof EnchantmentMenu menu)
                    || menu.containerId != payload.containerId())
            {return;}

            ClientSeerData.set(menu, payload.options());

            System.out.println("ENCHANT OPTIONS: " + payload.options());
        });
    }
}
