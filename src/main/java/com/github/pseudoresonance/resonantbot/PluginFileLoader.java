package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.pseudoresonance.resonantbot.api.Plugin;

public final class PluginFileLoader {

	private static final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
	private static final List<PluginClassLoader> loaders = new CopyOnWriteArrayList<PluginClassLoader>();

	public static Plugin loadPlugin(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath() + " does not exist");
		} else {
			PluginClassLoader loader = null;
			try {
				loader = new PluginClassLoader(PluginFileLoader.class.getClassLoader(), file);
			} catch (IOException e) {
				throw e;
			}
			loaders.add(loader);
			return loader.plugin;
		}
	}

	static Class<?> getClassByName(String name) {
		Class<?> cachedClass = (Class<?>) classes.get(name);
		if (cachedClass != null) {
			return cachedClass;
		} else {
			Iterator<PluginClassLoader> iterator = loaders.iterator();
			while (iterator.hasNext()) {
				PluginClassLoader loader = (PluginClassLoader) iterator.next();
				try {
					cachedClass = loader.findClass(name, false);
				} catch (ClassNotFoundException ex) {}
				if (cachedClass != null) {
					return cachedClass;
				}
			}
			return null;
		}
	}

	static void setClass(String name, Class<?> clazz) {
		if (!classes.containsKey(name)) {
			classes.put(name, clazz);
		}

	}

	public static void enablePlugin(Plugin plugin) {
		if (!plugin.isEnabled()) {
			ResonantBot.getLogger().info("Enabling plugin: " + plugin.getName());
			Plugin plug = (Plugin) plugin;
			PluginClassLoader pluginLoader = (PluginClassLoader) plug.getClassLoader();
			if (!loaders.contains(pluginLoader)) {
				loaders.add(pluginLoader);
				ResonantBot.getLogger().info("Enabling plugin with unregistered PluginClassLoader: " + plugin.getName());
			}
			try {
				plug.setEnabled(true);
			} catch (Throwable arg4) {
				ResonantBot.getLogger().warn("Error occurred while enabling: " + plugin.getName());
			}
		}
	}

	public static void disablePlugin(Plugin plugin) {
		if (plugin.isEnabled()) {
			ResonantBot.getLogger().info("Disabling plugin: " + plugin.getName());
			Plugin plug = (Plugin) plugin;
			ClassLoader cloader = plug.getClassLoader();
			try {
				plug.setEnabled(false);
			} catch (Throwable arg4) {
				ResonantBot.getLogger().warn("Error occurred while disabling: " + plugin.getName());
			}
			if (cloader instanceof PluginClassLoader) {
				PluginClassLoader loader = (PluginClassLoader) cloader;
				loaders.remove(loader);
				Set<?> names = loader.getClasses();
				Iterator<?> arg7 = names.iterator();
				while (arg7.hasNext()) {
					String name = (String) arg7.next();
					classes.remove(name);
				}
				loader.delete();
				loader = null;
			}
		}

	}
}