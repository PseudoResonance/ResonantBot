package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.security.auth.login.LoginException;

import org.simpleyaml.configuration.file.YamlConfiguration;
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
	
	private static File langDir;

	private static Timer saveTimer;

	public static void main(String[] args) throws InterruptedException {
		ResonantBot.args = Startup.parseArgs(args);
		directory = Startup.init();
		langDir = new File(directory + File.separator + "localization");
		langDir.mkdir();
		try (InputStream is = ResonantBot.class.getClassLoader().getResourceAsStream("localization/en-US.lang")) {
			if (is != null) {
				File dest = new File(langDir, "en-US.lang");
				if (!dest.exists()) {
					Files.copy(is, dest.toPath());
				}
			} else {
				if (ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("file")) {
					File enUS = new File(directory + "/src/main/resources/localization/en-US.lang");
					File dest = new File(langDir, "en-US.lang");
					if (!dest.exists()) {
						Files.copy(new FileInputStream(enUS), dest.toPath());
					}
				} else {
					log.error("Could not find default en-US language files! Please download a fresh copy of this bot! Shutting down!");
					System.exit(1);
				}
			}
		} catch (IOException e) {
			if (ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("file")) {
				File enUS = new File(directory + "/src/main/resources/localization/en-US.lang");
				File dest = new File(langDir, "en-US.lang");
				try {
					if (!dest.exists()) {
						Files.copy(new FileInputStream(enUS), dest.toPath());
					}
				} catch (IOException e1) {
					log.error("Could not find default en-US language files! Please download a fresh copy of this bot! Shutting down!");
					System.exit(1);
				}
			} else {
				log.error("Could not find default en-US language files! Please download a fresh copy of this bot! Shutting down!");
				System.exit(1);
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				saveTimer.cancel();
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
			System.out.println(
					Color.BRIGHT_WHITE + "\nPlease input the default prefix: (or leave blank for default: '|')");
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
			System.out
					.println(Color.BRIGHT_WHITE + "\nPlease name the bot: (or leave blank for default: 'ResonantBot')");
			String name = scanner.nextLine();
			log.debug("Name received.");
			if (name.length() == 0) {
				name = "ResonantBot";
				log.debug("Name blank. Using default: 'ResonantBot'");
			}
			log.debug("Prompting for language.");
			System.out
					.println(Color.BRIGHT_WHITE + "\nPlease choose a language: (or leave blank for default: 'en-US')");
			String lang = scanner.nextLine();
			log.debug("Language received.");
			if (lang.length() == 0) {
				lang = "en-US";
				log.debug("Language blank. Using default: 'en-US'");
			}
			scanner.close();
			Config.setToken(token);
			Config.setPrefix(prefix);
			Config.setOwner(owner);
			Config.setLang(lang);
			Config.save();
		}
		Language.updateLang(Config.getLang());
		new File(directory, "plugins").mkdir();
		try {
			client = new DefaultShardManagerBuilder().setMaxReconnectDelay(32).setToken(Config.getToken()).setGame(Config.getGame()).build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		client.addEventListener(new MessageListener(), new ReadyListener(), new ConnectionListener(), new GuildListener());
		PluginManager.reload();
		saveTimer = new Timer();
		saveTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Config.saveData();
			}
		}, 300000, 300000);
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

	public static File getLangDir() {
		return langDir;
	}

	public static ShardManager getClient() {
		return client;
	}

	public static Logger getLogger() {
		return log;
	}
	
	public static YamlConfiguration getLanguage(String name, boolean overwrite) {
		File lang = new File(langDir, name + ".lang");
		if (!lang.isFile() || overwrite) {
			if (ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("file")) {
				File srcDir = new File(directory + "/src/main/resources/localization");
				File newLang = new File(srcDir, name + ".lang");
				try {
					Files.copy(new FileInputStream(newLang), lang.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {}
			} else {
				try (InputStream is = ResonantBot.class.getClassLoader().getResourceAsStream("localization/" + name + ".lang")) {
					if (is != null) {
						Files.copy(is, lang.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {}
			}
		}
		if (!lang.exists()) {
			if (!name.equals("en-US")) {
				File en = new File(langDir, "en-US.lang");
				if (en.isFile() && !overwrite)
					lang = en;
				else {
					try (InputStream is = ResonantBot.class.getClassLoader().getResourceAsStream("localization/en-US.lang")) {
						if (is != null) {
							Files.copy(is, en.toPath(), StandardCopyOption.REPLACE_EXISTING);
							lang = en;
						}
					} catch (IOException e) {
						if (ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("file")) {
							File srcDir = new File(directory + "/src/main/resources/localization");
							lang = new File(srcDir, "en-US.lang");
						} else {
							log.error("Could not find default en-US language files! Please download a fresh copy of this bot! Shutting down!");
							System.exit(1);
						}
					}
				}
			} else {
				if (ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("file")) {
					File srcDir = new File(directory + "/src/main/resources/localization");
					lang = new File(srcDir, "en-US.lang");
				} else {
					log.error("Could not find default en-US language files! Please download a fresh copy of this bot! Shutting down!");
					System.exit(1);
				}
			}
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(lang);
		return yaml;
	}
	
	public static YamlConfiguration getLanguage(String name) {
		return getLanguage(name, false);
	}

}
