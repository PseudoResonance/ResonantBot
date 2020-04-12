package com.github.pseudoresonance.resonantbot.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.language.LanguageManager;
import com.github.pseudoresonance.resonantbot.permissions.PermissionGroup;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHandler extends Command {
	
	private final String cmdName;
	private final String descriptionKey;
	private final PermissionGroup permissionNode;
	private final String[] aliases;
	
	private final RunnableMap commandMap = new RunnableMap();

	/**
	 * Creates a new {@link CommandHandler} with the given command name, description language key
	 * 
	 * Permission node defaults to public
	 * 
	 * @param cmdName Name of command
	 * @param descriptionKey Localization key of command description
	 * @param aliases Command aliases
	 */
	public CommandHandler(String cmdName, String descriptionKey, String... aliases) {
		this(cmdName, descriptionKey, PermissionGroup.DEFAULT, aliases);
	}

	/**
	 * Creates a new {@link CommandHandler} with the given command name, description language key and permission node
	 * 
	 * @param cmdName Name of command
	 * @param descriptionKey Localization key of command description
	 * @param permissionNode Command permission node
	 * @param aliases Command aliases
	 */
	public CommandHandler(String cmdName, String descriptionKey, PermissionGroup permissionNode, String... aliases) {
		this.cmdName = cmdName;
		this.descriptionKey = descriptionKey;
		this.permissionNode = permissionNode;
		this.aliases = aliases;
	}
	
	/**
	 * Registers the command in the {@link CommandManager} when setup is complete
	 * 
	 * @param plugin Plugin to register command with
	 */
	public void register(Plugin plugin) {
		CommandManager.registerCommand(plugin, this, cmdName, descriptionKey, permissionNode, aliases);
	}
	
	/**
	 * Registers the given subcommand with the command
	 * 
	 * Permission node defaults to public
	 * 
	 * @param cmdPath Path of the subcommand, each consecutive subcommand separated by spaces
	 * ex: "leaderboard wins"
	 * @param run Runnable {@link TriPredicate} to call when subcommand is run
	 * Takes a {@link MessageReceivedEvent}, String of command name, and String array of arguments, and returns boolean on whether or not running subcommand was successful
	 */
	public void registerSubcommand(String cmdPath, TriPredicate<MessageReceivedEvent, String, String[]> run) {
		registerSubcommand(cmdPath, run, PermissionGroup.DEFAULT);
	}
	
	/**
	 * Registers the given subcommand with the command
	 * 
	 * @param cmdPath Path of the subcommand, each consecutive subcommand separated by spaces
	 * ex: "leaderboard wins"
	 * @param run Runnable {@link TriPredicate} to call when subcommand is run
	 * Takes a {@link MessageReceivedEvent}, String of command name, and String array of arguments, and returns boolean on whether or not running subcommand was successful
	 * @param permissionNode Permission node to register subcommand with
	 */
	public void registerSubcommand(String cmdPath, TriPredicate<MessageReceivedEvent, String, String[]> run, PermissionGroup permissionNode) {
		String[] split = cmdPath.toLowerCase().split(" ");
		RunnableMap lastNode = commandMap;
		for (int i = 0; i < split.length; i++) {
			RunnableMap node = lastNode.get(split[i]);
			if (node == null) {
				node = new RunnableMap();
				lastNode.put(split[i], node);
			}
			if (i == split.length - 1)
				node.attachRunnable(run, permissionNode);
			lastNode = node;
		}
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String command, HashSet<PermissionGroup> userPermissions, String[] args) {
		int lastI = 0;
		RunnableMap lastValidNode = null;
		RunnableMap lastNode = commandMap;
		for (int i = 0; i < args.length; i++) {
			RunnableMap node = lastNode.get(args[i].toLowerCase());
			if (node != null) {
				if (userPermissions.contains(node.getPermissionNode())) {
					if (node.isRunnable()) {
						lastValidNode = node;
						lastI = i;
					}
					lastNode = node;
				}
			} else
				break;
		}
		if (lastValidNode != null)
			if (lastValidNode.test(e, command, Arrays.copyOfRange(args, lastI + 1, args.length)))
				return;
		String subCommands = "";
		for (Entry<String, RunnableMap> entry : lastNode.entrySet())
			if (userPermissions.contains(entry.getValue().getPermissionNode()))
				subCommands += "`" + entry.getKey() + "`, ";
		if (subCommands.length() > 2)
			subCommands = subCommands.substring(0, subCommands.length() - 2);
		else if (subCommands.isEmpty())
			subCommands = LanguageManager.getLanguage(e).getMessage("main.none");
		e.getChannel().sendMessage(LanguageManager.getLanguage(e).getMessage("main.validSubcommands", subCommands)).queue();
	}
	
	/**
	 * Returns the command name
	 */
	public String getName() {
		return cmdName;
	}
	
	/**
	 * Returns the command localization description key
	 */
	public String getDescriptionKey() {
		return descriptionKey;
	}
	
	/**
	 * Returns the command permission node
	 */
	public PermissionGroup getPermissionNode() {
		return permissionNode;
	}
	
	/**
	 * Returns the command aliases
	 */
	public String[] getAliases() {
		return aliases;
	}
	
	private static class RunnableMap extends HashMap<String, RunnableMap> implements TriPredicate<MessageReceivedEvent, String, String[]> {
		private static final long serialVersionUID = -4337096481562532645L;
		
		private TriPredicate<MessageReceivedEvent, String, String[]> run = null;
		private PermissionGroup permissionNode = PermissionGroup.DEFAULT;
		
		public void attachRunnable(TriPredicate<MessageReceivedEvent, String, String[]> run, PermissionGroup permissionNode) {
			this.run = run;
			this.permissionNode = permissionNode;
		}
		
		public PermissionGroup getPermissionNode() {
			return permissionNode;
		}
		
		public boolean isRunnable() {
			return run != null;
		}

		@Override
		public boolean test(MessageReceivedEvent e, String command, String[] args) {
			return run.test(e, command, args);
		}
	}
	
	@FunctionalInterface
	public static interface TriPredicate<A,B,C> {
		boolean test(A a, B b, C c);

	    default TriPredicate<A, B, C> and(TriPredicate<? super A, ? super B, ? super C> other) {
	        Objects.requireNonNull(other);
	        return (A a, B b, C c) -> test(a, b, c) && other.test(a, b, c);
	    }
	}
	
}
