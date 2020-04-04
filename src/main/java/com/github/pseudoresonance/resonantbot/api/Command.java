package com.github.pseudoresonance.resonantbot.api;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
	
	private String name;
	private String descriptionKey;
	private String permissionNode;
	
	public String getName() {
		return name;
	}
	
	public String getDescriptionKey() {
		return descriptionKey;
	}
	
	public String getPermissionNode() {
		return permissionNode;
	}
	
	public abstract void onCommand(MessageReceivedEvent e, String command, String[] args);

}