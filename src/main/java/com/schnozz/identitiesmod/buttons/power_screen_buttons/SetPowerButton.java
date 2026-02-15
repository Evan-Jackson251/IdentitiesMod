package com.schnozz.identitiesmod.buttons.power_screen_buttons;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.leveldata.PowerSavedData;
import com.schnozz.identitiesmod.networking.payloads.PowerTakenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class SetPowerButton extends Button{
    String power;
    public SetPowerButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, CreateNarration createNarration, String power) {
        super(x, y, width, height, message, onPress, createNarration);
        this.power = power;
    }
    @Override
    public void onPress()
    {
        Player p = Minecraft.getInstance().player;
        if(p == null){return;}

        p.setData(ModDataAttachments.POWER_TYPE, power);
        PacketDistributor.sendToServer(new PowerTakenPayload(power));
    }
}
