package com.shnupbups.easyexcavate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;

public class EasyExcavateClient implements ClientModInitializer {
	public static FabricKeyBinding keybind;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory("easyexcavate.category");
		keybind = FabricKeyBinding.Builder.create(
					new Identifier("easyexcavate:excavate"),
					InputUtil.Type.KEY_KEYBOARD,
					96,
					"easyexcavate.category"
				).build();
		KeyBindingRegistry.INSTANCE.register(keybind);

		ClientSidePacketRegistry.INSTANCE.register(EasyExcavate.START, (packetContext, packetByteBuf) -> {
			BlockPos pos = null;
			Block block = null;
			if (packetByteBuf.readBoolean()) {
				pos = packetByteBuf.readBlockPos();
				block = Registry.BLOCK.get(Identifier.createSplit(packetByteBuf.readString(32767), ':'));
			}
			if(pos==null||block==null)return;
			int maxBlocks = packetByteBuf.readInt();
			int maxRange = packetByteBuf.readInt();
			float bonusExhaustionMultiplier = packetByteBuf.readFloat();
			PlayerEntity player = packetContext.getPlayer();
			World world = player.getEntityWorld();
			int blocksBroken = 1;
			ArrayList<BlockPos> brokenPos = new ArrayList<>();
			brokenPos.add(pos);
			BlockPos currentPos = pos;
			ArrayList<BlockPos> nextPos = new ArrayList<>();
			float exhaust = 0;
			EasyExcavate.debugOut("Start packet recieved! pos: "+pos+" block: "+block+" maxB: "+maxBlocks+" maxR: "+maxRange+" bem: "+bonusExhaustionMultiplier);
			while (blocksBroken < maxBlocks && player.isUsingEffectiveTool(block.getDefaultState()) && player.getHungerManager().getFoodLevel()>exhaust/2 && blocksBroken<(player.getMainHandStack().getDurability()-player.getMainHandStack().getDamage())) {
				ArrayList<BlockPos> neighbours = getSameNeighbours(world,currentPos,block);
				neighbours.removeAll(brokenPos);
				if(neighbours.size()>=1) {
					for(BlockPos p:neighbours) {
						if(blocksBroken>=maxBlocks||!player.isUsingEffectiveTool(block.getDefaultState())||player.getHungerManager().getFoodLevel()<=exhaust/2||blocksBroken>=(player.getMainHandStack().getDurability()-player.getMainHandStack().getDamage())) {
							break;
						}
						if(!brokenPos.contains(p)&&player.isUsingEffectiveTool(world.getBlockState(p))) {
							if(Math.sqrt(p.squaredDistanceTo(pos))<=maxRange)nextPos.add(p);
							MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(EasyExcavate.createBreakPacket(p));
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
				MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(EasyExcavate.createEndPacket(blocksBroken));
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
