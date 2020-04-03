package com.github.pseudoresonance.resonantbot;

import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.language.LanguageManager;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

	private static Field nameField = null;
	private static Field versionField = null;
	private static Field folderField = null;
	
	private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
	private final JarFile jar;
	final Plugin plugin;
	
	protected static void init() {
		try {
			nameField = Plugin.class.getDeclaredField("name");
			versionField = Plugin.class.getDeclaredField("version");
			folderField = Plugin.class.getDeclaredField("folder");
			nameField.setAccessible(true);
			versionField.setAccessible(true);
			folderField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			ResonantBot.getBot().getLogger().error("Unable to initialize PluginClassLoader!");
			e.printStackTrace();
			System.exit(1);
		}
	}

	PluginClassLoader(ClassLoader parent, File file) throws IOException, MalformedURLException {
		super(new URL[] { file.toURI().toURL() }, parent);
		this.jar = new JarFile(file);
		Plugin plug = null;
		String mainClass = "";
		String name = "";
		String version = "";
		try (InputStream in = getInputStream(file, "plugin.json")) {
			try (JsonReader jr = Json.createReader(in)) {
				JsonObject jo = jr.readObject();
				try {
					mainClass = jo.getString("Main");
				} catch (NullPointerException e) {
					ResonantBot.getBot().getLogger().error(LanguageManager.getLanguage().getMessage("main.noMainClass", file.getName()));
				}
				try {
					name = jo.getString("Name");
				} catch (NullPointerException e) {
					ResonantBot.getBot().getLogger().error(LanguageManager.getLanguage().getMessage("main.noName", file.getName()));
				}
				try {
					version = jo.getString("Version");
				} catch (NullPointerException e) {
					ResonantBot.getBot().getLogger().error(LanguageManager.getLanguage().getMessage("main.noVersion", file.getName()));
				}
				try {
					Class<?> ex;
					try {
						ex = Class.forName(mainClass, true, this);
					} catch (ClassNotFoundException exc) {
						throw new IllegalStateException(LanguageManager.getLanguage().getMessage("main.invalidMainClass", mainClass));
					}
					File folder = new File(PluginManager.getDir(), name);
					Class<?> pluginClass;
					try {
						pluginClass = ex.asSubclass(Plugin.class);
					} catch (ClassCastException exc) {
						throw new IllegalStateException(LanguageManager.getLanguage().getMessage("main.doesNotExtendPlugin", mainClass));
					}
					Constructor<?> constructor = pluginClass.getConstructor();
					plug = (Plugin) constructor.newInstance();
					nameField.set(plug, name);
					versionField.set(plug, version);
					folderField.set(plug, folder);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(LanguageManager.getLanguage().getMessage("main.noPublicConstructor", file.getName()), e);
				} catch (InstantiationException e) {
					throw new IllegalStateException(LanguageManager.getLanguage().getMessage("main.abnormalType", file.getName()), e);
				} catch (Exception e) {
					throw new IllegalStateException(LanguageManager.getLanguage().getMessage("main.genericLoadingError", file.getName()), e);
				} catch (OutOfMemoryError e) {
					ResonantBot.getBot().getLogger().error(LanguageManager.getLanguage().getMessage("main.outOfMemory", file.getName()) + "\n", e);
					System.exit(1);
				} catch (Error e) {
					throw new IllegalStateException(LanguageManager.getLanguage().getMessage("main.genericLoadingError", file.getName()), e);
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
	
	public void delete() {
		this.classes.clear();
		try {
			this.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public JarFile getJar() {
		return this.jar;
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
		throw new EOFException(LanguageManager.getLanguage().getMessage("main.cannotFind", entry));
	}
}