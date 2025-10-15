package com.coremod.network;

import com.coremod.menu.CoreMenu;
import com.coremod.storage.CoreStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端请求存储数据的包
 */
public class PacketStorageRequest {
    private final String coreId;

    public PacketStorageRequest(String coreId) {
        this.coreId = coreId;
    }

    public PacketStorageRequest(FriendlyByteBuf buf) {
        this.coreId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.coreId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                CoreStorage storage = CoreStorage.get(serverLevel);
                
                // 发送存储数据给客户端
                NetworkHandler.sendToPlayer(new PacketStorageSync(coreId, storage.getItems(coreId)), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
