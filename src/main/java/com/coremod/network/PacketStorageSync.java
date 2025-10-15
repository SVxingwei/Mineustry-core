package com.coremod.network;

import com.coremod.client.screen.CoreScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 服务器发送存储数据给客户端的包
 */
public class PacketStorageSync {
    private final String coreId;
    private final Map<String, ItemStack> items;

    public PacketStorageSync(String coreId, Map<String, ItemStack> items) {
        this.coreId = coreId;
        this.items = items;
    }

    public PacketStorageSync(FriendlyByteBuf buf) {
        this.coreId = buf.readUtf();
        this.items = new HashMap<>();
        
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            ItemStack stack = buf.readItem();
            items.put(key, stack);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.coreId);
        buf.writeInt(this.items.size());
        
        for (Map.Entry<String, ItemStack> entry : this.items.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeItem(entry.getValue());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof CoreScreen coreScreen) {
                coreScreen.updateStorageItems(this.items);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
