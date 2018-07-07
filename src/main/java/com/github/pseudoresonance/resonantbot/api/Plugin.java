package com.github.pseudoresonance.resonantbot.api;

import java.io.File;

import com.github.pseudoresonance.resonantbot.PluginManager;

public abstract class Plugin {

	private boolean isEnabled = false;
	private final ClassLoader classLoader;
	private String name;
	private File folder;
	
	public Plugin() {
		this.name = this.getClass().getSimpleName();
		this.folder = new File(PluginManager.getDir(), name);
		this.classLoader = this.getClass().getClassLoader();
	}

	public abstract void onEnable();

	public abstract void onDisable();

	public final String getName() {
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
	
	public final File getFolder() {
		return this.folder;
	}

	public final ClassLoader getClassLoader() {
		return this.classLoader;
	}
	
	protected final void init(String name, File folder) {
		this.name = name;
		this.folder = folder;
	}

}
