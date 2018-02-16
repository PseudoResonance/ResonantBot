package com.github.pseudoresonance.resonantbot.api;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public abstract class Command {
	
	public abstract void onCommand(MessageReceivedEvent e, String command, String[] args);
	
	public abstract String getDesc();
	
	public abstract boolean isHidden();

}
