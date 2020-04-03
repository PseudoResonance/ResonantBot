package com.github.pseudoresonance.resonantbot.listeners;

import com.github.pseudoresonance.resonantbot.Config;

import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ConnectionListener extends ListenerAdapter {

	@Override
	public void onReconnect(ReconnectedEvent e) {
		e.getJDA().getPresence().setActivity(Config.getActivity());
	}

	@Override
	public void onResume(ResumedEvent e) {
		e.getJDA().getPresence().setActivity(Config.getActivity());
	}
	
}
