package com.github.pseudoresonance.resonantbot.api;

public abstract class Plugin {

	private boolean isEnabled = false;
	private final ClassLoader classLoader;
	private String name;
	
	public Plugin() {
		this.classLoader = this.getClass().getClassLoader();
	}

	public abstract void onEnable();

	public abstract void onDisable();

	public String getName() {
		return this.name;
	}

	public final boolean isEnabled() {
		return this.isEnabled;
	}

	public final void setEnabled(boolean enabled) {
		if (this.isEnabled != enabled) {
			this.isEnabled = enabled;
			if (this.isEnabled) {
				this.onEnable();
			} else {
				this.onDisable();
			}
		}
	}
	
	public void init(String name) {
		this.name = name;
	}

	public final ClassLoader getClassLoader() {
		return this.classLoader;
	}

}
