package com.github.pseudoresonance.resonantbot.data;

public abstract class Backend {
	
	public abstract void setup();
	
	public abstract void stop();
	
	public abstract boolean isEnabled();
	
	public abstract String getName();
	
	public abstract boolean equals(Backend obj);

}
