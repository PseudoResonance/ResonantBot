package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.IDiscordClient;

public class ResonantBot {
	
	private static HashMap<String, String> args;
	private static String directory;
	private static Logger log = LoggerFactory.getLogger(ResonantBot.class.getName());
	private static IDiscordClient client;

	public static void main(String[] args) {
		ResonantBot.args = Startup.parseArgs(args);
		directory = Startup.init();
		if (directory == "") {
			System.exit(0);
		}
		log.info("Starting up ResonantBot");
		Config.init();
		if (!Config.isTokenSet()) {
			Scanner scanner = new Scanner(System.in);
			log.debug("No token found. Prompting for token.");
			System.out.println(Color.BRIGHT_WHITE + "No token is set for the bot! Please input a token:");
			String token = scanner.nextLine();
			log.debug("Token received.");
			log.debug("Prompting for prefix.");
			System.out.println(Color.BRIGHT_WHITE + "\nPlease input the default prefix: (or leave blank for default: '|')");
			String prefix = scanner.nextLine();
			log.debug("Prefix received.");
			if (prefix.length() == 0) {
				prefix = "|";
				log.debug("Prefix blank. Using default: '|'");
			}
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
			Config.save();
		}
		new File(directory, "commands").mkdir();
		JarManager.getJars();
		JarManager.loadJars();
		client = BotUtils.getBuiltDiscordClient(Config.getToken());
		client.getDispatcher().registerListener(new EventListen());
		client.login();
		boolean loop = true;
		while (loop) {
			if (client.isLoggedIn()) {
				loop = false;
				log.info(Color.BRIGHT_CYAN + "Successfully connected to Discord as " + client.getApplicationName() + "!");
				log.info(Color.BRIGHT_CYAN + "Use the following link to add me to your guild:\n");
				log.info("https://discordapp.com/oauth2/authorize?client_id=" + client.getApplicationClientID() + "&scope=bot&permissions=2146958591\n");
			}
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
	
	public static Logger getLogger() {
		return log;
	}
	
	public static IDiscordClient getClient() {
		return client;
	}
	
}
