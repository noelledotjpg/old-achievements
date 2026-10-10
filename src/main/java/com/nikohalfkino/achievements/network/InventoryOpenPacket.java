package com.nikohalfkino.achievements.network;

import com.nikohalfkino.achievements.OldAchievements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class InventoryOpenPacket implements CustomPacketPayload {

    private static final ResourceLocation TAKING_INVENTORY_ID = ResourceLocation.withDefaultNamespace("story/root");

    public static final Type<InventoryOpenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(OldAchievements.MODID, "inventory_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryOpenPacket> STREAM_CODEC = StreamCodec.of((buf, packet) -> {}, buf -> new InventoryOpenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(InventoryOpenPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        AdvancementHolder advancement = player.getServer().getAdvancements().get(TAKING_INVENTORY_ID);
        if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, "requirement");
        }
    }
}
