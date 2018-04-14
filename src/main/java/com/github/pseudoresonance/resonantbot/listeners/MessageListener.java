package com.github.pseudoresonance.resonantbot.listeners;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.github.pseudoresonance.resonantbot.BotUtils;
import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.ResonantBot;
import com.github.pseudoresonance.resonantbot.api.Command;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MessageListener {
	
	private static HashMap<String, String> prefixes = new HashMap<String, String>();

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		String message = e.getMessage().getContent();
		if (e.getMessage().getMentions().contains(ResonantBot.getClient().getOurUser())) {
			if (e.getChannel().isPrivate()) {
				BotUtils.sendMessage(e.getChannel(), "The prefix for DMs is `" + Config.getPrefix() + "`");
			} else {
				BotUtils.sendMessage(e.getChannel(), "The prefix for " + e.getGuild().getName() + " is `" + getPrefix(e.getGuild().getStringID()) + "`");
			}
		} else {
			String prefix = Config.getPrefix();
			if (!e.getChannel().isPrivate()) {
				String id = e.getGuild().getStringID();
				prefix = getPrefix(id);
			}
			if (message.startsWith(prefix)) {
				String command = message.replaceFirst(Pattern.quote(prefix), "");
				if (!command.equals("")) {
					String[] parts = command.split(" ");
					String[] args = ArrayUtils.remove(parts, 0);
					Command c = CommandManager.getCommand(parts[0].toLowerCase());
					if (c != null) {
						try {
							c.onCommand(e, parts[0], args);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						return;
					} else {
						return;
					}
				} else {
					return;
				}
			}
		}
	}
	
	public static String getPrefix(String guild) {
		if (prefixes.containsKey(guild)) {
			return prefixes.get(guild);
		} else {
			return Config.getPrefix();
		}
	}
	
	public static void setPrefix(String guild, String prefix) {
		prefixes.put(guild, prefix);
		Config.saveData();
	}
	
	public static HashMap<String, String> getPrefixes() {
		return prefixes;
	}
	
	public static void setPrefixes(HashMap<String, String> prefixes) {
		MessageListener.prefixes = prefixes;
	}

}