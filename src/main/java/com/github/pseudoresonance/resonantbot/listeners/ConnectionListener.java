package com.github.pseudoresonance.resonantbot.listeners;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.ResonantBot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;

public class ConnectionListener {
	
	private static Logger log = ResonantBot.getLogger();

	@EventSubscriber
	public void onDisconnect(DisconnectedEvent e) {
		log.warn("Disconnected from Discord for reason: " + e.getReason());
		log.info("Attempting to login.");
		e.getShard().login();
	}

	@EventSubscriber
	public void onResonnectFail(ReconnectFailureEvent e) {
		log.warn("Shard " + e.getShard().toString() + " disconnected from Discord on " + e.getCurrentAttempt() + " try.");
		if (!e.isShardAbandoned()) {
			log.info("Attempting to login.");
			e.getShard().login();
		}
	}

	@EventSubscriber
	public void onResonnectSuccess(ReconnectSuccessEvent e) {
		log.warn("Shard " + e.getShard().toString() + " reconnected to Discord.");
	}
	
}
