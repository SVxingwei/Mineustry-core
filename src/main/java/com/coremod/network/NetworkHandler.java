package com.coremod.network;

import com.coremod.CoreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络处理器
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CoreMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(PacketStorageRequest.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketStorageRequest::new)
                .encoder(PacketStorageRequest::toBytes)
                .consumerMainThread(PacketStorageRequest::handle)
                .add();

        INSTANCE.messageBuilder(PacketStorageSync.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketStorageSync::new)
                .encoder(PacketStorageSync::toBytes)
                .consumerMainThread(PacketStorageSync::handle)
                .add();

        INSTANCE.messageBuilder(PacketExtractItem.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketExtractItem::new)
                .encoder(PacketExtractItem::toBytes)
                .consumerMainThread(PacketExtractItem::handle)
                .add();

        INSTANCE.messageBuilder(PacketInsertItem.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketInsertItem::new)
                .encoder(PacketInsertItem::toBytes)
                .consumerMainThread(PacketInsertItem::handle)
                .add();

        INSTANCE.messageBuilder(PacketConfigUpdate.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketConfigUpdate::new)
                .encoder(PacketConfigUpdate::toBytes)
                .consumerMainThread(PacketConfigUpdate::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
