package com.github.pseudoresonance.resonantbot.listeners;

import com.github.pseudoresonance.resonantbot.Config;

import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ConnectionListener extends ListenerAdapter {

	@Override
	public void onReconnect(ReconnectedEvent e) {
		e.getJDA().getPresence().setGame(Config.getGame());
	}

	@Override
	public void onResume(ResumedEvent e) {
		e.getJDA().getPresence().setGame(Config.getGame());
	}
	
}
