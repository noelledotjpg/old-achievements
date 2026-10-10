package com.nikohalfkino.achievements.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    private PacketHandler() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(
            InventoryOpenPacket.TYPE,
            InventoryOpenPacket.STREAM_CODEC,
            InventoryOpenPacket::handle
        );
        registrar.playToClient(
            BackgroundSyncPacket.TYPE,
            BackgroundSyncPacket.STREAM_CODEC,
            BackgroundSyncPacket::handle
        );
    }
}
