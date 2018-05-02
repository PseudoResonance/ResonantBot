package com.github.pseudoresonance.resonantbot.listeners;

import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.github.pseudoresonance.resonantbot.BotUtils;
import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.api.Command;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

public class MessageListener {
	
	private static HashMap<Long, String> prefixes = new HashMap<Long, String>();

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		String message = e.getMessage().getContent();
		String prefix = getPrefix(e.getGuild());
		if (message.startsWith(prefix)) {
			String command = message.substring(prefix.length());
			String[] parts = command.split(" ");
			Command c = CommandManager.getCommand(parts[0].toLowerCase());
			if (c != null) {
				try {
					String[] args = new String[0];
					if (parts.length > 1) {
						args = ArrayUtils.remove(parts, 0);
					}
					c.onCommand(e, parts[0], args);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return;
		}
		if (message.startsWith("<@" + e.getClient().getOurUser().getStringID() + ">")) {
			if (e.getChannel().isPrivate()) {
				BotUtils.sendMessage(e.getChannel(), "The prefix for DMs is `" + Config.getPrefix() + "`");
			} else {
				BotUtils.sendMessage(e.getChannel(), "The prefix for " + e.getGuild().getName() + " is `" + getPrefix(e.getGuild()) + "`");
			}
		}
	}
	
	public static String getPrefix(IGuild guild) {
		if (guild != null) {
			String prefix = prefixes.get(guild.getLongID());
			if (prefix != null)
				return prefix;
		}
		return Config.getPrefix();
	}
	
	public static void setPrefix(Long guild, String prefix) {
		prefixes.put(guild, prefix);
		Config.saveData();
	}
	
	public static HashMap<Long, String> getPrefixes() {
		return prefixes;
	}
	
	public static void setPrefixes(HashMap<Long, String> prefixes) {
		MessageListener.prefixes = prefixes;
	}

}
