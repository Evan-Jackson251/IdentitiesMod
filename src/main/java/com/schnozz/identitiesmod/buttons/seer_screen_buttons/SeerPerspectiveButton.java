package com.schnozz.identitiesmod.buttons.seer_screen_buttons;

import com.schnozz.identitiesmod.networking.payloads.SeerPerspectivePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class SeerPerspectiveButton extends Button{
    private UUID id;
    public SeerPerspectiveButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, CreateNarration createNarration, UUID id) {
        super(x, y, width, height, message, onPress, createNarration);
        this.id = id;
    }
    @Override
    public void onPress()
    {
        Minecraft mc = Minecraft.getInstance();

        Player seerPlayer = mc.player;
        if(seerPlayer == null){return;}
        if(mc.level == null){return;}

        PacketDistributor.sendToServer(new SeerPerspectivePayload(id));
    }
}
