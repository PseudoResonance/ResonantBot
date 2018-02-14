package com.github.pseudoresonance.resonantbot;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;

public class CommandManager {
	
	private static Logger log = ResonantBot.getLogger();

	private static ArrayList<File> jars = new ArrayList<File>();
	private static File dir = new File(ResonantBot.getDir(), "commands");
	
	private static HashMap<String, Command> commands = new HashMap<String, Command>();
	private static HashMap<String, File> commandFiles = new HashMap<String, File>();
	
	public static boolean registerCommand(String text, Command c) {
		if (commands.containsKey(text)) {
			return false;
		} else {
			commands.put(text, c);
			return true;
		}
	}
	
	public static Command getCommand(String text) {
		if (commands.containsKey(text)) {
			return commands.get(text);
		} else {
			return null;
		}
	}
	
	public static File getCommandFile(String text) {
		if (commandFiles.containsKey(text)) {
			return commandFiles.get(text);
		} else {
			return null;
		}
	}
	
	public static boolean removeCommand(String text) {
		if (commands.containsKey(text)) {
			log.debug("Removing plugin: " + text);
			commands.remove(text);
			commandFiles.remove(text);
			return true;
		} else {
			return false;
		}
	}
	
	public static HashMap<String, Command> getCommands() {
		return CommandManager.commands;
	}

	public static void loadJars() {
		jars.clear();
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.getName().endsWith(".jar")) {
				jars.add(f);
				log.debug("Found plugin: " + f.getName());
			}
		}
		for (File f : jars) {
			log.debug("Loading plugin: " + f.getName());
			loadJar(f, false);
		}
	}

	public static String loadJar(File f, boolean reload) {
		if (f.exists()) {
			try {
				InputStream in = getInputStream(f, "command.json");
				JsonReader jr = Json.createReader(in);
				JsonObject jo = jr.readObject();
				jr.close();
				in.close();
				String command = jo.getString("Command");
				try {
					PluginLoader loader = new PluginLoader(f, Command.class.getClassLoader());
					Command com = loader.command;
					log.info("Initializing Command: " + command);
					commands.put(command.toLowerCase(), com);
					commandFiles.put(command.toLowerCase(), f);
					loader.close();
					log.info("Initialized Command: " + command);
					if (reload) {
						return "Successfully reloaded " + command + "!";
					} else {
						return "Successfully loaded " + command + "!";
					}
				} catch (IOException e) {
					e.printStackTrace();
					return "An error occurred!";
				}
			} catch (IOException e) {
				log.error("Could not read " + f.getName() + "!");
				e.printStackTrace();
				return "Could not read " + f.getName() + "!";
			}
		} else {
			return "Jarfile: " + f.getName() + " does not exist!";
		}
	}

	static InputStream getInputStream(File zip, String entry) throws IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zip));
		for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
			if (e.getName().equals(entry)) {
				return zin;
			}
		}
		throw new EOFException("Cannot find " + entry);
	}
	
	public static File getDir() {
		return dir;
	}

}
