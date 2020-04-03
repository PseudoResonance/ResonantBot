package com.github.pseudoresonance.resonantbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.api.Command;
import com.github.pseudoresonance.resonantbot.api.Plugin;

public class CommandManager {
	
	private static Logger log = ResonantBot.getBot().getLogger();
	
	private static HashMap<String, Command> commands = new HashMap<String, Command>();
	private static HashMap<Plugin, ArrayList<String>> commandPlugins = new HashMap<Plugin, ArrayList<String>>();
	
	public static boolean registerCommand(String text, Command c, Plugin plugin) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			return false;
		} else {
			log.debug("Adding command: " + text);
			commands.put(text, c);
			if (commandPlugins.containsKey(plugin)) {
				ArrayList<String> list = commandPlugins.get(plugin);
				if (!list.contains(text))
					list.add(text);
				list.sort(String::compareToIgnoreCase);
				commandPlugins.put(plugin, list);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(text);
				commandPlugins.put(plugin, list);
			}
			return true;
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

}
