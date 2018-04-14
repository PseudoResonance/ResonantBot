package com.github.pseudoresonance.resonantbot;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
				PluginFileLoader.enablePlugin(m);
			} catch (Exception e) {
				ResonantBot.getLogger().error("Error while enabling plugin: " + m.getName(), e);
			}
		}
	}
	
	public static String load(File f, boolean enable) {
		ResonantBot.getLogger().info("Loading plugin jar: " + f.getName());
		Plugin p;
		try {
			p = PluginFileLoader.loadPlugin(f);
		} catch (IOException e) {
			e.printStackTrace();
			return "Error while loading jar: " + f.getName();
		}
		if (p != null) {
			plugins.put(f, p);
			if (enable) {
				PluginFileLoader.enablePlugin(p);
			}
			return "Completed loading plugin: " + p.getName();
		} else
			return "Could not find jar: " + f.getName();
	}
	
	public static boolean unload(Plugin plugin) {
		if (plugins.containsValue(plugin)) {
			File f = plugins.getKey(plugin);
			plugin.onDisable();
			CommandManager.unregisterPluginCommands(plugin);
			PluginFileLoader.disablePlugin(plugin);
			ResonantBot.getLogger().info("Unloaded plugin: " + plugin.getName() + " in jar: " + f.getName());
			plugins.remove(f, plugin);
			plugin = null;
			System.gc();
			return true;
		}
		return false;
	}
	
	public static void reload(File f, IChannel chan) {
		unload(plugins.get(f));
		String result = load(f, true);
		BotUtils.sendMessage(chan, result);
	}
	
	public static File getFile(Plugin plugin) {
		return plugins.getKey(plugin);
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
