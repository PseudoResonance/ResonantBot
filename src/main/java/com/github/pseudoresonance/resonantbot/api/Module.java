package com.github.pseudoresonance.resonantbot.api;

public abstract class Module {
	
	public abstract void onEnable();
	
	public abstract void onDisable();
	
	public abstract String getName();

}
