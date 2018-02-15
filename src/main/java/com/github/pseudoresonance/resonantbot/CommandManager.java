package com.github.pseudoresonance.resonantbot;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;

public class CommandManager {
	
	private static Logger log = ResonantBot.getLogger();
	
	private static HashMap<String, Command> commands = new HashMap<String, Command>();
	private static DualHashBidiMap<Module, ArrayList<String>> commandModules = new DualHashBidiMap<Module, ArrayList<String>>();
	
	public static boolean registerCommand(String text, Command c, Module module) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			return false;
		} else {
			log.debug("Adding command: " + text);
			commands.put(text, c);
			if (commandModules.containsKey(module)) {
				ArrayList<String> list = commandModules.get(module);
				if (!list.contains(text))
					list.add(text);
				commandModules.put(module, list);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(text);
				commandModules.put(module, list);
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
	
	public static ArrayList<String> getModuleCommands(Module module) {
		if (commandModules.containsKey(module)) {
			return commandModules.get(module);
		} else {
			return null;
		}
	}
	
	public static boolean unregisterCommand(String text, Module module) {
		text = text.toLowerCase();
		if (commands.containsKey(text)) {
			log.debug("Removing command: " + text);
			ArrayList<String> commandList = commandModules.get(module);
			commandList.remove(text);
			if (commandList.size() > 0)
				commandModules.put(module, commandList);
			else
				commandModules.remove(module);
			commands.remove(text);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean unregisterModuleCommands(Module module) {
		if (commandModules.containsKey(module)) {
			ArrayList<String> commandList = commandModules.get(module);
			for (String text : commandList) {
				log.debug("Removing command: " + text);
				commands.remove(text);
			}
			commandModules.remove(module);
			module = null;
			return true;
		} else
			return false;
	}
	
	public static HashMap<String, Command> getCommands() {
		return CommandManager.commands;
	}
	
	public static DualHashBidiMap<Module, ArrayList<String>> getAllModuleCommands() {
		return CommandManager.commandModules;
	}

}
