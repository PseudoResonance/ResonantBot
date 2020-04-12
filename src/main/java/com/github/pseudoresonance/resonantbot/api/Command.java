package com.github.pseudoresonance.resonantbot.api;

import java.util.HashSet;

import com.github.pseudoresonance.resonantbot.permissions.PermissionGroup;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
	
	private String name;
	private String descriptionKey;
	private PermissionGroup permissionNode;
	private Plugin plugin;
	private String[] aliases;
	
	public String getName() {
		return name;
	}
	
	public String getDescriptionKey() {
		return descriptionKey;
	}
	
	public PermissionGroup getPermissionNode() {
		return permissionNode;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public String[] getAliases() {
		return aliases;
	}
	
	/**
	 * Run when a command is to be executed
	 * 
	 * @param e Message received event from JDA
	 * @param command Command that was executed
	 * @param userPermissions {@link HashSet} containing calculated user permissions
	 * @param args Arguments to the command
	 */
	public abstract void onCommand(MessageReceivedEvent e, String command, HashSet<PermissionGroup> userPermissions, String[] args);

}