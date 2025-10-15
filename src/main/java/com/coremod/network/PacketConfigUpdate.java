package com.coremod.network;

import com.coremod.blockentity.CoreBlockEntity;
import com.coremod.storage.CoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 更新核心配置的包
 */
public class PacketConfigUpdate {
    private final BlockPos pos;
    private final String coreId;
    private final boolean isPublic;
    private final boolean allowOthers;
    private final boolean burnOverflow;

    public PacketConfigUpdate(BlockPos pos, String coreId, boolean isPublic, boolean allowOthers, boolean burnOverflow) {
        this.pos = pos;
        this.coreId = coreId;
        this.isPublic = isPublic;
        this.allowOthers = allowOthers;
        this.burnOverflow = burnOverflow;
    }

    public PacketConfigUpdate(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.coreId = buf.readUtf();
        this.isPublic = buf.readBoolean();
        this.allowOthers = buf.readBoolean();
        this.burnOverflow = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.coreId);
        buf.writeBoolean(this.isPublic);
        buf.writeBoolean(this.allowOthers);
        buf.writeBoolean(this.burnOverflow);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if (blockEntity instanceof CoreBlockEntity coreBlockEntity) {
                    CoreConfig config = coreBlockEntity.getConfig();
                    
                    // 只有拥有者可以修改配置
                    if (config.getOwnerId().isEmpty() || config.getOwnerId().equals(player.getStringUUID())) {
                        config.setCoreId(coreId);
                        config.setPublic(isPublic);
                        config.setAllowOthers(allowOthers);
                        config.setBurnOverflow(burnOverflow);
                        
                        blockEntity.setChanged();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
