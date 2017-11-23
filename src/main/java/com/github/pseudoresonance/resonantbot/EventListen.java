package com.github.pseudoresonance.resonantbot;

import java.util.regex.Pattern;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;

public class EventListen {
	
	private static final String shutdownmsg = "Shutting down! Goodbye! 😢";

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent e) {
		String message = e.getMessage().getContent();
		if (message.startsWith(Config.getPrefix())) {
			e.getMessage().addReaction(ReactionEmoji.of("✅"));
			String command = message.replaceFirst(Pattern.quote(Config.getPrefix()), "");
			if (command.equalsIgnoreCase("shutdown")) {
				if (e.getAuthor().getStringID().equals(ResonantBot.getClient().getApplicationOwner().getStringID())) {
					BotUtils.sendMessage(e.getChannel(), shutdownmsg);
				}
			} else if (command.equalsIgnoreCase("ping")) {
				BotUtils.sendMessage(e.getChannel(), "Pong! (" + e.getGuild().getShard().getResponseTime() + "ms)");
			}
			CommandManager.getCommand(command).onCommand(e, command);
		}
	}
	
}
