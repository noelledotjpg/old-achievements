package com.nikohalfkino.achievements.network;

import com.nikohalfkino.achievements.OldAchievements;
import com.nikohalfkino.achievements.background.BackgroundManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class BackgroundSyncPacket implements CustomPacketPayload {

    private static final int MAX_LEN = 262144;

    private final Map<ResourceLocation, String> jsons;

    public BackgroundSyncPacket(Map<ResourceLocation, String> jsons) {
        this.jsons = jsons;
    }

    public static final Type<BackgroundSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(OldAchievements.MODID, "background_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BackgroundSyncPacket> STREAM_CODEC = StreamCodec.of(BackgroundSyncPacket::encode, BackgroundSyncPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, BackgroundSyncPacket msg) {
        buf.writeVarInt(msg.jsons.size());
        msg.jsons.forEach((id, json) -> {
            buf.writeResourceLocation(id);
            buf.writeUtf(json, MAX_LEN);
        });
    }

    public static BackgroundSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Map<ResourceLocation, String> jsons = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            jsons.put(id, buf.readUtf(MAX_LEN));
        }
        return new BackgroundSyncPacket(jsons);
    }

    public static void handle(BackgroundSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BackgroundManager.setSynced(msg.jsons));
    }
}
