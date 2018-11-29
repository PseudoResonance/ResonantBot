package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.data.Backend;
import com.github.pseudoresonance.resonantbot.data.Data;
import com.github.pseudoresonance.resonantbot.data.FileBackend;
import com.github.pseudoresonance.resonantbot.data.MySQLBackend;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

public class Config {
	
	private static Logger log = null;
	
	private static YamlFile yaml = null;

	private static String token = "";
	private static String name = "ResonantBot";
	private static String prefix = "|";
	private static long owner = 0;
	private static String lang = "en-US";
	private static Game.GameType statusType = Game.GameType.LISTENING;
	private static String status = "%servers% Servers | %prefix%help";
	
	private static String backend = "file";
	private static HashMap<String, Backend> backends = new HashMap<String, Backend>();
	
	public static void loadConfig() {
		log.debug("Loading config");
		File f = ResonantBot.copyFileFromJar("config.yml");
		try {
			yaml = new YamlFile(f);
			try {
				yaml.load();
			} catch (InvalidConfigurationException | IOException e) {
				log.error("Could not load config file! Shutting down!", e);
			}
			token = yaml.getString("Bot.Token");
			if (token == null) token = "";
			name = yaml.getString("Bot.Name");
			if (name == null) name = "ResonantBot";
			prefix = yaml.getString("Bot.Prefix");
			if (prefix == null) prefix = "|";
			String ownerTemp = yaml.getString("Bot.Owner");
			owner = (ownerTemp == null) ? 0L : Long.valueOf(ownerTemp);
			lang = yaml.getString("Bot.Lang");
			if (lang == null) lang = "en-US";
			String statusTypeTemp = yaml.getString("Bot.StatusType");
			if (statusTypeTemp == null) statusTypeTemp = "LISTENING";
			String statusTypeString = statusTypeTemp.toUpperCase().trim();
			if (statusTypeString.equals("PLAYING"))
				statusTypeString = "DEFAULT";
			statusType = Game.GameType.valueOf(statusTypeString);
			status = yaml.getString("Bot.Status");
			if (status == null) status = "%servers% Servers | %prefix%help";
			
			backend = yaml.getString("Data.Backend");
			if (backend == null) backend = "";
			ConfigurationSection cs = yaml.getConfigurationSection("Data.Backends");
			HashMap<String, Backend> backends = new HashMap<String, Backend>();
			for (String key : cs.getKeys(false)) {
				log.debug("Reading backend: " + key + " from config");
				ConfigurationSection be = cs.getConfigurationSection(key);
				String type = be.getString("type");
				switch (type) {
				case "mysql":
					String host = be.getString("host");
					int port = be.getInt("port");
					String username = be.getString("username");
					String password = be.getString("password");
					String database = be.getString("database");
					String prefix = be.getString("prefix");
					boolean ssl = be.getBoolean("useSSL");
					if (host != null && host.equals("")) {
						log.error("Host for backend: " + key + " is missing!");
						break;
					} else if (database != null && database.equals("")) {
						log.error("Database for backend: " + key + " is missing!");
						break;
					} else if (port == 0) {
						log.error("Port for backend: " + key + " is missing!");
						break;
					}
					MySQLBackend mb = new MySQLBackend(key, host, port, username, password, database, prefix, ssl);
					backends.put(key, mb);
					break;
				case "file":
				default:
					String dir = be.getString("directory");
					if (dir != null) {
						File file = new File(ResonantBot.getDir(), dir);
						FileBackend fb = new FileBackend(key, file);
						backends.put(key, fb);
					} else {
						log.error("Directory for backend: " + key + " is missing!");
					}
				}
			}
			if (backends.size() > 0) {
				Config.backends = backends;
				if (!backends.containsKey(backend)) {
					log.error("Selected backend: " + backend + " does not exist in config!");
					System.exit(1);
				}
			} else {
				log.error("No valid backends configured in config!");
				System.exit(1);
			}
		} catch (IllegalArgumentException e) {
			log.error("Could not load default config! Please download a new copy of this bot!", e);
			System.exit(1);
		}
	}

	public static boolean isTokenSet() {
		if (yaml == null)
			loadConfig();
		if (token.equals("") || token == null) {
			return false;
		} else {
			return true;
		}
	}

	protected static String getToken() {
		if (yaml == null)
			loadConfig();
		return token;
	}

	protected static void setPrefix(String prefix) {
		if (yaml == null)
			loadConfig();
		Config.prefix = prefix;
		yaml.set("Bot.Prefix", prefix);
		saveConfig();
	}

	public static String getPrefix() {
		if (yaml == null)
			loadConfig();
		return prefix;
	}

	protected static void setName(String name) {
		if (yaml == null)
			loadConfig();
		Config.name = name;
		yaml.set("Bot.Name", name);
		saveConfig();
	}

	public static String getName() {
		if (yaml == null)
			loadConfig();
		return name;
	}

	public static void setOwner(long owner) {
		if (yaml == null)
			loadConfig();
		Config.owner = owner;
		yaml.set("Bot.Owner", owner);
		saveConfig();
	}

	public static void setOwner(String owner) {
		setOwner(Long.valueOf(owner));
	}

	public static long getOwner() {
		if (yaml == null)
			loadConfig();
		return owner;
	}

	public static void setLang(String lang) {
		if (yaml == null)
			loadConfig();
		Config.lang = lang;
		yaml.set("Bot.Lang", lang);
		saveConfig();
	}

	public static String getLang() {
		if (yaml == null)
			loadConfig();
		return lang;
	}

	public static void setStatus(String status) {
		if (yaml == null)
			loadConfig();
		Config.status = status;
		yaml.set("Bot.Status", status);
		saveConfig();
	}

	public static void setStatusType(Game.GameType statusType) {
		if (yaml == null)
			loadConfig();
		Config.statusType = statusType;
		yaml.set("Bot.StatusType", statusType.toString());
		saveConfig();
	}

	public static void setStatus(Game.GameType statusType, String status) {
		if (yaml == null)
			loadConfig();
		Config.status = status;
		Config.statusType = statusType;
		yaml.set("Bot.Status", status);
		yaml.set("Bot.StatusType", statusType.toString());
		saveConfig();
	}

	public static String getStatus() {
		if (yaml == null)
			loadConfig();
		String status = Config.status;
		if (ResonantBot.getJDA() != null) {
			status = status.replaceAll(Pattern.quote("%prefix%"), prefix);
			status = status.replaceAll(Pattern.quote("%servers%"),
					String.valueOf(ResonantBot.getJDA().getGuilds().size()));
			status = status.replaceAll(Pattern.quote("%ping%"),
					String.valueOf(ResonantBot.getJDA().getAveragePing()) + "ms");
			status = status.replaceAll(Pattern.quote("%shards%"),
					String.valueOf(ResonantBot.getJDA().getShardsTotal()));
		} else {
			status = prefix + "help";
		}
		return status;
	}

	public static Game.GameType getStatusType() {
		if (yaml == null)
			loadConfig();
		return statusType;
	}
	
	public static void setBackend(String name) {
		if (yaml == null)
			loadConfig();
		for (String n : backends.keySet()) {
			if (n.equals(name)) {
				yaml.set("Data.Backend", name);
				backend = name;
			}
		}
		saveConfig();
		Data.updateBackend();
	}
	
	public static Backend getBackend() {
		if (yaml == null)
			loadConfig();
		return backends.get(backend);
	}
	
	public static String getBackendName() {
		if (yaml == null)
			loadConfig();
		return backend;
	}

	
	public static HashMap<String, Backend> getBackends() {
		if (yaml == null)
			loadConfig();
		return backends;
	}

	public static Game getGame() {
		if (yaml == null)
			loadConfig();
		Game game = Game.listening(prefix + "help");
		if (statusType != Game.GameType.STREAMING)
			game = Game.of(statusType, getStatus());
		else {
			String[] split = getStatus().split(Pattern.quote("|"), 2);
			game = Game.of(statusType, split[0], split[1]);
		}
		return game;
	}

	public static void updateStatus() {
		List<JDA> statuses = ResonantBot.getJDA().getShards();
		Game game = getGame();
		for (JDA jda : statuses) {
			jda.getPresence().setGame(game);
		}
	}

	protected static void init(Logger log) {
		Config.log = log;
		log.debug("Initializing config");
		loadConfig();
		log.debug("Loading data");
		Data.init(log);
	}
	
	private synchronized static void saveConfig() {
		try {
			yaml.saveWithComments();
		} catch (IOException e) {
			log.error("Could not save config!", e);
		}
	}

}
