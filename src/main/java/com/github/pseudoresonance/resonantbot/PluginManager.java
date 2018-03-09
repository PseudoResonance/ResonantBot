package com.github.pseudoresonance.resonantbot;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.github.pseudoresonance.resonantbot.api.Plugin;

import sx.blah.discord.handle.obj.IChannel;

public class PluginManager {
	
	private static File dir = new File(ResonantBot.getDir(), "plugins");
	
	private static DualHashBidiMap<File, Plugin> plugins = new DualHashBidiMap<File, Plugin>();
	
	public static void reload() {
		for (Plugin mod : plugins.values()) {
			mod.onDisable();
			plugins.removeValue(mod);
		}
		plugins.clear();
		File[] newFiles = dir.listFiles();
		for (File f : newFiles) {
			if (f.getName().endsWith(".jar")) {
				ResonantBot.getLogger().info("Found plugin jar: " + f.getName());
				load(f, false);
			}
		}
		for (Plugin m : plugins.values()) {
			try {
				m.onEnable();
			} catch (Exception e) {
				ResonantBot.getLogger().error("Error while enabling plugin: " + m.getName(), e);
			}
		}
	}
	
	public static String load(File f, boolean enable) {
		ResonantBot.getLogger().info("Loading plugin jar: " + f.getName());
		String mainClass = "";
		String name = "";
		String error = "";
		Plugin plugin = null;
		if (f.exists()) {
			ZipInputStream in = null;
			JsonReader jr = null;
			URLClassLoader loader = null;
			try {
				URL url = new URL("jar", "","file:" + f.getAbsolutePath() + "!/");
				in = getInputStream(f, "plugin.json");
				jr = Json.createReader(in);
				JsonObject jo = jr.readObject();
				try {
					mainClass = jo.getString("Main");
				} catch (NullPointerException e) {
					ResonantBot.getLogger().error("No main class in plugin.json in plugin: " + f.getName());
					error = "No main class in plugin.json in plugin: " + f.getName();
				}
				try {
					name = jo.getString("Name");
				} catch (NullPointerException e) {
					ResonantBot.getLogger().error("No name in plugin.json in plugin: " + f.getName());
					error = "No name in plugin.json in plugin: " + f.getName();
				}
				loader = new URLClassLoader(new URL[] {url}, ResonantBot.class.getClassLoader());
				Class<?> clazz = loader.loadClass(mainClass);
				if (Plugin.class.isAssignableFrom(clazz)) {
					ResonantBot.getLogger().info("Initializing plugin: " + name + " in jar: " + f.getName());
					plugin = (Plugin) clazz.getConstructors()[0].newInstance();
					plugins.put(f, plugin);
				} else {
					ResonantBot.getLogger().error("Class: " + mainClass + " does not extend plugin!");
					error = "Class: " + mainClass + " does not extend plugin!";
				}
			} catch (EOFException e) {
				ResonantBot.getLogger().error("No plugin.json in plugin: " + f.getName());
				error = "No plugin.json in plugin: " + f.getName();
			} catch (IOException e) {
				ResonantBot.getLogger().error("Failed to load plugin: " + f.getName(), e);
				error = "Failed to load plugin: " + f.getName();
			} catch (ClassNotFoundException e) {
				ResonantBot.getLogger().error("Invalid main class: " + mainClass + " in plugin: " + f.getName(), e);
				error = "Invalid main class: " + mainClass + " in plugin: " + f.getName();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				ResonantBot.getLogger().error("Main class has incorrect constructor in plugin: " + f.getName(), e);
				error = "Main class has incorrect constructor in plugin: " + f.getName();
			} catch (Exception e) {
				ResonantBot.getLogger().error("Error while loading plugin: " + f.getName(), e);
				error = "Error while loading plugin: " + f.getName();
			} catch (OutOfMemoryError e) {
				ResonantBot.getLogger().error("Error while loading plugin: " + f.getName(), e);
				error = "Error while loading plugin: " + f.getName();
				System.exit(1);
			} catch (Error e) {
				ResonantBot.getLogger().error("Error while loading plugin: " + f.getName(), e);
				error = "Error while loading plugin: " + f.getName();
			}finally {
				try {
					jr.close();
				} catch (NullPointerException e) {
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
				}
				try {
					loader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
				}
			}
		} else
			error = "File: " + f.getName() + " does not exist!";
		if (error != "")
			ResonantBot.getLogger().info("Failed to initialize plugin: " + name + " in jar: " + f.getName());
		else {
			ResonantBot.getLogger().info("Completed initializing plugin: " + name + " in jar: " + f.getName());
			error = "Completed initializing plugin: " + name;
		}
		if (enable) {
			if (plugin != null) {
				try {
					plugin.onEnable();
				} catch (Exception e) {
					ResonantBot.getLogger().error("Error while enabling plugin: " + plugin.getName(), e);
				}
			}
		}
		return error;
	}
	
	public static void unload(Plugin mod) {
		if (plugins.containsValue(mod)) {
			File f = plugins.getKey(mod);
			mod.onDisable();
			CommandManager.unregisterPluginCommands(mod);
			ResonantBot.getLogger().info("Unloaded plugin: " + mod.getName() + " in jar: " + f.getName());
			plugins.remove(mod);
			mod = null;
		}
	}
	
	public static void reload(File f, IChannel chan) {
		unload(plugins.get(f));
		String result = load(f, true);
		BotUtils.sendMessage(chan, result);
	}
	
	public static File getFile(Plugin mod) {
		return plugins.getKey(mod);
	}
	
	public static Plugin getPlugin(File f) {
		return plugins.get(f);
	}
	
	public static Set<Plugin> getPlugins() {
		return plugins.values();
	}
	
	public static File getDir() {
		return dir;
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
