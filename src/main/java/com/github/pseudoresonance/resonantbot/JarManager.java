package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;

public class JarManager {

	private static Logger log = ResonantBot.getLogger();
	
	private static ArrayList<String> jars = new ArrayList<String>();
	
	public static void getJars() {
		jars.clear();
		File[] files = new File(ResonantBot.getDir(), "commands").listFiles();
		for (File f : files) {
			if (f.getName().endsWith(".jar")) {
				jars.add(f.getAbsolutePath());
				log.debug("Found plugin: " + f.getName());
			}
		}
	}
	
	public static void loadJars() {
		for (String s : jars) {
			log.debug("Loading plugin at: " + s);
			loadJar(new File(s));
		}
	}
	
	public static String loadJar(File f) {
		if (f.exists()) {
			try {
				InputStream in = f.getClass().getResourceAsStream("command.json");
				JsonReader jr = Json.createReader(in);
				JsonObject jo = jr.readObject();
				jr.close();
				in.close();
				String main = jo.getString("Main");
				String command = jo.getString("Command");
				ClassLoader loader = URLClassLoader.newInstance(new URL[] {f.toURI().toURL()});
				try {
					Command c = (Command) loader.loadClass(main).newInstance();
					CommandManager.registerCommand(command, c);
					return "Successfully loaded " + main + " inside " + f.getName() + "!";
				} catch (InstantiationException e) {
					log.error("Could not load class: " + main + " inside " + f.getName() + "!");
					e.printStackTrace();
					return "Could not load class: " + main + " inside " + f.getName() + "!";
				} catch (IllegalAccessException e) {
					log.error("Could not access class: " + main + " inside " + f.getName() + "!");
					e.printStackTrace();
					return "Could not access class: " + main + " inside " + f.getName() + "!";
				} catch (ClassNotFoundException e) {
					log.error("Could not find class: " + main + " inside " + f.getName() + "!");
					e.printStackTrace();
					return "Could not find class: " + main + " inside " + f.getName() + "!";
				}
			} catch (IOException e) {
				log.error("Could not read " + f.getName() + "!");
				e.printStackTrace();
				return "Could not read " + f.getName() + "!";
			}
		}
		return "Jarfile: " + f.getName() + " does not exist!";
	}

}
