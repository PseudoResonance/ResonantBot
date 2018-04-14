package com.github.pseudoresonance.resonantbot;

import com.github.pseudoresonance.resonantbot.api.Plugin;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public final class PluginClassLoader extends URLClassLoader {
	private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
	private final JarFile jar;
	final Plugin plugin;

	PluginClassLoader(ClassLoader parent, File file) throws IOException, MalformedURLException {
		super(new URL[] { file.toURI().toURL() }, parent);
		this.jar = new JarFile(file);
		Plugin plug = null;
		String mainClass = "";
		String name = "";
		try (InputStream in = getInputStream(file, "plugin.json")) {
			try (JsonReader jr = Json.createReader(in)) {
				JsonObject jo = jr.readObject();
				try {
					mainClass = jo.getString("Main");
				} catch (NullPointerException e) {
					ResonantBot.getLogger().error("No main class in plugin.json in plugin: " + file.getName());
				}
				try {
					name = jo.getString("Name");
				} catch (NullPointerException e) {
					ResonantBot.getLogger().error("No name in plugin.json in plugin: " + file.getName());
				}
				try {
					try {
						Class<?> ex = Class.forName(mainClass, true, this);
						try {
							Class<?> pluginClass = ex.asSubclass(Plugin.class);
							plug = (Plugin) pluginClass.getConstructor().newInstance();
							plug.init(name);
						} catch (ClassCastException exc) {
							ResonantBot.getLogger().error("Class: " + mainClass + " does not extend plugin!", exc);
						}
					} catch (ClassNotFoundException exc) {
						ResonantBot.getLogger().error("Invalid main class: " + mainClass + " in plugin: " + file.getName(), exc);
					}

				} catch (IllegalAccessException e) {
					ResonantBot.getLogger().error("Main class has no public constructor in plugin: " + file.getName(), e);
				} catch (InstantiationException e) {
					ResonantBot.getLogger().error("Abnormal type while loading plugin: " + file.getName(), e);
				} catch (Exception e) {
					ResonantBot.getLogger().error("Error while loading plugin: " + file.getName(), e);
				} catch (OutOfMemoryError e) {
					ResonantBot.getLogger().error("Error while loading plugin: " + file.getName(), e);
					System.exit(1);
				} catch (Error e) {
					ResonantBot.getLogger().error("Error while loading plugin: " + file.getName(), e);
				}
			}
		}
		this.plugin = plug;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return this.findClass(name, true);
	}

	Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
		Class<?> result = (Class<?>) this.classes.get(name);
		if (result == null) {
			if (checkGlobal) {
				result = PluginFileLoader.getClassByName(name);
			}
			if (result == null) {
				if (result == null) {
					result = super.findClass(name);
				}
				if (result != null) {
					PluginFileLoader.setClass(name, result);
				}
			}
			this.classes.put(name, result);
		}
		return result;
	}

	public void close() throws IOException {
		try {
			super.close();
		} finally {
			this.jar.close();
		}
	}

	Set<String> getClasses() {
		return this.classes.keySet();
	}

	static ZipInputStream getInputStream(File zip, String entry) throws IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zip));
		for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
			if (e.getName().equals(entry)) {
				return zin;
			}
		}
		throw new EOFException("Cannot find " + entry);
	}
}