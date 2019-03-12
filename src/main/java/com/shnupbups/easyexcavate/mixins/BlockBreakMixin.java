package com.shnupbups.easyexcavate.mixins;

import com.shnupbups.easyexcavate.ClientKeybind;
import com.shnupbups.easyexcavate.EasyExcavate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;


@Mixin(Block.class)
public abstract class BlockBreakMixin {

	private static final int excavateMaxBlocks = 125;
	private static final int excavateRange = 7;

	@Inject(at = @At(value="HEAD"), method = "onBreak")
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo cbinfo) {
		if(world.isClient()&&ClientKeybind.keybind.isPressed()&&player.isUsingEffectiveTool(state)&&player.getHungerManager().getFoodLevel()>0) {
			int blocksBroken = 1;
			ArrayList<BlockPos> brokenPos = new ArrayList<BlockPos>();
			brokenPos.add(pos);
			BlockPos currentPos = pos;
			ArrayList<BlockPos> nextPos = new ArrayList<BlockPos>();
			float exhaust = 0;
			while (blocksBroken < excavateMaxBlocks && player.isUsingEffectiveTool(state) && player.getHungerManager().getFoodLevel()>exhaust/2 && blocksBroken<(player.getMainHandStack().getDurability()-player.getMainHandStack().getDamage())) {
				ArrayList<BlockPos> neighbours = getSameNeighbours(world,currentPos,state);
				neighbours.removeAll(brokenPos);
				if(neighbours.size()>=1) {
					for(BlockPos p:neighbours) {
						if(blocksBroken>=excavateMaxBlocks||!player.isUsingEffectiveTool(state)||player.getHungerManager().getFoodLevel()<=exhaust/2||blocksBroken>=(player.getMainHandStack().getDurability()-player.getMainHandStack().getDamage())) {
							break;
						}
						if(!brokenPos.contains(p)&&player.isUsingEffectiveTool(world.getBlockState(p))) {
							if(p.distanceTo(pos)<=excavateRange)nextPos.add(p);
							world.breakBlock(p, !player.isCreative());
							MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(EasyExcavate.createBreakPacket(p));
							brokenPos.add(p);
							blocksBroken++;
							exhaust = (0.005F*blocksBroken)*((blocksBroken/8)+1);
						}
					}
				}
				if(nextPos.size()>=1) {
					currentPos = nextPos.get(0);
					nextPos.remove(currentPos);
				} else break;
			}
			if(!player.isCreative()) {
				MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(EasyExcavate.createEndPacket(blocksBroken-1, exhaust));
			}
			//System.out.println("broken: "+blocksBroken+" exhaust: "+exhaust+" durability:"+(player.getMainHandStack().getDurability()-player.getMainHandStack().getDamage()));
		}
	}

	private ArrayList<BlockPos> getSameNeighbours(World world, BlockPos pos, BlockState state) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		for(int x=-1; x<=1; x++) {
			for(int y=-1; y<=1; y++) {
				for(int z=-1; z<=1; z++) {
					if(!(x==0&&y==0&&z==0)&&world.getBlockState(pos.add(x,y,z)).getBlock().equals(state.getBlock())) {
						list.add(pos.add(x,y,z));
					}
				}
			}
		}
		return list;
	}
}
