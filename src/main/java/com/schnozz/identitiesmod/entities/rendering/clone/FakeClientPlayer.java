package com.schnozz.identitiesmod.entities.rendering.clone;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

public class FakeClientPlayer extends AbstractClientPlayer {

    private final GameProfile profile;

    // Minimal fake client player used ONLY for rendering skins
    public FakeClientPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
        this.profile = profile;
    }

    // Ensure the renderer always uses the clone's GameProfile
    @Override
    public GameProfile getGameProfile() {
        return profile;
    }
}