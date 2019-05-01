package com.shnupbups.easyexcavate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

public class EasyExcavateClient implements ClientModInitializer {
	public static FabricKeyBinding keybind;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory("easyexcavate.category");
		keybind = FabricKeyBinding.Builder.create(
					new Identifier("easyexcavate:excavate"),
					InputUtil.Type.KEYSYM,
					96,
					"easyexcavate.category"
				).build();
		KeyBindingRegistry.INSTANCE.register(keybind);

		ClientSidePacketRegistry.INSTANCE.register(EasyExcavate.START, (packetContext, packetByteBuf) -> {
			BlockPos pos = null;
			Block block = null;
			float hardness = 0.0f;
			ItemStack tool = null;
			if (packetByteBuf.readBoolean()) {
				pos = packetByteBuf.readBlockPos();
				block = Registry.BLOCK.get(Identifier.createSplit(packetByteBuf.readString(packetByteBuf.readInt()), ':'));
				hardness = packetByteBuf.readFloat();
			}
			if(pos==null||block==null)return;
			if(packetByteBuf.readBoolean()) {
				tool = packetByteBuf.readItemStack();
			}
			int maxBlocks = packetByteBuf.readInt();
			int maxRange = packetByteBuf.readInt();
			float bonusExhaustionMultiplier = packetByteBuf.readFloat();
			boolean enableBlockEntities = packetByteBuf.readBoolean();
			int blacklistBlocksLength = packetByteBuf.readInt();
			String[] blacklistBlocks = new String[blacklistBlocksLength];
			if (blacklistBlocksLength != 0) {
				for (int i = 0; i < blacklistBlocksLength; i++) {
					blacklistBlocks[i] = packetByteBuf.readString(packetByteBuf.readInt());
				}
			}
			int blacklistToolsLength = packetByteBuf.readInt();
			String[] blacklistTools = new String[blacklistToolsLength];
			if (blacklistToolsLength != 0) {
				for (int j = 0; j < blacklistToolsLength; j++) {
					blacklistTools[j] = packetByteBuf.readString(packetByteBuf.readInt());
				}
			}
			boolean checkHardness = packetByteBuf.readBoolean();
			boolean isToolRequired = packetByteBuf.readBoolean();
			boolean invertBlockBlacklist = packetByteBuf.readBoolean();
			boolean invertToolBlacklist = packetByteBuf.readBoolean();
			PlayerEntity player = packetContext.getPlayer();
			World world = player.getEntityWorld();
			int blocksBroken = 1;
			ArrayList<BlockPos> brokenPos = new ArrayList<>();
			brokenPos.add(pos);
			BlockPos currentPos = pos;
			ArrayList<BlockPos> nextPos = new ArrayList<>();
			EasyExcavate.debugOut(pos+ " be: "+world.getBlockEntity(pos));
			float exhaust = 0;
			ExcavateConfig serverConfig = new ExcavateConfig(maxBlocks, maxRange, bonusExhaustionMultiplier, EasyExcavate.debug(), enableBlockEntities, EasyExcavate.reverseBehavior(), blacklistBlocks, blacklistTools, checkHardness, isToolRequired, invertBlockBlacklist, invertToolBlacklist);
			EasyExcavate.debugOut("Start packet recieved! "+serverConfig.toString());
			while (blocksBroken < maxBlocks && player.isUsingEffectiveTool(block.getDefaultState()) && player.getHungerManager().getFoodLevel()>exhaust/2 && (!(block instanceof BlockWithEntity) || enableBlockEntities)) {
				if(
						(Arrays.asList(blacklistBlocks).contains(Registry.BLOCK.getId(block).toString()) && !invertBlockBlacklist) ||
						(!Arrays.asList(blacklistBlocks).contains(Registry.BLOCK.getId(block).toString()) && invertBlockBlacklist) ||
						(tool!=null && Arrays.asList(blacklistTools).contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) && !invertToolBlacklist) ||
						(tool!=null && !Arrays.asList(blacklistTools).contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) && invertToolBlacklist)
				) break;
				ArrayList<BlockPos> neighbours = getSameNeighbours(world,currentPos,block);
				neighbours.removeAll(brokenPos);
				if(neighbours.size()>=1) {
					for(BlockPos p:neighbours) {
						if(
								blocksBroken>=maxBlocks ||
								!player.isUsingEffectiveTool(block.getDefaultState()) ||
								player.getHungerManager().getFoodLevel()<=exhaust/2 ||
								Arrays.asList(blacklistBlocks).contains(Registry.BLOCK.getId(block).toString()) && !invertBlockBlacklist ||
								!Arrays.asList(blacklistBlocks).contains(Registry.BLOCK.getId(block).toString()) && invertBlockBlacklist ||
								(tool!=null && Arrays.asList(blacklistTools).contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) && !invertToolBlacklist) ||
								(tool!=null && !Arrays.asList(blacklistTools).contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) && invertToolBlacklist) ||
								(block instanceof BlockWithEntity && !enableBlockEntities) ||
								((tool==null||!tool.getItem().canDamage())&&isToolRequired) ||
								(tool.getItem().canDamage()&&blocksBroken>=(tool.getDurability()-tool.getDamage()))
						) break;
						if(!brokenPos.contains(p)&&player.isUsingEffectiveTool(world.getBlockState(p))&&(!checkHardness||world.getBlockState(p).getHardness(world,p)<=hardness)) {
							if(Math.sqrt(p.getSquaredDistance(pos))<=maxRange)nextPos.add(p);
							MinecraftClient.getInstance().getNetworkHandler().getClientConnection().send(EasyExcavate.createBreakPacket(p));
							brokenPos.add(p);
							blocksBroken++;
							exhaust = (0.005F*blocksBroken)*((blocksBroken*bonusExhaustionMultiplier)+1);
						}
					}
				}
				if(nextPos.size()>=1) {
					currentPos = nextPos.get(0);
					nextPos.remove(currentPos);
				} else break;
			}
			if(!player.isCreative()) {
				MinecraftClient.getInstance().getNetworkHandler().getClientConnection().send(EasyExcavate.createEndPacket(blocksBroken));
				EasyExcavate.debugOut("End packet sent! blocks broken: "+blocksBroken);
			}
		});
	}

	private ArrayList<BlockPos> getSameNeighbours(World world, BlockPos pos, Block block) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		for(int x=-1; x<=1; x++) {
			for(int y=-1; y<=1; y++) {
				for(int z=-1; z<=1; z++) {
					if(!(x==0&&y==0&&z==0)&&world.getBlockState(pos.add(x,y,z)).getBlock().equals(block)) {
						list.add(pos.add(x,y,z));
					}
				}
			}
		}
		return list;
	}
}
