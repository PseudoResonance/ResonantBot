package com.github.pseudoresonance.resonantbot;

import java.util.HashMap;

public class CommandManager {
	
	private static HashMap<String, Command> commands = new HashMap<String, Command>();
	
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
	
	public static boolean removeCommand(String text) {
		if (commands.containsKey(text)) {
			commands.remove(text);
			return true;
		} else {
			return false;
		}
	}
	
	public static HashMap<String, Command> getCommands() {
		return CommandManager.commands;
	}

}
