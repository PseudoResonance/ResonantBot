package com.github.pseudoresonance.resonantbot.listeners;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.ArrayUtils;

import com.github.pseudoresonance.resonantbot.CommandManager;
import com.github.pseudoresonance.resonantbot.ResonantBot;
import com.github.pseudoresonance.resonantbot.api.Command;
import com.github.pseudoresonance.resonantbot.data.Data;
import com.github.pseudoresonance.resonantbot.language.LanguageManager;
import com.github.pseudoresonance.resonantbot.log.MessageErrorLogger;
import com.github.pseudoresonance.resonantbot.permissions.PermissionGroup;
import com.github.pseudoresonance.resonantbot.permissions.PermissionManager;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	
	private static final MessageErrorLogger logger = new MessageErrorLogger(new File(ResonantBot.getBot().getDirectory(), "errors"));
	ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		String message = e.getMessage().getContentRaw();
		String prefix = "";
		prefix = getPrefix(e);
		if (message.startsWith(prefix)) {
			String command = message.substring(prefix.length());
			String[] parts = command.split("\\s+");
			Command c = CommandManager.getCommand(parts[0].toLowerCase());
			if (c != null) {
				HashSet<PermissionGroup> permissions = PermissionManager.getUserPermissions(e.getMember(), e.getAuthor());
				if (c.getPermissionNode() == PermissionGroup.DEFAULT || permissions.contains(c.getPermissionNode())) {
					executor.execute(() -> {
						try {
							String[] args = new String[0];
							if (parts.length > 1) {
								args = ArrayUtils.remove(parts, 0);
							}
							c.onCommand(e, parts[0], permissions, args);
						} catch (Exception ex) {
							String error = "Error on Message: \"" + e.getMessage().getContentRaw() + "\" (" + e.getMessageId() + ") from: " + e.getAuthor().getName() + " (" + e.getAuthor().getId() + ") in ";
							if (e.getChannelType() == ChannelType.PRIVATE)
								error += "Direct Messages";
							else
								error += "Guild: " + e.getGuild().getName() + " (" + e.getGuild().getId() + ")";
							ResonantBot.getBot().getLogger().error(error + "\n", ex);
							logger.logError(error, ex);
							e.getChannel().sendMessage(LanguageManager.getLanguage(e).getMessage("main.errorOccurred")).queue();
						}
					});
				} else
					e.getChannel().sendMessage(LanguageManager.getLanguage(e).getMessage("main.noPermission", parts[0])).queue();
			}
			return;
		}
		if (message.startsWith("<@" + e.getJDA().getSelfUser().getId() + ">") || message.startsWith("<@!" + e.getJDA().getSelfUser().getId() + ">")) {
			if (e.getChannelType() == ChannelType.PRIVATE)
				e.getChannel().sendMessage(LanguageManager.getLanguage(e.getChannel().getIdLong()).getMessage("main.privatePrefix", Data.getGuildPrefix(e.getPrivateChannel().getIdLong()))).queue();
			else
				e.getChannel().sendMessage(LanguageManager.getLanguage(e.getGuild().getIdLong()).getMessage("main.prefix", e.getGuild().getName(), Data.getGuildPrefix(e.getGuild().getIdLong()))).queue();
		}
	}
	
	public static String getPrefix(GenericMessageEvent e) {
		return Data.getGuildPrefix((e.getChannelType() == ChannelType.PRIVATE ? e.getPrivateChannel() : e.getGuild()).getIdLong());
	}

}
