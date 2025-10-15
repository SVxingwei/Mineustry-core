package com.coremod.network;

import com.coremod.storage.CoreStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端请求提取物品的包
 */
public class PacketExtractItem {
    private final String coreId;
    private final String itemKey;
    private final int amount;

    public PacketExtractItem(String coreId, String itemKey, int amount) {
        this.coreId = coreId;
        this.itemKey = itemKey;
        this.amount = amount;
    }

    public PacketExtractItem(FriendlyByteBuf buf) {
        this.coreId = buf.readUtf();
        this.itemKey = buf.readUtf();
        this.amount = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.coreId);
        buf.writeUtf(this.itemKey);
        buf.writeInt(this.amount);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                CoreStorage storage = CoreStorage.get(serverLevel);
                
                ItemStack extracted = storage.extractItem(coreId, itemKey, amount, false);
                if (!extracted.isEmpty()) {
                    // 将物品给玩家
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                }
                
                // 发送更新的存储数据
                NetworkHandler.sendToPlayer(new PacketStorageSync(coreId, storage.getItems(coreId)), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
