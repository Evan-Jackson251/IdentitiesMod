package com.schnozz.identitiesmod.buttons.power_screen_buttons;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.networking.payloads.PowerTakenPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.PowerSyncPayload;
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
        if(!p.getData(ModDataAttachments.POWER_TYPE).equals("")){return;}
        p.setData(ModDataAttachments.POWER_TYPE, power);
        PacketDistributor.sendToServer(new PowerSyncPayload(power));
        PacketDistributor.sendToServer(new PowerTakenPayload(power));

        if(power.equals("Adaptation"))
        {
            p.sendSystemMessage(Component.literal("You can now Adapt").withColor(0x1495e5));
        }
        else if(power.equals("Parry"))
        {
            p.sendSystemMessage(Component.literal("You can now Parry").withColor(0x1495e5));
        }
        else if(power.equals("Gravity"))
        {
            p.sendSystemMessage(Component.literal("You can now control Gravity").withColor(0x1495e5));
        }
        else {
            p.sendSystemMessage(Component.literal("You are now the " + power).withColor(0x1495e5));
        }
    }
}
