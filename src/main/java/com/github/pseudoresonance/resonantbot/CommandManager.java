package com.github.pseudoresonance.resonantbot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.api.Command;
import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.permissions.PermissionGroup;

public class CommandManager {
	
	private static Logger log = ResonantBot.getBot().getLogger();
	
	private static HashMap<String, Command> commands = new HashMap<String, Command>();
	private static HashMap<Plugin, ArrayList<String>> commandPlugins = new HashMap<Plugin, ArrayList<String>>();

	private static Field nameField = null;
	private static Field descriptionField = null;
	private static Field permissionField = null;
	private static Field pluginField = null;
	
	protected static void init() {
		try {
			nameField = Command.class.getDeclaredField("name");
			descriptionField = Command.class.getDeclaredField("descriptionKey");
			permissionField = Command.class.getDeclaredField("permissionNode");
			pluginField = Command.class.getDeclaredField("plugin");
			nameField.setAccessible(true);
			descriptionField.setAccessible(true);
			permissionField.setAccessible(true);
			pluginField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			log.error("Unable to initialize CommandManager!");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static boolean registerCommand(Plugin plugin, Command cmd, String name, String descriptionKey) {
		return registerCommand(plugin, cmd, name, descriptionKey, PermissionGroup.DEFAULT);
	}
	
	public static boolean registerCommand(Plugin plugin, Command cmd, String name, String descriptionKey, PermissionGroup permissionNode) {
		name = name.toLowerCase();
		if (commands.containsKey(name)) {
			return false;
		} else {
			log.debug("Adding command: " + name);
			if (injectCommandData(cmd, name, descriptionKey, permissionNode, plugin)) {
				commands.put(name, cmd);
				if (commandPlugins.containsKey(plugin)) {
					ArrayList<String> list = commandPlugins.get(plugin);
					if (!list.contains(name))
						list.add(name);
					list.sort(String::compareToIgnoreCase);
					commandPlugins.put(plugin, list);
				} else {
					ArrayList<String> list = new ArrayList<String>();
					list.add(name);
					commandPlugins.put(plugin, list);
				}
				return true;
			} else
				return false;
		}
	}
	
	public static Command getCommand(String text) {
		return commands.get(text.toLowerCase());
	}
	
	public static ArrayList<String> getPluginCommands(Plugin plugin) {
		if (commandPlugins.containsKey(plugin)) {
			return commandPlugins.get(plugin);
		} else {
			return null;
		}
	}
	
	public static boolean unregisterCommand(String text, Plugin plugin) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			log.debug("Removing command: " + text);
			ArrayList<String> commandList = commandPlugins.get(plugin);
			commandList.remove(text);
			if (commandList.size() > 0)
				commandPlugins.put(plugin, commandList);
			else
				commandPlugins.remove(plugin);
			commands.remove(text);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean unregisterPluginCommands(Plugin plugin) {
		if (commandPlugins.containsKey(plugin)) {
			ArrayList<String> commandList = commandPlugins.get(plugin);
			for (String text : commandList) {
				log.debug("Removing command: " + text);
				commands.remove(text);
			}
			commandPlugins.remove(plugin);
			plugin = null;
			return true;
		} else
			return false;
	}
	
	public static HashMap<String, Command> getCommands() {
		return CommandManager.commands;
	}
	
	public static LinkedHashMap<String, Command> getPluginCommandMap(Plugin plugin) {
		if (plugin != null) {
			LinkedHashMap<String, Command> ret = new LinkedHashMap<String, Command>();
			ArrayList<String> cmds = CommandManager.commandPlugins.get(plugin);
			if (cmds == null)
				return null;
			for (String cmd : cmds) {
				ret.put(cmd, commands.get(cmd));
			}
			return ret;
		}
		return null;
	}
	
	public static HashMap<Plugin, ArrayList<String>> getAllPluginCommands() {
		return CommandManager.commandPlugins;
	}
	
	private static boolean injectCommandData(Command cmd, String name, String descriptionKey, PermissionGroup permissionNode, Plugin plugin) {
		try {
			nameField.set(cmd, name);
			descriptionField.set(cmd, descriptionKey);
			permissionField.set(cmd, permissionNode);
			pluginField.set(cmd, plugin);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("Unable to set command data on cmd: " + name);
			e.printStackTrace();
			return false;
		}
	}

}
