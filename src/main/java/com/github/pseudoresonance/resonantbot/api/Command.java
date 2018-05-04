package com.github.pseudoresonance.resonantbot.api;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface Command {
	
	public void onCommand(MessageReceivedEvent e, String command, String[] args);
	
	public String getDesc();
	
	public boolean isHidden();

}