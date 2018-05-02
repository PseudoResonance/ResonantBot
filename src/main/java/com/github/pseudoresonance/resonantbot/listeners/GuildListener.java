package com.github.pseudoresonance.resonantbot.listeners;

import com.github.pseudoresonance.resonantbot.ResonantBot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

public class GuildListener {

	@EventSubscriber
	public void onGuildCreate(GuildCreateEvent e) {
		e.getClient().changePresence(StatusType.ONLINE, ActivityType.LISTENING, ResonantBot.getStatusMessage());
	}

	@EventSubscriber
	public void onGuildLeave(GuildLeaveEvent e) {
		e.getClient().changePresence(StatusType.ONLINE, ActivityType.LISTENING, ResonantBot.getStatusMessage());
	}

}
