package com.nikohalfkino.achievements.network;

import com.nikohalfkino.achievements.background.BackgroundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BackgroundSyncPacket {

    private static final int MAX_LEN = 262144;

    private final Map<ResourceLocation, String> jsons;

    public BackgroundSyncPacket(Map<ResourceLocation, String> jsons) {
        this.jsons = jsons;
    }

    public static void encode(BackgroundSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.jsons.size());
        msg.jsons.forEach((id, json) -> {
            buf.writeResourceLocation(id);
            buf.writeUtf(json, MAX_LEN);
        });
    }

    public static BackgroundSyncPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Map<ResourceLocation, String> jsons = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            jsons.put(id, buf.readUtf(MAX_LEN));
        }
        return new BackgroundSyncPacket(jsons);
    }

    public static void handle(BackgroundSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> BackgroundManager.setSynced(msg.jsons));
        ctx.get().setPacketHandled(true);
    }
}
