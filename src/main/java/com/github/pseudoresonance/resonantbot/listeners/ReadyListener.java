package com.github.pseudoresonance.resonantbot.listeners;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.Color;
import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.ResonantBot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

public class ReadyListener {
	
	private static Logger log = ResonantBot.getLogger();

	@EventSubscriber
	public void onReady(ReadyEvent e) {
		IDiscordClient client = ResonantBot.getClient();
		log.info(Color.BRIGHT_CYAN + "Successfully connected to Discord as " + client.getApplicationName() + "!");
		log.info(Color.BRIGHT_CYAN + "Use the following link to add me to your guild:\n");
		log.info("https://discordapp.com/oauth2/authorize?client_id=" + client.getApplicationClientID() + "&scope=bot&permissions=2146958591\n");
		client.changePresence(StatusType.ONLINE, ActivityType.LISTENING, Config.getPrefix() + "help | " + Config.getName());
	}

}
