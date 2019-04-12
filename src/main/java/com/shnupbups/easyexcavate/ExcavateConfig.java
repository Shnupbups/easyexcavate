package com.shnupbups.easyexcavate;

public class ExcavateConfig {
	public int maxBlocks;
	public int maxRange;
	public float bonusExhaustionMultiplier;
	public boolean debugOutput;
	public boolean enableBlockEntities;
	public boolean reverseBehavior;
	public String[] blacklistBlocks;

	public ExcavateConfig(int maxBlocks, int maxRange, float bonusExhaustionMultiplier, boolean debugOutput, boolean enableBlockEntities, boolean reverseBehavior, String[] blacklistBlocks) {
		this.maxBlocks = maxBlocks;
		this.maxRange = maxRange;
		this.bonusExhaustionMultiplier = bonusExhaustionMultiplier;
		this.debugOutput = debugOutput;
		this.enableBlockEntities = enableBlockEntities;
		this.reverseBehavior = reverseBehavior;
		this.blacklistBlocks = blacklistBlocks;
		updateConfig();
	}

	public ExcavateConfig() {
		this(128, 8, 0.125f, false, false, false, new String[]{"minecraft:example"});
	}

	public void updateConfig() {
		if(blacklistBlocks==null) {
			blacklistBlocks=new String[]{"minecraft:example"};
		}
	}
}
