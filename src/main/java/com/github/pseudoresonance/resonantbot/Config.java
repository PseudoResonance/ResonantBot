package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import com.github.pseudoresonance.resonantbot.listeners.MessageListener;

public class Config {
	
	private static String token = "";
	private static String prefix = "|";
	private static String name = "ResonantBot";

	public static boolean isTokenSet() {
		if (token == "" || token == null) {
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
	
	public static void save() {
		File dir = new File(ResonantBot.getDir(), "data");
		dir.mkdir();
		JsonObjectBuilder configBuild = Json.createObjectBuilder();
		configBuild.add("token", token);
		configBuild.add("prefix", prefix);
		configBuild.add("name", name);
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
		HashMap<String, String> prefixes = MessageListener.getPrefixes();
		for (String p : prefixes.keySet()) {
			prefixesBuild.add(p, prefixes.get(p));
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
				token = config.getString("token");
				prefix = config.getString("prefix");
				name = config.getString("name");
				
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
				HashMap<String, String> prefixes = new HashMap<String, String>();
				for (String k : prefix.keySet()) {
					prefixes.put(k, prefix.getString(k));
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
