package com.shnupbups.easyexcavate;

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

	public ExcavateConfig(int maxBlocks, int maxRange, float bonusExhaustionMultiplier, boolean debugOutput, boolean enableBlockEntities, boolean reverseBehavior, String[] blacklistBlocks, String[] blacklistTools, boolean checkHardness, boolean isToolRequired, boolean invertBlockBlacklist, boolean invertToolBlacklist) {
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
		updateConfig();
	}

	public ExcavateConfig() {
		this(128, 8, 0.125f, false, false, false, new String[]{"minecraft:example_block","somemod:example_block_two"},new String[]{"minecraft:example_pickaxe","somemod:example_axe"},false,false,false,false);
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
		return "maxB: "+maxBlocks+" maxR: "+maxRange+" bem: "+bonusExhaustionMultiplier+" ebe: "+enableBlockEntities+" blackB: "+Arrays.asList(blacklistBlocks)+" blackT: "+Arrays.asList(blacklistTools)+" checkH: "+checkHardness+" itr: "+isToolRequired+" invBB: "+invertBlockBlacklist+" invTB: "+invertToolBlacklist;
	}
}
