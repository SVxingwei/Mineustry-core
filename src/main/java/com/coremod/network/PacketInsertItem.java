package com.coremod.network;

import com.coremod.storage.CoreStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端请求将手持物品插入核心存储的包
 */
public class PacketInsertItem {
    private final String coreId;
    private final int slotIndex; // 玩家背包槽位索引

    public PacketInsertItem(String coreId, int slotIndex) {
        this.coreId = coreId;
        this.slotIndex = slotIndex;
    }

    public PacketInsertItem(FriendlyByteBuf buf) {
        this.coreId = buf.readUtf();
        this.slotIndex = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.coreId);
        buf.writeInt(this.slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                CoreStorage storage = CoreStorage.get(serverLevel);
                
                // 获取玩家背包中的物品
                ItemStack stackInSlot = player.getInventory().getItem(slotIndex);
                
                if (!stackInSlot.isEmpty()) {
                    // 尝试插入到核心存储
                    ItemStack remainder = storage.insertItem(coreId, stackInSlot.copy(), false);
                    
                    // 更新玩家背包
                    player.getInventory().setItem(slotIndex, remainder);
                    
                    // 发送更新的存储数据
                    NetworkHandler.sendToPlayer(new PacketStorageSync(coreId, storage.getItems(coreId)), player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
