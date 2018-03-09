package com.github.pseudoresonance.resonantbot.api;

public abstract class Plugin {
	
	public abstract void onEnable();
	
	public abstract void onDisable();
	
	public abstract String getName();

}
