package com.github.pseudoresonance.resonantbot.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.data.Data;
import com.github.pseudoresonance.resonantbot.language.LanguageManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHandler extends Command {
	
	private final String cmdName;
	private final String descriptionKey;
	private final String permissionNode;
	
	private final RunnableMap commandMap = new RunnableMap();

	/**
	 * Creates a new {@link CommandHandler} with the given command name, description language key
	 * 
	 * Permission node defaults to public
	 * 
	 * @param cmdName Name of command
	 * @param descriptionKey Localization key of command description
	 */
	public CommandHandler(String cmdName, String descriptionKey) {
		this.cmdName = cmdName;
		this.descriptionKey = descriptionKey;
		this.permissionNode = "";
	}

	/**
	 * Creates a new {@link CommandHandler} with the given command name, description language key and permission node
	 * 
	 * @param cmdName Name of command
	 * @param descriptionKey Localization key of command description
	 * @param permissionNode Command permission node
	 */
	public CommandHandler(String cmdName, String descriptionKey, String permissionNode) {
		this.cmdName = cmdName;
		this.descriptionKey = descriptionKey;
		this.permissionNode = permissionNode.toLowerCase();
	}
	
	/**
	 * Registers the command in the {@link CommandManager} when setup is complete
	 * 
	 * @param plugin Plugin to register command with
	 */
	public void register(Plugin plugin) {
		CommandManager.registerCommand(plugin, this, cmdName, descriptionKey, permissionNode);
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
		registerSubcommand(cmdPath, run, "");
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
	public void registerSubcommand(String cmdPath, TriPredicate<MessageReceivedEvent, String, String[]> run, String permissionNode) {
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
	public void onCommand(MessageReceivedEvent e, String command, String[] args) {
		HashSet<String> permissions = Data.getUserPermissions(e.getMember(), e.getAuthor());
		if (permissions.contains(permissionNode)) {
			int lastI = 0;
			RunnableMap lastValidNode = null;
			RunnableMap lastNode = commandMap;
			for (int i = 0; i < args.length; i++) {
				RunnableMap node = lastNode.get(args[i].toLowerCase());
				if (node != null) {
					if (permissions.contains(node.getPermissionNode().toLowerCase())) {
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
				if (permissions.contains(entry.getValue().getPermissionNode().toLowerCase()))
					subCommands += "`" + entry.getKey() + "`, ";
			if (subCommands.length() > 2)
				subCommands = subCommands.substring(0, subCommands.length() - 2);
			else if (subCommands.isEmpty())
				subCommands = LanguageManager.getLanguage(e).getMessage("main.none");
			e.getChannel().sendMessage(LanguageManager.getLanguage(e).getMessage("main.validSubcommands", subCommands)).queue();
		} else
			e.getChannel().sendMessage(LanguageManager.getLanguage(e).getMessage("main.noPermission", command)).queue();
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
	public String getPermissionNode() {
		return permissionNode;
	}
	
	private static class RunnableMap extends HashMap<String, RunnableMap> implements TriPredicate<MessageReceivedEvent, String, String[]> {
		private static final long serialVersionUID = -4337096481562532645L;
		
		private TriPredicate<MessageReceivedEvent, String, String[]> run = null;
		private String permissionNode = "";
		
		public void attachRunnable(TriPredicate<MessageReceivedEvent, String, String[]> run, String permissionNode) {
			this.run = run;
			this.permissionNode = permissionNode;
		}
		
		public String getPermissionNode() {
			return permissionNode;
		}
		
		public boolean isRunnable() {
			return run != null;
		}

		@Override
		public boolean test(MessageReceivedEvent e, String command, String[] args) {
			try {
			if (run != null)
				return run.test(e, command, args);
			} catch (Exception ex) {
			}
			return false;
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
