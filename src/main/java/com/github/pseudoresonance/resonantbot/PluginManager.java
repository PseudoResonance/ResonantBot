package com.github.pseudoresonance.resonantbot;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.simpleyaml.configuration.file.YamlConfiguration;

import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.language.LanguageManager;

import net.dv8tion.jda.api.entities.MessageChannel;

public class PluginManager {
	
	private static final File dir = new File(ResonantBot.getBot().getDirectory(), "plugins");
	
	private static DualHashBidiMap<File, Plugin> plugins = new DualHashBidiMap<File, Plugin>();
	private static ArrayList<String> pluginNames = new ArrayList<String>();
	
	public static void reload() {
		for (Plugin plug : plugins.values()) {
			PluginFileLoader.disablePlugin(plug);
			plugins.removeValue(plug);
		}
		plugins.clear();
		File[] newFiles = dir.listFiles();
		for (File f : newFiles) {
			if (f.getName().endsWith(".jar")) {
				ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.foundPluginJar", f.getName()));
				loadBatch(f, false);
			}
		}
		for (Plugin m : plugins.values()) {
			try {
				PluginFileLoader.enablePlugin(m);
			} catch (Exception e) {
				ResonantBot.getBot().getLogger().error(LanguageManager.getLanguage().getMessage("main.enablingPluginError", m.getName()), e);
			}
		}
		pluginNames.sort(String::compareToIgnoreCase);
	}
	
	private static String loadBatch(File f, boolean enable) {
		ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.loadingPluginJar", f.getName()));
		Plugin p;
		try {
			p = PluginFileLoader.loadPlugin(f);
		} catch (IOException e) {
			e.printStackTrace();
			return LanguageManager.getLanguage().getMessage("main.errorLoadingJar", f.getName());
		} catch (IllegalStateException e) {
			return LanguageManager.getLanguage().getMessage("main.errorLoadingJar", f.getName());
		}
		if (p != null) {
			plugins.put(f, p);
			if (enable) {
				PluginFileLoader.enablePlugin(p);
			}
			pluginNames.add(p.getName());
			LanguageManager.copyDefaultPluginLanguageFiles(p, false);
			return LanguageManager.getLanguage().getMessage("main.completedLoadingPlugin", p.getName(), LanguageManager.getLanguage().getMessage("main.version", p.getVersion()));
		} else
			return LanguageManager.getLanguage().getMessage("main.jarNotFound", f.getName());
	}
	
	public static String load(File f, boolean enable, long id) {
		ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.loadingPluginJar", f.getName()));
		Plugin p;
		try {
			p = PluginFileLoader.loadPlugin(f);
		} catch (IOException e) {
			e.printStackTrace();
			if (id == -1)
				return LanguageManager.getLanguage().getMessage("main.errorLoadingJar", f.getName());
			else
				return LanguageManager.getLanguage(id).getMessage("main.errorLoadingJar", f.getName());
		} catch (IllegalStateException e) {
			if (id == -1)
				return LanguageManager.getLanguage().getMessage("main.errorLoadingJar", f.getName());
			else
				return LanguageManager.getLanguage(id).getMessage("main.errorLoadingJar", f.getName());
		}
		if (p != null) {
			plugins.put(f, p);
			if (enable) {
				PluginFileLoader.enablePlugin(p);
			}
			pluginNames.add(p.getName());
			pluginNames.sort(String::compareToIgnoreCase);
			LanguageManager.copyDefaultPluginLanguageFiles(p, false);
			if (id == -1)
				return LanguageManager.getLanguage().getMessage("main.completedLoadingPlugin", p.getName(), LanguageManager.getLanguage().getMessage("main.version", p.getVersion()));
			else
				return LanguageManager.getLanguage(id).getMessage("main.completedLoadingPlugin", p.getName(), LanguageManager.getLanguage(id).getMessage("main.version", p.getVersion()));
		} else
			if (id == -1)
				return LanguageManager.getLanguage().getMessage("main.jarNotFound", f.getName());
			else
				return LanguageManager.getLanguage(id).getMessage("main.jarNotFound", f.getName());
	}
	
	public static String load(File f, boolean enable) {
		return load(f, enable, -1);
	}
	
	public static boolean unload(String name) {
		Plugin plugin = null;
		for (Plugin p : plugins.values()) {
			if (p.getName().equalsIgnoreCase(name)) {
				plugin = p;
			}
		}
		if (plugin != null) {
			File f = plugins.getKey(plugin);
			CommandManager.unregisterPluginCommands(plugin);
			PluginFileLoader.disablePlugin(plugin);
			pluginNames.remove(plugin.getName());
			ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.unloadedPluginInJar", plugin.getName(), f.getName()));
			plugins.remove(f, plugin);
			plugin = null;
			System.gc();
			return true;
		}
		return false;
	}
	
	public static void reload(File f, MessageChannel chan, long id) {
		unload(plugins.get(f).getName());
		String result = load(f, true, id);
		chan.sendMessage(result).queue();
	}
	
	public static void reload(File f, MessageChannel chan) {
		unload(plugins.get(f).getName());
		String result = load(f, true);
		chan.sendMessage(result).queue();
	}
	
	public static File getFile(Plugin plugin) {
		return plugins.getKey(plugin);
	}
	
	public static Plugin getPlugin(String s) {
		for (Plugin p : plugins.values()) {
			if (p.getName().equalsIgnoreCase(s)) {
				return p;
			}
		}
		return null;
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
	
	public static ArrayList<String> getPluginNames() {
		return pluginNames;
	}

	static ZipInputStream getInputStream(File zip, String entry) throws IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zip));
		for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
			if (e.getName().equals(entry)) {
				return zin;
			}
		}
		throw new EOFException(LanguageManager.getLanguage().getMessage("main.cannotFind", entry));
	}
	
	protected static YamlConfiguration updateLanguage(YamlConfiguration global, boolean overwrite) {
		for (Plugin p : plugins.values()) {
			YamlConfiguration config = getLanguage(p, Config.getLang(), overwrite);
			for (String key : config.getKeys(true)) {
				global.set(key, config.get(key));
			}
		}
		return global;
	}
	
	protected static YamlConfiguration updateGuildLanguage(String name, YamlConfiguration global, boolean overwrite) {
		for (Plugin p : plugins.values()) {
			YamlConfiguration config = getLanguage(p, name, overwrite);
			for (String key : config.getKeys(true)) {
				global.set(key, config.get(key));
			}
		}
		return global;
	}
	
	protected static YamlConfiguration updateLanguage(YamlConfiguration global) {
		return updateLanguage(global, false);
	}
	
	public static YamlConfiguration getLanguage(Plugin plugin, String name, boolean overwrite) {
		File lang = new File(plugin.getFolder(), "localization/" + name + ".lang");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(lang);
		return yaml;
	}
	
	public static YamlConfiguration getLanguage(Plugin plugin, String name) {
		return getLanguage(plugin, name, false);
	}

}
