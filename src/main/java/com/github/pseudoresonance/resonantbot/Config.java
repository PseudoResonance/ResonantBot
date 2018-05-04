package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import com.github.pseudoresonance.resonantbot.listeners.MessageListener;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

public class Config {
	
	private static String token = "";
	private static String prefix = "|";
	private static String name = "ResonantBot";
	private static long owner = 0;
	private static String status = "%servers% Servers | %prefix%help";
	private static Game.GameType statusType = Game.GameType.LISTENING;

	public static boolean isTokenSet() {
		if (token.equals("") || token == null) {
			return false;
		} else {
			return true;
		}
	}
	
	protected static void setToken(String token) {
		Config.token = token;
	}
	
	protected static String getToken() {
		return token;
	}
	
	protected static void setPrefix(String prefix) {
		Config.prefix = prefix;
	}
	
	public static String getPrefix() {
		return prefix;
	}
	
	protected static void setName(String name) {
		Config.name = name;
	}
	
	public static String getName() {
		return name;
	}
	
	public static void setOwner(long owner) {
		Config.owner = owner;
	}
	
	public static void setOwner(String owner) {
		Config.owner = Long.valueOf(owner);
	}
	
	public static long getOwner() {
		return owner;
	}
	
	public static void setStatus(String status) {
		Config.status = status;
	}
	
	public static void setStatusType(Game.GameType statusType) {
		Config.statusType = statusType;
	}
	
	public static void setStatus(Game.GameType statusType, String status) {
		Config.status = status;
		Config.statusType = statusType;
	}
	
	public static String getStatus() {
		String status = Config.status;
		if (ResonantBot.getClient() != null) {
			status = status.replaceAll(Pattern.quote("%prefix%"), prefix);
			status = status.replaceAll(Pattern.quote("%servers%"), String.valueOf(ResonantBot.getClient().getGuilds().size()));
			status = status.replaceAll(Pattern.quote("%ping%"), String.valueOf(ResonantBot.getClient().getAveragePing()) + "ms");
			status = status.replaceAll(Pattern.quote("%shards%"), String.valueOf(ResonantBot.getClient().getShardsTotal()));
		} else {
			status = prefix + "help";
		}
		return status;
	}
	
	public static Game.GameType getStatusType() {
		return statusType;
	}
	
	public static Game getGame() {
		Game game = Game.listening(prefix + "help");;
		if (statusType != Game.GameType.STREAMING)
			game = Game.of(statusType, getStatus());
		else {
			String[] split = getStatus().split(Pattern.quote("|"), 2);
			game = Game.of(statusType, split[0], split[1]);
		}
		return game;
	}
	
	public static void updateStatus() {
		List<JDA> statuses = ResonantBot.getClient().getShards();
		Game game = getGame();
		for (JDA jda : statuses) {
			jda.getPresence().setGame(game);
		}
	}
	
	public static void save() {
		File dir = new File(ResonantBot.getDir(), "data");
		dir.mkdir();
		JsonObjectBuilder configBuild = Json.createObjectBuilder();
		configBuild.add("token", token);
		configBuild.add("prefix", prefix);
		configBuild.add("name", name);
		configBuild.add("owner", String.valueOf(owner));
		configBuild.add("status", status);
		configBuild.add("statusType", statusType.toString());
		JsonObject config = configBuild.build();
		File conf = new File(dir, "config.json");
		try {
			FileOutputStream os = new FileOutputStream(conf);
			JsonWriter json = Json.createWriter(os);
			json.writeObject(config);
			json.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean saveData() {
		File dir = new File(ResonantBot.getDir(), "data");
		dir.mkdir();
		JsonObjectBuilder prefixesBuild = Json.createObjectBuilder();
		HashMap<Long, String> prefixes = MessageListener.getPrefixes();
		for (Long p : prefixes.keySet()) {
			prefixesBuild.add(String.valueOf(p), prefixes.get(p));
		}
		JsonObject prefix = prefixesBuild.build();
		File pre = new File(dir, "prefixes.json");
		try {
			FileOutputStream os = new FileOutputStream(pre);
			JsonWriter json = Json.createWriter(os);
			json.writeObject(prefix);
			json.close();
			os.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	protected static void init() {
		File conf = new File(ResonantBot.getDir() + File.separator + "data", "config.json");
		if (conf.exists()) {
			try {
				FileInputStream fs = new FileInputStream(conf);
				JsonReader json = Json.createReader(fs);
				JsonObject config = json.readObject();
				json.close();
				fs.close();
				fs.close();
				try {
					token = config.getString("token");
				} catch (NullPointerException e) {}
				try {
					prefix = config.getString("prefix");
				} catch (NullPointerException e) {}
				try {
					name = config.getString("name");
				} catch (NullPointerException e) {}
				try {
					owner = Long.valueOf(config.getString("owner"));
				} catch (NullPointerException e) {}
				try {
					status = config.getString("status");
				} catch (NullPointerException e) {}
				try {
					statusType = Game.GameType.valueOf(config.getString("statusType").toUpperCase());
				} catch (NullPointerException e) {}
				config = null;
				conf = null;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JsonException e) {
				return;
			}
		}
		File pre = new File(ResonantBot.getDir() + File.separator + "data", "prefixes.json");
		if (pre.exists()) {
			try {
				FileInputStream fs = new FileInputStream(pre);
				JsonReader json = Json.createReader(fs);
				JsonObject prefix = json.readObject();
				json.close();
				fs.close();
				fs.close();
				HashMap<Long, String> prefixes = new HashMap<Long, String>();
				for (String k : prefix.keySet()) {
					try {
						prefixes.put(Long.valueOf(k), prefix.getString(k));
					} catch (NumberFormatException e) {}
				}
				prefix = null;
				pre = null;
				MessageListener.setPrefixes(prefixes);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JsonException e) {
				return;
			}
		}
	}
	
}
