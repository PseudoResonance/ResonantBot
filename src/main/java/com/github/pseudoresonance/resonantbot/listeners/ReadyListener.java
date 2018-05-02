package com.github.pseudoresonance.resonantbot.listeners;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.Color;
import com.github.pseudoresonance.resonantbot.ResonantBot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

public class ReadyListener {
	
	private static Logger log = ResonantBot.getLogger();

	@EventSubscriber
	public void onReady(ReadyEvent e) {
		log.info(Color.BRIGHT_CYAN + "Successfully connected to Discord as " + e.getClient().getApplicationName() + "!" + Color.RESET);
		log.info(Color.BRIGHT_CYAN + "Use the following link to add me to your guild:\n" + Color.RESET);
		log.info("https://discordapp.com/oauth2/authorize?client_id=" + e.getClient().getApplicationClientID() + "&scope=bot&permissions=3505222\n");
		e.getClient().changePresence(StatusType.ONLINE, ActivityType.LISTENING, ResonantBot.getStatusMessage());
	}

}
