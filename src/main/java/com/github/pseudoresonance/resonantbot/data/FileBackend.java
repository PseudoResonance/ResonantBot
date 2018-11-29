package com.github.pseudoresonance.resonantbot.data;

import java.io.File;

public class FileBackend extends Backend {
	
	private boolean enabled = false;
	
	private final String name;
	private final File folder;

	public FileBackend(String name, File folder) {
		this.name = name;
		this.folder = folder;
	}

	public void setup() {
		enabled = true;
	}

	public void stop() {
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public String getName() {
		return this.name;
	}
	
	public File getFolder() {
		return this.folder;
	}
	
	public boolean equals(Backend obj) {
		if (obj instanceof FileBackend) {
			FileBackend b = (FileBackend) obj;
			if (b.getFolder().equals(this.folder)) {
				return true;
			}
		}
		return false;
	}

}