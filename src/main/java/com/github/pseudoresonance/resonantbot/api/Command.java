package com.github.pseudoresonance.resonantbot.api;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {
	
	public void onCommand(MessageReceivedEvent e, String command, String[] args);
	
	public String getDesc(long guildID);
	
	public boolean isHidden();

}