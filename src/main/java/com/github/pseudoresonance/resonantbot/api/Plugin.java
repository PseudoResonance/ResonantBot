package com.github.pseudoresonance.resonantbot.api;

import java.io.File;

public abstract class Plugin {

	private boolean isEnabled = false;
	private final ClassLoader classLoader;
	private String name = "";
	private String version = "";
	private File folder = new File("");
	
	public Plugin() {
		this.classLoader = this.getClass().getClassLoader();
	}

	public abstract void onEnable();

	public abstract void onDisable();

	public final String getName() {
		return this.name;
	}

	public final String getVersion() {
		return this.version;
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
	
	public final File getFolder() {
		return this.folder;
	}

	public final ClassLoader getClassLoader() {
		return this.classLoader;
	}

}
