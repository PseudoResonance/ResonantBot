package com.github.pseudoresonance.resonantbot.listeners;

import com.github.pseudoresonance.resonantbot.Config;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildListener extends ListenerAdapter {

	@Override
	public void onGuildJoin(GuildJoinEvent e) {
		Config.updateStatus();
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent e) {
		Config.updateStatus();
	}

}
