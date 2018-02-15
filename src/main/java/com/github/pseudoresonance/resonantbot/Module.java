package com.github.pseudoresonance.resonantbot;

public abstract class Module {
	
	public abstract void onEnable();
	
	public abstract void onDisable();
	
	public abstract String getName();

}
