package com.shnupbups.easyexcavate.mixins;

import com.shnupbups.easyexcavate.EasyExcavateClient;
import com.shnupbups.easyexcavate.EasyExcavate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Block.class)
public abstract class BlockBreakMixin {

	@Inject(at = @At(value="HEAD"), method = "onBreak")
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo cbinfo) {
		if (world.isClient() && (EasyExcavateClient.keybind.isPressed() && !EasyExcavate.reverseBehavior() || !EasyExcavateClient.keybind.isPressed() && EasyExcavate.reverseBehavior()) && player.isUsingEffectiveTool(state) && player.getHungerManager().getFoodLevel() > 0) {
			if (EasyExcavate.blacklistBlocks() != null) {
				if (!Arrays.asList(EasyExcavate.blacklistBlocks()).contains(String.valueOf(state.getBlock()).substring(6, String.valueOf(state.getBlock()).length() - 1)))
					MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(EasyExcavate.createRequestPacket(pos, state.getBlock()));
			} else
				MinecraftClient.getInstance().getNetworkHandler().getClientConnection().sendPacket(EasyExcavate.createRequestPacket(pos, state.getBlock()));
		}
	}
}