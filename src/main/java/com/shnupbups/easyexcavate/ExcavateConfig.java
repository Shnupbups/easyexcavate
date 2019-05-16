package com.shnupbups.easyexcavate;

import net.minecraft.util.PacketByteBuf;

import java.util.Arrays;

public class ExcavateConfig {
	public int maxBlocks;
	public int maxRange;
	public float bonusExhaustionMultiplier;
	public boolean debugOutput;
	public boolean enableBlockEntities;
	public boolean reverseBehavior;
	public String[] blacklistBlocks;
	public String[] blacklistTools;
	public boolean checkHardness;
	public boolean isToolRequired;
	public boolean invertBlockBlacklist;
	public boolean invertToolBlacklist;
	public boolean dontTakeDurability;

	public ExcavateConfig(int maxBlocks, int maxRange, float bonusExhaustionMultiplier, boolean debugOutput, boolean enableBlockEntities, boolean reverseBehavior, String[] blacklistBlocks, String[] blacklistTools, boolean checkHardness, boolean isToolRequired, boolean invertBlockBlacklist, boolean invertToolBlacklist, boolean dontTakeDurability) {
		this.maxBlocks = maxBlocks;
		this.maxRange = maxRange;
		this.bonusExhaustionMultiplier = bonusExhaustionMultiplier;
		this.debugOutput = debugOutput;
		this.enableBlockEntities = enableBlockEntities;
		this.reverseBehavior = reverseBehavior;
		this.blacklistBlocks = blacklistBlocks;
		this.blacklistTools = blacklistTools;
		this.checkHardness = checkHardness;
		this.isToolRequired = isToolRequired;
		this.invertBlockBlacklist = invertBlockBlacklist;
		this.invertToolBlacklist = invertToolBlacklist;
		this.dontTakeDurability = dontTakeDurability;
		updateConfig();
	}

	public ExcavateConfig() {
		this(128, 8, 0.125f, false, false, false, new String[]{"minecraft:example_block","somemod:example_block_two"},new String[]{"minecraft:example_pickaxe","somemod:example_axe"},false,false,false,false,false);
	}

	public void updateConfig() {
		if(blacklistBlocks==null||blacklistBlocks.length==0) {
			blacklistBlocks=new String[]{"minecraft:example_block","somemod:example_block_two"};
		}
		if(blacklistTools==null||blacklistTools.length==0) {
			blacklistTools=new String[]{"minecraft:example_pickaxe","somemod:example_axe"};
		}
	}

	public String toString() {
		return "maxB: "+maxBlocks+" maxR: "+maxRange+" bem: "+bonusExhaustionMultiplier+" ebe: "+enableBlockEntities+" blackB: "+Arrays.asList(blacklistBlocks)+" blackT: "+Arrays.asList(blacklistTools)+" checkH: "+checkHardness+" itr: "+isToolRequired+" invBB: "+invertBlockBlacklist+" invTB: "+invertToolBlacklist+" dTD: "+dontTakeDurability;
	}

	public PacketByteBuf writeConfig(PacketByteBuf buf) {
		return writeConfig(buf, this);
	}

	public static PacketByteBuf writeConfig(PacketByteBuf buf, ExcavateConfig config) {
		buf.writeInt(config.maxBlocks);
		buf.writeInt(config.maxRange);
		buf.writeFloat(config.bonusExhaustionMultiplier);
		buf.writeBoolean(config.enableBlockEntities);
		if (config.blacklistBlocks != null && config.blacklistBlocks.length > 0) {
			buf.writeInt(config.blacklistBlocks.length);
			for (String s: config.blacklistBlocks) {
				buf.writeInt(s.length());
				buf.writeString(s);
			}
		} else
			buf.writeInt(0);
		if (config.blacklistTools != null && config.blacklistTools.length > 0) {
			buf.writeInt(config.blacklistTools.length);
			for (String s: config.blacklistTools) {
				buf.writeInt(s.length());
				buf.writeString(s);
			}
		} else
			buf.writeInt(0);
		buf.writeBoolean(config.checkHardness);
		buf.writeBoolean(config.isToolRequired);
		buf.writeBoolean(config.invertBlockBlacklist);
		buf.writeBoolean(config.invertToolBlacklist);
		buf.writeBoolean(config.dontTakeDurability);
		return buf;
	}

	public static ExcavateConfig readConfig(PacketByteBuf buf) {
		int maxBlocks = buf.readInt();
		int maxRange = buf.readInt();
		float bonusExhaustionMultiplier = buf.readFloat();
		boolean enableBlockEntities = buf.readBoolean();
		int blacklistBlocksLength = buf.readInt();
		String[] blacklistBlocks = new String[blacklistBlocksLength];
		if (blacklistBlocksLength != 0) {
			for (int i = 0; i < blacklistBlocksLength; i++) {
				blacklistBlocks[i] = buf.readString(buf.readInt());
			}
		}
		int blacklistToolsLength = buf.readInt();
		String[] blacklistTools = new String[blacklistToolsLength];
		if (blacklistToolsLength != 0) {
			for (int j = 0; j < blacklistToolsLength; j++) {
				blacklistTools[j] = buf.readString(buf.readInt());
			}
		}
		boolean checkHardness = buf.readBoolean();
		boolean isToolRequired = buf.readBoolean();
		boolean invertBlockBlacklist = buf.readBoolean();
		boolean invertToolBlacklist = buf.readBoolean();
		boolean dontTakeDurability = buf.readBoolean();
		return new ExcavateConfig(maxBlocks,maxRange,bonusExhaustionMultiplier,EasyExcavate.debug(),enableBlockEntities,EasyExcavate.reverseBehavior(),blacklistBlocks,blacklistTools,checkHardness,isToolRequired,invertBlockBlacklist,invertToolBlacklist,dontTakeDurability);
	}

	public boolean equals(ExcavateConfig config) {
		return (
				config.maxBlocks==maxBlocks&&
				config.maxRange==maxRange&&
				config.bonusExhaustionMultiplier==bonusExhaustionMultiplier&&
				config.enableBlockEntities==enableBlockEntities&&
				Arrays.equals(config.blacklistBlocks,blacklistBlocks)&&
				Arrays.equals(config.blacklistTools,blacklistTools)&&
				config.checkHardness==checkHardness&&
				config.isToolRequired==isToolRequired&&
				config.invertBlockBlacklist==invertBlockBlacklist&&
				config.invertToolBlacklist==invertToolBlacklist&&
				config.dontTakeDurability==dontTakeDurability
		);
	}
}
