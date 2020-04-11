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
import com.github.pseudoresonance.resonantbot.language.LanguageManager;

public final class PluginFileLoader {

	private static final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
	private static final List<PluginClassLoader> loaders = new CopyOnWriteArrayList<PluginClassLoader>();

	public static Plugin loadPlugin(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(LanguageManager.getLanguage().getMessage("main.doesNotExist", file.getPath()));
		} else {
			PluginClassLoader loader = null;
			try {
				loader = new PluginClassLoader(PluginFileLoader.class.getClassLoader(), file);
			} catch (IllegalStateException e) {
				ResonantBot.getBot().getLogger().error(LanguageManager.getLanguage().getMessage("main.genericLoadingError", file.getName()) + "\n", e);
				e.printStackTrace();
				throw e;
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
			ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.enablingPlugin", plugin.getName(), LanguageManager.getLanguage().getMessage("main.version", plugin.getVersion())));
			Plugin plug = (Plugin) plugin;
			PluginClassLoader pluginLoader = (PluginClassLoader) plug.getClassLoader();
			if (!loaders.contains(pluginLoader)) {
				loaders.add(pluginLoader);
				ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.enablingPluginUnregisteredLoader", plugin.getName()));
			}
			try {
				Thread thread = Thread.currentThread();
				ClassLoader old = thread.getContextClassLoader();
				thread.setContextClassLoader(plugin.getClassLoader());
				try {
					plug.setEnabled(true);
				} finally {
					thread.setContextClassLoader(old);
				}
			} catch (Throwable e) {
				ResonantBot.getBot().getLogger().warn(LanguageManager.getLanguage().getMessage("main.enablingPluginError", plugin.getName()));
				e.printStackTrace();
			}
		}
	}

	public static void disablePlugin(Plugin plugin) {
		if (plugin.isEnabled()) {
			ResonantBot.getBot().getLogger().info(LanguageManager.getLanguage().getMessage("main.disablingPlugin", plugin.getName(), LanguageManager.getLanguage().getMessage("main.version", plugin.getVersion())));
			Plugin plug = (Plugin) plugin;
			ClassLoader cloader = plug.getClassLoader();
			try {
				Thread thread = Thread.currentThread();
				ClassLoader old = thread.getContextClassLoader();
				thread.setContextClassLoader(plugin.getClassLoader());
				try {
					plug.setEnabled(false);
				} finally {
					thread.setContextClassLoader(old);
				}
			} catch (Throwable e) {
				ResonantBot.getBot().getLogger().warn(LanguageManager.getLanguage().getMessage("main.disablingPluginError", plugin.getName()));
				e.printStackTrace();
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