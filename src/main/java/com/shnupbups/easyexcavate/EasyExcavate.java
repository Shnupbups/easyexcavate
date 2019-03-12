package com.shnupbups.easyexcavate;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EasyExcavate implements ModInitializer {
	private static final int excavateMaxBlocks = 125;
	private static final int excavateRange = 7;

	public static final Identifier END = new Identifier("easyexcavate", "end");
	public static final Identifier BREAK_BLOCK = new Identifier("easyexcavate", "break_block");

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(BREAK_BLOCK, (packetContext, packetByteBuf) -> {
			BlockPos pos = null;
			if (packetByteBuf.readBoolean()) {
				pos = packetByteBuf.readBlockPos();
			}
			if(pos==null)return;
			PlayerEntity player = packetContext.getPlayer();
			World world = player.getEntityWorld();
			world.breakBlock(pos,!player.isCreative());
		});

		ServerSidePacketRegistry.INSTANCE.register(END, (packetContext, packetByteBuf) -> {
			int damage = 0;
			float exhaust = 0.0f;
			damage = packetByteBuf.readInt();
			exhaust = packetByteBuf.readFloat();
			PlayerEntity player = packetContext.getPlayer();
			player.getMainHandStack().applyDamage(damage, player);
			player.addExhaustion(exhaust);
		});
	}

	public static CustomPayloadC2SPacket createEndPacket(int damage, float exhaust) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(damage);
		buf.writeFloat(exhaust);
		return new CustomPayloadC2SPacket(END, buf);
	}

	public static CustomPayloadC2SPacket createBreakPacket(BlockPos pos) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(pos != null);
		if (pos != null) {
			buf.writeBlockPos(pos);
		}
		return new CustomPayloadC2SPacket(BREAK_BLOCK, buf);
	}
}
