package com.github.pseudoresonance.resonantbot.listeners;

import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.Language;
import com.github.pseudoresonance.resonantbot.ResonantBot;
import com.github.pseudoresonance.resonantbot.api.Command;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	
	private static HashMap<Long, String> prefixes = new HashMap<Long, String>();

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		String message = e.getMessage().getContentRaw();
		String prefix = "";
		if (e.getChannelType() == ChannelType.PRIVATE) {
			prefix = getPrefix(e.getPrivateChannel().getIdLong());
		} else {
			prefix = getPrefix(e.getGuild().getIdLong());
		}
		if (message.startsWith(prefix)) {
			String command = message.substring(prefix.length());
			String[] parts = command.split("\\s+");
			Command c = CommandManager.getCommand(parts[0].toLowerCase());
			if (c != null) {
				try {
					String[] args = new String[0];
					if (parts.length > 1) {
						args = ArrayUtils.remove(parts, 0);
					}
					c.onCommand(e, parts[0], args);
				} catch (Exception ex) {
					ResonantBot.getLogger().error("Error on Message \"" + e.getMessage().getContentRaw() + "\" (" + e.getMessageId() + ") from " + e.getAuthor().getId() + "\n", ex);
					e.getChannel().sendMessage(Language.getMessage(e, "main.errorOccurred")).queue();
				}
			}
			return;
		}
		if (message.startsWith("<@" + e.getJDA().getSelfUser().getId() + ">")) {
			if (e.getChannelType() == ChannelType.PRIVATE) {
				e.getChannel().sendMessage(Language.getMessage(e.getPrivateChannel().getIdLong(), "main.privatePrefix", getPrefix(e.getPrivateChannel().getIdLong()))).queue();
			} else {
				e.getChannel().sendMessage(Language.getMessage(e.getGuild().getIdLong(), "main.prefix", e.getGuild().getName(), getPrefix(e.getGuild().getIdLong()))).queue();
			}
		}
	}
	
	public static String getPrefix(Long guildId) {
		String prefix = prefixes.get(guildId);
		if (prefix != null)
			return prefix;
		return Config.getPrefix();
	}
	
	public static String getPrefix(GenericMessageEvent e) {
		Long id = 0L;
		if (e.getChannelType() == ChannelType.PRIVATE) {
			id = e.getPrivateChannel().getIdLong();
		} else {
			id = e.getGuild().getIdLong();
		}
		String prefix = prefixes.get(id);
		if (prefix != null)
			return prefix;
		return Config.getPrefix();
	}
	
	public static void setPrefix(Long guild, String prefix) {
		if (prefix.equals(Config.getPrefix()))
			prefixes.remove(guild);
		else
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
