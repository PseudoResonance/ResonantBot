package com.github.pseudoresonance.resonantbot.listeners;

import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.ResonantBot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

public class ConnectionListener {
	
	private static Logger log = ResonantBot.getLogger();

	@EventSubscriber
	public void onDisconnect(DisconnectedEvent e) {
		log.warn("Disconnected from Discord for reason: " + e.getReason());
		log.info("Attempting to reconnect...");
		e.getShard().login();
	}

	@EventSubscriber
	public void onResonnectFail(ReconnectFailureEvent e) {
		log.warn("Shard " + e.getShard().toString() + " disconnected from Discord on " + e.getCurrentAttempt() + " try.");
	}

	@EventSubscriber
	public void onResonnectSuccess(ReconnectSuccessEvent e) {
		log.info("Shard " + e.getShard().toString() + " reconnected to Discord.");
	}

	@EventSubscriber
	public void onLogin(LoginEvent e) {
		log.info("Shard " + e.getShard().toString() + " logged in to Discord.");
		e.getShard().changePresence(StatusType.ONLINE, ActivityType.LISTENING, ResonantBot.getStatusMessage());
	}
	
}
