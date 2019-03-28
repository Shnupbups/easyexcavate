package com.shnupbups.easyexcavate;

public class ExcavateConfig {
	public int maxBlocks;
	public int maxRange;
	public float bonusExhaustionMultiplier;
	public boolean debugOutput;
	public boolean reverseBehavior;

	public ExcavateConfig(int maxBlocks, int maxRange, float bonusExhaustionMultiplier, boolean debugOutput, boolean reverseBehavior) {
		this.maxBlocks = maxBlocks;
		this.maxRange = maxRange;
		this.bonusExhaustionMultiplier = bonusExhaustionMultiplier;
		this.debugOutput = debugOutput;
		this.reverseBehavior = reverseBehavior;
	}

	public ExcavateConfig() {
		this(128, 8, 0.125f, false, false);
	}
}
