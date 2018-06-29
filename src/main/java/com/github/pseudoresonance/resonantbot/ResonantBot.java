package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.listeners.ConnectionListener;
import com.github.pseudoresonance.resonantbot.listeners.GuildListener;
import com.github.pseudoresonance.resonantbot.listeners.MessageListener;
import com.github.pseudoresonance.resonantbot.listeners.ReadyListener;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

public class ResonantBot {

	private static Logger log = LoggerFactory.getLogger(ResonantBot.class.getName());

	private static HashMap<String, String> args;
	private static String directory;
	private static ShardManager client;

	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Shutting down!");
				Config.saveData();
				log.info("Data saved!");
				Config.save();
				log.info("Configuration saved!");
				ArrayList<String> names = new ArrayList<String>();
				for (Plugin p : PluginManager.getPlugins()) {
					names.add(p.getName());
				}
				for (String n : names) {
					PluginManager.unload(n);
				}
				log.info("Plugins unloaded!");
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Logging out!");
				if (client != null) {
					client.shutdown();
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
		try {
			client = new DefaultShardManagerBuilder().setMaxReconnectDelay(32).setToken(Config.getToken()).setGame(Config.getGame()).build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		client.addEventListener(new MessageListener(), new ReadyListener(), new ConnectionListener(), new GuildListener());
		PluginManager.reload();
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

	public static ShardManager getClient() {
		return client;
	}

	public static Logger getLogger() {
		return log;
	}

}
