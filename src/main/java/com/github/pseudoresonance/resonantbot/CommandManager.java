package com.github.pseudoresonance.resonantbot;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.api.Command;
import com.github.pseudoresonance.resonantbot.api.Plugin;

public class CommandManager {
	
	private static Logger log = ResonantBot.getLogger();
	
	private static HashMap<String, Command> commands = new HashMap<String, Command>();
	private static DualHashBidiMap<Plugin, ArrayList<String>> commandPlugins = new DualHashBidiMap<Plugin, ArrayList<String>>();
	
	public static boolean registerCommand(String text, Command c, Plugin module) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			return false;
		} else {
			log.debug("Adding command: " + text);
			commands.put(text, c);
			if (commandPlugins.containsKey(module)) {
				ArrayList<String> list = commandPlugins.get(module);
				if (!list.contains(text))
					list.add(text);
				commandPlugins.put(module, list);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(text);
				commandPlugins.put(module, list);
			}
			return true;
		}
	}
	
	public static Command getCommand(String text) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			return commands.get(text);
		} else {
			return null;
		}
	}
	
	public static ArrayList<String> getModuleCommands(Plugin module) {
		if (commandPlugins.containsKey(module)) {
			return commandPlugins.get(module);
		} else {
			return null;
		}
	}
	
	public static boolean unregisterCommand(String text, Plugin module) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			log.debug("Removing command: " + text);
			ArrayList<String> commandList = commandPlugins.get(module);
			commandList.remove(text);
			if (commandList.size() > 0)
				commandPlugins.put(module, commandList);
			else
				commandPlugins.remove(module);
			commands.remove(text);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean unregisterPluginCommands(Plugin module) {
		if (commandPlugins.containsKey(module)) {
			ArrayList<String> commandList = commandPlugins.get(module);
			for (String text : commandList) {
				log.debug("Removing command: " + text);
				commands.remove(text);
			}
			commandPlugins.remove(module);
			module = null;
			return true;
		} else
			return false;
	}
	
	public static HashMap<String, Command> getCommands() {
		return CommandManager.commands;
	}
	
	public static DualHashBidiMap<Plugin, ArrayList<String>> getAllModuleCommands() {
		return CommandManager.commandPlugins;
	}

}
