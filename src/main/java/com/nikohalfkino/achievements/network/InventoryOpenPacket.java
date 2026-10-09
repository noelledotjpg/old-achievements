package com.nikohalfkino.achievements.network;

import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InventoryOpenPacket {

    private static final ResourceLocation TAKING_INVENTORY_ID = new ResourceLocation("minecraft", "story/root");

    public static void encode(InventoryOpenPacket msg, FriendlyByteBuf buf) {}

    public static InventoryOpenPacket decode(FriendlyByteBuf buf) {
        return new InventoryOpenPacket();
    }

    public static void handle(InventoryOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Advancement advancement = player.getServer().getAdvancements().getAdvancement(TAKING_INVENTORY_ID);
            if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                player.getAdvancements().award(advancement, "requirement");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
