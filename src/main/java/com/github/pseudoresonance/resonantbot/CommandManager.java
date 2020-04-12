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
	private static HashMap<String, Command> aliases = new HashMap<String, Command>();
	private static HashMap<Plugin, ArrayList<String>> aliasesPlugins = new HashMap<Plugin, ArrayList<String>>();

	private static HashMap<String, Command> masterMap = new HashMap<String, Command>();

	private static Field nameField = null;
	private static Field descriptionField = null;
	private static Field permissionField = null;
	private static Field pluginField = null;
	private static Field aliasesField = null;

	protected static void init() {
		try {
			nameField = Command.class.getDeclaredField("name");
			descriptionField = Command.class.getDeclaredField("descriptionKey");
			permissionField = Command.class.getDeclaredField("permissionNode");
			pluginField = Command.class.getDeclaredField("plugin");
			aliasesField = Command.class.getDeclaredField("aliases");
			nameField.setAccessible(true);
			descriptionField.setAccessible(true);
			permissionField.setAccessible(true);
			pluginField.setAccessible(true);
			aliasesField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			log.error("Unable to initialize CommandManager!");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static boolean registerCommand(Plugin plugin, Command cmd, String name, String descriptionKey, String... aliasArr) {
		return registerCommand(plugin, cmd, name, descriptionKey, PermissionGroup.DEFAULT, aliasArr);
	}

	@SuppressWarnings("unchecked")
	public static boolean registerCommand(Plugin plugin, Command cmd, String name, String descriptionKey, PermissionGroup permissionNode, String... aliasArr) {
		name = name.toLowerCase();
		log.debug("Adding command: " + name);
		if (injectCommandData(cmd, name, descriptionKey, permissionNode, plugin, aliasArr)) {
			boolean added = false;
			if (!commands.containsKey(name)) {
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
				added = true;
			}
			for (String alias : aliasArr) {
				alias = alias.toLowerCase();
				if (!aliases.containsKey(alias)) {
					aliases.put(alias, cmd);
					if (aliasesPlugins.containsKey(plugin)) {
						ArrayList<String> list = aliasesPlugins.get(plugin);
						if (!list.contains(alias))
							list.add(alias);
						list.sort(String::compareToIgnoreCase);
						aliasesPlugins.put(plugin, list);
					} else {
						ArrayList<String> list = new ArrayList<String>();
						list.add(alias);
						aliasesPlugins.put(plugin, list);
					}
					added = true;
				}
			}
			if (added) {
				masterMap = ((HashMap<String, Command>) aliases.clone());
				masterMap.putAll(commands);
			}
			return added;
		} else
			return false;
	}

	public static Command getCommand(String text) {
		return masterMap.get(text.toLowerCase());
	}

	public static ArrayList<String> getPluginCommands(Plugin plugin) {
		ArrayList<String> cmds = new ArrayList<String>();
		if (aliasesPlugins.containsKey(plugin))
			cmds.addAll(aliasesPlugins.get(plugin));
		if (commandPlugins.containsKey(plugin))
			cmds.addAll(commandPlugins.get(plugin));
		return cmds;
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

	public static boolean unregisterAlias(String text, Plugin plugin) {
		text = text.toLowerCase();
		if (aliases.containsKey(text)) {
			log.debug("Removing alias: " + text);
			ArrayList<String> commandList = aliasesPlugins.get(plugin);
			commandList.remove(text);
			if (commandList.size() > 0)
				aliasesPlugins.put(plugin, commandList);
			else
				aliasesPlugins.remove(plugin);
			aliases.remove(text);
			return true;
		} else {
			return false;
		}
	}

	public static boolean unregisterPluginCommands(Plugin plugin) {
		boolean ret = false;
		if (commandPlugins.containsKey(plugin)) {
			ArrayList<String> commandList = commandPlugins.get(plugin);
			for (String text : commandList) {
				log.debug("Removing command: " + text);
				commands.remove(text);
			}
			commandPlugins.remove(plugin);
			plugin = null;
			ret = true;
		}
		if (aliasesPlugins.containsKey(plugin)) {
			ArrayList<String> commandList = aliasesPlugins.get(plugin);
			for (String text : commandList) {
				log.debug("Removing alias: " + text);
				aliases.remove(text);
			}
			aliasesPlugins.remove(plugin);
			plugin = null;
			ret = true;
		}
		return ret;
	}

	public static HashMap<String, Command> getCommands() {
		return CommandManager.masterMap;
	}

	public static LinkedHashMap<String, Command> getPluginCommandMap(Plugin plugin) {
		return getPluginCommandMap(plugin, false);
	}

	public static LinkedHashMap<String, Command> getPluginCommandMap(Plugin plugin, boolean excludeDuplicates) {
		if (plugin != null) {
			LinkedHashMap<String, Command> ret = new LinkedHashMap<String, Command>();
			if (CommandManager.commandPlugins.containsKey(plugin)) {
				for (String cmd : CommandManager.commandPlugins.get(plugin)) {
					Command c = masterMap.get(cmd);
					if (c.getPlugin().getName().equals(plugin.getName()) && (!excludeDuplicates || !ret.containsValue(c)))
						ret.put(cmd, c);
				}
			}
			if (CommandManager.aliasesPlugins.containsKey(plugin)) {
				for (String cmd : CommandManager.aliasesPlugins.get(plugin)) {
					Command c = masterMap.get(cmd);
					if (!ret.containsKey(cmd) && c.getPlugin().getName().equals(plugin.getName()) && (!excludeDuplicates || !ret.containsValue(c)))
						ret.put(cmd, c);
				}
			}
			return ret;
		}
		return null;
	}

	public static HashMap<Plugin, ArrayList<String>> getAllPluginCommands() {
		return CommandManager.commandPlugins;
	}

	public static HashMap<Plugin, ArrayList<String>> getAllPluginAliases() {
		return CommandManager.aliasesPlugins;
	}

	private static boolean injectCommandData(Command cmd, String name, String descriptionKey, PermissionGroup permissionNode, Plugin plugin, String[] aliases) {
		try {
			nameField.set(cmd, name);
			descriptionField.set(cmd, descriptionKey);
			permissionField.set(cmd, permissionNode);
			pluginField.set(cmd, plugin);
			aliasesField.set(cmd, aliases);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("Unable to set command data on cmd: " + name);
			e.printStackTrace();
			return false;
		}
	}

}
