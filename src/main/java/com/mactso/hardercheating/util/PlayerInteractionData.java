package com.mactso.hardercheating.util;

// A simple record or class to hold our counter and timestamp
public class PlayerInteractionData {
	public int interactionCount;
	public long lastInteractionTime;

	public PlayerInteractionData() {
		this.interactionCount = 0;
		this.lastInteractionTime = 0;
	}
}
