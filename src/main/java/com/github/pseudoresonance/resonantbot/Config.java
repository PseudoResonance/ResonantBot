package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

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
	
	protected static String getPrefix() {
		return prefix;
	}
	
	protected static void setName(String name) {
		Config.name = name;
	}
	
	protected static String getName() {
		return name;
	}
	
	protected static void save() {
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
				token = config.getString("token");
				prefix = config.getString("prefix");
				name = config.getString("name");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JsonException e) {
				return;
			}
		}
	}
	
}
