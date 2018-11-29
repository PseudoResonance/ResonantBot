package com.github.pseudoresonance.resonantbot.listeners;

import java.io.File;
import org.apache.commons.lang3.ArrayUtils;

import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.Language;
import com.github.pseudoresonance.resonantbot.ResonantBot;
import com.github.pseudoresonance.resonantbot.api.Command;
import com.github.pseudoresonance.resonantbot.data.Data;
import com.github.pseudoresonance.resonantbot.log.MessageErrorLogger;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	
	private static final MessageErrorLogger logger = new MessageErrorLogger(new File(ResonantBot.getDir(), "errors"));

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		String message = e.getMessage().getContentRaw();
		String prefix = "";
		if (e.getChannelType() == ChannelType.PRIVATE) {
			prefix = Data.getGuildPrefix(e.getPrivateChannel().getIdLong());
		} else {
			prefix = Data.getGuildPrefix(e.getGuild().getIdLong());
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
					String error = "Error on Message: \"" + e.getMessage().getContentRaw() + "\" (" + e.getMessageId() + ") from: " + e.getAuthor().getName() + " (" + e.getAuthor().getId() + ") in ";
					if (e.getChannelType() == ChannelType.PRIVATE)
						error += "Direct Messages";
					else
						error += "Guild: " + e.getGuild().getName() + " (" + e.getGuild().getId() + ")";
					ResonantBot.getLogger().error(error + "\n", ex);
					logger.logError(error, ex);
					e.getChannel().sendMessage(Language.getMessage(e, "main.errorOccurred")).queue();
				}
			}
			return;
		}
		if (message.startsWith("<@" + e.getJDA().getSelfUser().getId() + ">")) {
			if (e.getChannelType() == ChannelType.PRIVATE) {
				e.getChannel().sendMessage(Language.getMessage(e.getPrivateChannel().getIdLong(), "main.privatePrefix", Data.getGuildPrefix(e.getPrivateChannel().getIdLong()))).queue();
			} else {
				e.getChannel().sendMessage(Language.getMessage(e.getGuild().getIdLong(), "main.prefix", e.getGuild().getName(), Data.getGuildPrefix(e.getGuild().getIdLong()))).queue();
			}
		}
	}
	
	public static String getPrefix(GenericMessageEvent e) {
		Long id = 0L;
		if (e.getChannelType() == ChannelType.PRIVATE) {
			id = e.getPrivateChannel().getIdLong();
		} else {
			id = e.getGuild().getIdLong();
		}
		return Data.getGuildPrefix(id);
	}

}
