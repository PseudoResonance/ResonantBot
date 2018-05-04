package com.github.pseudoresonance.resonantbot.listeners;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.Color;
import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.ResonantBot;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {
	
	private static Logger log = ResonantBot.getLogger();

	@Override
	public void onReady(ReadyEvent e) {
		log.info(Color.BRIGHT_CYAN + "Successfully connected to Discord as " + e.getJDA().getSelfUser().getName() + "!" + Color.RESET);
		log.info(Color.BRIGHT_CYAN + "Use the following link to add me to your guild:\n" + Color.RESET);
		log.info("https://discordapp.com/oauth2/authorize?client_id=" + e.getJDA().getSelfUser().getId() + "&scope=bot&permissions=3505222\n");
		e.getJDA().getPresence().setGame(Config.getGame());
	}

}
