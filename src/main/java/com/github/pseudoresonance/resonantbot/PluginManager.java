package com.github.pseudoresonance.resonantbot;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.simpleyaml.configuration.file.YamlConfiguration;

import com.github.pseudoresonance.resonantbot.api.Plugin;

import net.dv8tion.jda.core.entities.MessageChannel;

public class PluginManager {
	
	private static final File dir = new File(ResonantBot.getDir(), "plugins");
	
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
				ResonantBot.getLogger().info(Language.getMessage("main.foundPluginJar", f.getName()));
				loadBatch(f, false);
			}
		}
		Language.updateAllLang();
		for (Plugin m : plugins.values()) {
			try {
				PluginFileLoader.enablePlugin(m);
			} catch (Exception e) {
				ResonantBot.getLogger().error(Language.getMessage("main.enablingPluginError", m.getName()), e);
			}
		}
		pluginNames.sort(String::compareToIgnoreCase);
	}
	
	private static String loadBatch(File f, boolean enable) {
		ResonantBot.getLogger().info(Language.getMessage("main.loadingPluginJar", f.getName()));
		Plugin p;
		try {
			p = PluginFileLoader.loadPlugin(f);
		} catch (IOException e) {
			e.printStackTrace();
			return Language.getMessage("main.errorLoadingJar", f.getName());
		} catch (IllegalStateException e) {
			return Language.getMessage("main.errorLoadingJar", f.getName());
		}
		if (p != null) {
			plugins.put(f, p);
			if (enable) {
				PluginFileLoader.enablePlugin(p);
			}
			pluginNames.add(p.getName());
			copyDefaultLang(p);
			return Language.getMessage("main.completedLoadingPlugin", f.getName());
		} else
			return Language.getMessage("main.jarNotFound", f.getName());
	}
	
	public static String load(File f, boolean enable, long guildId) {
		ResonantBot.getLogger().info(Language.getMessage("main.loadingPluginJar", f.getName()));
		Plugin p;
		try {
			p = PluginFileLoader.loadPlugin(f);
		} catch (IOException e) {
			e.printStackTrace();
			if (guildId == -1)
				return Language.getMessage("main.errorLoadingJar", f.getName());
			else
				return Language.getMessage(guildId, "main.errorLoadingJar", f.getName());
		} catch (IllegalStateException e) {
			if (guildId == -1)
				return Language.getMessage("main.errorLoadingJar", f.getName());
			else
				return Language.getMessage(guildId, "main.errorLoadingJar", f.getName());
		}
		if (p != null) {
			plugins.put(f, p);
			if (enable) {
				PluginFileLoader.enablePlugin(p);
			}
			pluginNames.add(p.getName());
			pluginNames.sort(String::compareToIgnoreCase);
			copyDefaultLang(p);
			Language.updateLang(Config.getLang());
			if (guildId == -1)
				return Language.getMessage("main.completedLoadingPlugin", f.getName());
			else
				return Language.getMessage(guildId, "main.completedLoadingPlugin", f.getName());
		} else
			if (guildId == -1)
				return Language.getMessage("main.jarNotFound", f.getName());
			else
				return Language.getMessage(guildId, "main.jarNotFound", f.getName());
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
			ResonantBot.getLogger().info(Language.getMessage("main.unloadedPluginInJar", plugin.getName(), f.getName()));
			plugins.remove(f, plugin);
			plugin = null;
			System.gc();
			return true;
		}
		return false;
	}
	
	public static void reload(File f, MessageChannel chan, long guildId) {
		unload(plugins.get(f).getName());
		String result = load(f, true, guildId);
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
		throw new EOFException(Language.getMessage("main.cannotFind", entry));
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
	
	public static void copyDefaultLang(Plugin plugin, boolean overwrite) {
		JarFile jar = ((PluginClassLoader) plugin.getClass().getClassLoader()).getJar();
		Enumeration<? extends ZipEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().startsWith("localization/") && entry.getName().endsWith(".lang")) {
				try (InputStream is = jar.getInputStream(entry)) {
					if (is != null) {
						File langDir = new File(plugin.getFolder(), "localization");
						plugin.getFolder().mkdir();
						langDir.mkdir();
						String name = entry.getName().substring(13, entry.getName().length());
						File dest = new File(langDir, name);
						if (!dest.exists() || overwrite) {
							Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						}
					}
				} catch (IOException | NullPointerException e) {}
			}
		}
	}
	
	public static void copyDefaultLang(Plugin plugin) {
		copyDefaultLang(plugin, false);
	}

}
