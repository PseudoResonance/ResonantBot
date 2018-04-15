package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.listeners.ConnectionListener;
import com.github.pseudoresonance.resonantbot.listeners.GuildListener;
import com.github.pseudoresonance.resonantbot.listeners.MessageListener;
import com.github.pseudoresonance.resonantbot.listeners.ReadyListener;

import sx.blah.discord.api.IDiscordClient;

public class ResonantBot {

	private static Logger log = LoggerFactory.getLogger(ResonantBot.class.getName());

	private static HashMap<String, String> args;
	private static String directory;
	private static IDiscordClient client;
	
	private static boolean ready = false;

	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Shutting down!");
				Config.save();
				Config.saveData();
				log.info("Configuration saved!");
				for (Plugin p : PluginManager.getPlugins()) {
					PluginManager.unload(p);
				}
				log.info("Plugins unloaded!");
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Logging out!");
				if (client != null) {
					client.logout();
				}
			}
		});
		ResonantBot.args = Startup.parseArgs(args);
		directory = Startup.init();
		if (directory == "") {
			System.exit(0);
		}
		log.info("Using directory: " + directory);
		log.info("Starting up ResonantBot");
		Config.init();
		if (!Config.isTokenSet()) {
			Scanner scanner = new Scanner(System.in);
			log.debug("No token found. Prompting for token.");
			String token = "";
			while (true) {
				System.out.println(Color.BRIGHT_WHITE + "No token is set for the bot! Please input a token:");
				token = scanner.nextLine();
				if (token.length() > 0) {
					break;
				}
			}
			log.debug("Token received.");
			log.debug("Prompting for prefix.");
			System.out.println(Color.BRIGHT_WHITE + "\nPlease input the default prefix: (or leave blank for default: '|')");
			String prefix = scanner.nextLine();
			log.debug("Prefix received.");
			if (prefix.length() == 0) {
				prefix = "|";
				log.debug("Prefix blank. Using default: '|'");
			}
			log.debug("Prompting for owner.");
			String owner = "";
			while (true) {
				System.out.println(Color.BRIGHT_WHITE + "\nPlease input the id of the bot owner:");
				owner = scanner.nextLine();
				if (owner.length() > 0) {
					break;
				}
			}
			log.debug("Owner received.");
			log.debug("Prompting for name.");
			System.out.println(Color.BRIGHT_WHITE + "\nPlease name the bot: (or leave blank for default: 'ResonantBot')");
			String name = scanner.nextLine();
			log.debug("Name received.");
			if (name.length() == 0) {
				name = "ResonantBot";
				log.debug("Name blank. Using default: 'ResonantBot'");
			}
			scanner.close();
			Config.setToken(token);
			Config.setPrefix(prefix);
			Config.setOwner(owner);
			Config.save();
		}
		new File(directory, "plugins").mkdir();
		client = BotUtils.getBuiltDiscordClient(Config.getToken());
		client.getDispatcher().registerListeners(new MessageListener(), new ReadyListener(), new ConnectionListener());
		PluginManager.reload();
		client.login();
	}
	
	protected static void ready() {
		if (!ready) {
			ready = true;
			client.getDispatcher().registerListener(new GuildListener());
		}
	}

	public static String getArg(String arg) {
		if (args.containsKey(arg)) {
			return args.get(arg);
		} else {
			return null;
		}
	}

	public static String getDir() {
		return directory;
	}

	public static IDiscordClient getClient() {
		return client;
	}

	public static Logger getLogger() {
		return log;
	}

}
