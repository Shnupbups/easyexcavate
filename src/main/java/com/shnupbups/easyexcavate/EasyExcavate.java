package com.shnupbups.easyexcavate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EasyExcavate implements ModInitializer {

	public static ExcavateConfig config;

	public static final Identifier REQUEST_CONFIG = new Identifier("easyexcavate", "request_config");
	public static final Identifier START = new Identifier("easyexcavate", "start");
	public static final Identifier END = new Identifier("easyexcavate", "end");
	public static final Identifier BREAK_BLOCK = new Identifier("easyexcavate", "break_block");

	@Override
	public void onInitialize() {
		File configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "easyexcavate.json");
		try (FileReader reader = new FileReader(configFile)) {
			config = new Gson().fromJson(reader, ExcavateConfig.class);
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
			} catch (IOException e2) {
				System.out.println("[EasyExcavate] Failed to update config file!");
			}
			System.out.println("[EasyExcavate] Config loaded!");
			debugOut("[EasyExcavate] Debug Output enabled! "+config.toString());
		} catch (IOException e) {
			System.out.println("[EasyExcavate] No config found, generating!");
			config = new ExcavateConfig();
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
			} catch (IOException e2) {
				System.out.println("[EasyExcavate] Failed to generate config file!");
			}
		}

		ServerSidePacketRegistry.INSTANCE.register(REQUEST_CONFIG, (packetContext, packetByteBuf) -> {
			BlockPos pos = null;
			Block block = null;
			float hardness = 0.0f;
			ItemStack tool = null;
			if (packetByteBuf.readBoolean()) {
				pos = packetByteBuf.readBlockPos();
				block = Registry.BLOCK.get(Identifier.splitOn(packetByteBuf.readString(packetByteBuf.readInt()), ':'));
				hardness = packetByteBuf.readFloat();
			}
			if(pos==null||block==null)return;
			if (packetByteBuf.readBoolean()) {
				tool = packetByteBuf.readItemStack();
			}
			debugOut("Config request packet recieved! pos: " + pos + " block: " + block + " hardness: " + hardness + " tool: " + tool);
			ServerPlayerEntity player = (ServerPlayerEntity) packetContext.getPlayer();
			player.networkHandler.sendPacket(createStartPacket(pos, block, hardness, tool, config));
			debugOut("Start packet sent! pos: " + pos + " block: " + block + " hardness: " + hardness + " tool: " + tool + " " + config.toString());
		});

		ServerSidePacketRegistry.INSTANCE.register(BREAK_BLOCK, (packetContext, packetByteBuf) -> {
			BlockPos pos = null;
			if (packetByteBuf.readBoolean()) {
				pos = packetByteBuf.readBlockPos();
			}
			if(pos==null)return;
			PlayerEntity player = packetContext.getPlayer();
			World world = player.getEntityWorld();
			ItemStack stack = player.getMainHandStack();
			BlockState state = world.getBlockState(pos);
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof Inventory) {
				debugOut(((Inventory)blockEntity).isInvEmpty());
				ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
			}
			if(!config.dontTakeDurability)stack.onBlockBroken(world, state, pos, player);
			if (!player.isCreative()) {
				state.getBlock().afterBreak(world, player, pos, state, world.getBlockEntity(pos), stack.copy());
			}
			world.breakBlock(pos,false);
		});

		ServerSidePacketRegistry.INSTANCE.register(END, (packetContext, packetByteBuf) -> {
			int blocksBroken = packetByteBuf.readInt();
			PlayerEntity player = packetContext.getPlayer();
			float exhaust = (0.005F*blocksBroken)*(blocksBroken*config.bonusExhaustionMultiplier);
			player.addExhaustion(exhaust);
			debugOut("End packet recieved! blocks broken: "+blocksBroken+" exhaust: "+exhaust+" actual exhaust: "+(exhaust+0.005F*blocksBroken));
		});
	}

	public static CustomPayloadC2SPacket createRequestPacket(BlockPos pos, Block block, float hardness, ItemStack tool) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(pos != null && block != null);
		if (pos != null && block != null) {
			buf.writeBlockPos(pos);
			String s = Registry.BLOCK.getId(block).toString();
			buf.writeInt(s.length());
			buf.writeString(s);
			buf.writeFloat(hardness);
		}
		buf.writeBoolean(tool!=null);
		if(tool != null) {
			buf.writeItemStack(tool);
		}
		return new CustomPayloadC2SPacket(REQUEST_CONFIG, buf);
	}

	public static CustomPayloadS2CPacket createStartPacket(BlockPos pos, Block block, float hardness, ItemStack tool, ExcavateConfig serverConfig) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(pos != null && block != null);
		if (pos != null && block != null) {
			buf.writeBlockPos(pos);
			String s = Registry.BLOCK.getId(block).toString();
			buf.writeInt(s.length());
			buf.writeString(s);
			buf.writeFloat(hardness);
		}
		buf.writeBoolean(tool!=null);
		if(tool != null) {
			buf.writeItemStack(tool);
		}
		serverConfig.writeConfig(buf);
		return new CustomPayloadS2CPacket(START, buf);
	}

	public static CustomPayloadC2SPacket createEndPacket(int blocksBroken) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(blocksBroken);
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

	public static String debugOut(String out) {
		if(config.debugOutput) System.out.println(out);
		return out;
	}
	public static Object debugOut(Object out) {
		debugOut(out.toString());
		return out;
	}

	public static boolean reverseBehavior() {
		return config.reverseBehavior;
	}
	public static boolean debug() {
		return config.debugOutput;
	}
}
