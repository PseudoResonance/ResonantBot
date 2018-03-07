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

import com.github.pseudoresonance.resonantbot.api.Module;

import sx.blah.discord.handle.obj.IChannel;

public class ModuleManager {
	
	private static File dir = new File(ResonantBot.getDir(), "modules");
	
	private static DualHashBidiMap<File, Module> modules = new DualHashBidiMap<File, Module>();
	
	public static void reload() {
		for (Module mod : modules.values()) {
			mod.onDisable();
			modules.removeValue(mod);
		}
		modules.clear();
		File[] newFiles = dir.listFiles();
		for (File f : newFiles) {
			if (f.getName().endsWith(".jar")) {
				ResonantBot.getLogger().info("Found module jar: " + f.getName());
				load(f, false);
			}
		}
		for (Module m : modules.values()) {
			try {
				m.onEnable();
			} catch (Exception e) {
				ResonantBot.getLogger().error("Error while enabling module: " + m.getName(), e);
			}
		}
	}
	
	public static String load(File f, boolean enable) {
		ResonantBot.getLogger().info("Loading module jar: " + f.getName());
		String mainClass = "";
		String name = "";
		String error = "";
		Module module = null;
		if (f.exists()) {
			ZipInputStream in = null;
			JsonReader jr = null;
			URLClassLoader loader = null;
			try {
				URL url = new URL("jar", "","file:" + f.getAbsolutePath() + "!/");
				in = getInputStream(f, "module.json");
				jr = Json.createReader(in);
				JsonObject jo = jr.readObject();
				try {
					mainClass = jo.getString("Main");
				} catch (NullPointerException e) {
					ResonantBot.getLogger().error("No main class in module.json in module: " + f.getName());
					error = "No main class in module.json in module: " + f.getName();
				}
				try {
					name = jo.getString("Name");
				} catch (NullPointerException e) {
					ResonantBot.getLogger().error("No name in module.json in module: " + f.getName());
					error = "No name in module.json in module: " + f.getName();
				}
				loader = new URLClassLoader(new URL[] {url}, ResonantBot.class.getClassLoader());
				Class<?> clazz = loader.loadClass(mainClass);
				if (Module.class.isAssignableFrom(clazz)) {
					ResonantBot.getLogger().info("Initializing module: " + name + " in jar: " + f.getName());
					module = (Module) clazz.getConstructors()[0].newInstance();
					modules.put(f, module);
				} else {
					ResonantBot.getLogger().error("Class: " + mainClass + " does not extend Module!");
					error = "Class: " + mainClass + " does not extend Module!";
				}
			} catch (EOFException e) {
				ResonantBot.getLogger().error("No module.json in module: " + f.getName());
				error = "No module.json in module: " + f.getName();
			} catch (IOException e) {
				ResonantBot.getLogger().error("Failed to load module: " + f.getName(), e);
				error = "Failed to load module: " + f.getName();
			} catch (ClassNotFoundException e) {
				ResonantBot.getLogger().error("Invalid main class: " + mainClass + " in module: " + f.getName(), e);
				error = "Invalid main class: " + mainClass + " in module: " + f.getName();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				ResonantBot.getLogger().error("Main class has incorrect constructor in module: " + f.getName(), e);
				error = "Main class has incorrect constructor in module: " + f.getName();
			} catch (Exception e) {
				ResonantBot.getLogger().error("Error while loading module: " + f.getName(), e);
				error = "Error while loading module: " + f.getName();
			} catch (OutOfMemoryError e) {
				ResonantBot.getLogger().error("Error while loading module: " + f.getName(), e);
				error = "Error while loading module: " + f.getName();
				System.exit(1);
			} catch (Error e) {
				ResonantBot.getLogger().error("Error while loading module: " + f.getName(), e);
				error = "Error while loading module: " + f.getName();
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
			ResonantBot.getLogger().info("Failed to initialize module: " + name + " in jar: " + f.getName());
		else {
			ResonantBot.getLogger().info("Completed initializing module: " + name + " in jar: " + f.getName());
			error = "Completed initializing module: " + name;
		}
		if (enable) {
			if (module != null) {
				try {
					module.onEnable();
				} catch (Exception e) {
					ResonantBot.getLogger().error("Error while enabling module: " + module.getName(), e);
				}
			}
		}
		return error;
	}
	
	public static void unload(Module mod) {
		if (modules.containsValue(mod)) {
			File f = modules.getKey(mod);
			mod.onDisable();
			CommandManager.unregisterModuleCommands(mod);
			ResonantBot.getLogger().info("Unloaded module: " + mod.getName() + " in jar: " + f.getName());
			modules.remove(mod);
			mod = null;
		}
	}
	
	public static void reload(File f, IChannel chan) {
		unload(modules.get(f));
		String result = load(f, true);
		BotUtils.sendMessage(chan, result);
	}
	
	public static File getFile(Module mod) {
		return modules.getKey(mod);
	}
	
	public static Module getModule(File f) {
		return modules.get(f);
	}
	
	public static Set<Module> getModules() {
		return modules.values();
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
