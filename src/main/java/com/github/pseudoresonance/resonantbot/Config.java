package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
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

	private static HashMap<String, Object> map;

	public static boolean isTokenSet() {
		if (token.equals("") || token == null) {
			return false;
		} else {
			return true;
		}
	}

	protected static void setToken(String token) {
		Config.token = token;
		map.put("token", token);
	}

	protected static String getToken() {
		return token;
	}

	protected static void setPrefix(String prefix) {
		Config.prefix = prefix;
		map.put("prefix", prefix);
	}

	public static String getPrefix() {
		return prefix;
	}

	protected static void setName(String name) {
		Config.name = name;
		map.put("name", name);
	}

	public static String getName() {
		return name;
	}

	public static void setOwner(long owner) {
		Config.owner = owner;
		map.put("owner", owner);
	}

	public static void setOwner(String owner) {
		Config.owner = Long.valueOf(owner);
	}

	public static long getOwner() {
		return owner;
	}

	public static void setStatus(String status) {
		Config.status = status;
		map.put("status", status);
	}

	public static void setStatusType(Game.GameType statusType) {
		Config.statusType = statusType;
		map.put("statusType", statusType.toString());
	}

	public static void setStatus(Game.GameType statusType, String status) {
		Config.status = status;
		Config.statusType = statusType;
		map.put("status", status);
		map.put("statusType", statusType.toString());
	}

	public static String getStatus() {
		String status = Config.status;
		if (ResonantBot.getClient() != null) {
			status = status.replaceAll(Pattern.quote("%prefix%"), prefix);
			status = status.replaceAll(Pattern.quote("%servers%"),
					String.valueOf(ResonantBot.getClient().getGuilds().size()));
			status = status.replaceAll(Pattern.quote("%ping%"),
					String.valueOf(ResonantBot.getClient().getAveragePing()) + "ms");
			status = status.replaceAll(Pattern.quote("%shards%"),
					String.valueOf(ResonantBot.getClient().getShardsTotal()));
		} else {
			status = prefix + "help";
		}
		return status;
	}

	public static Game.GameType getStatusType() {
		return statusType;
	}

	public static Game getGame() {
		Game game = Game.listening(prefix + "help");
		;
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
				} catch (NullPointerException e) {
				}
				try {
					prefix = config.getString("prefix");
				} catch (NullPointerException e) {
				}
				try {
					name = config.getString("name");
				} catch (NullPointerException e) {
				}
				try {
					owner = Long.valueOf(config.getString("owner"));
				} catch (NullPointerException e) {
				}
				try {
					status = config.getString("status");
				} catch (NullPointerException e) {
				}
				try {
					statusType = Game.GameType.valueOf(config.getString("statusType").toUpperCase());
				} catch (NullPointerException e) {
				}
				conf = null;
				Config.map = toMap(config);
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
					} catch (NumberFormatException e) {
					}
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

	public static void save() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof BigDecimal) {
				builder.add(entry.getKey(), (BigDecimal) value);
			} else if (value instanceof BigInteger) {
				builder.add(entry.getKey(), (BigInteger) value);
			} else if (value instanceof Boolean) {
				builder.add(entry.getKey(), (Boolean) value);
			} else if (value instanceof Double) {
				builder.add(entry.getKey(), (Double) value);
			} else if (value instanceof Integer) {
				builder.add(entry.getKey(), (Integer) value);
			} else if (value instanceof JsonArrayBuilder) {
				builder.add(entry.getKey(), (JsonArrayBuilder) value);
			} else if (value instanceof JsonObjectBuilder) {
				builder.add(entry.getKey(), (JsonObjectBuilder) value);
			} else if (value instanceof JsonValue) {
				builder.add(entry.getKey(), (JsonValue) value);
			} else if (value instanceof Long) {
				builder.add(entry.getKey(), (Long) value);
			} else if (value instanceof String) {
				builder.add(entry.getKey(), (String) value);
			}
		}
		File dir = new File(ResonantBot.getDir(), "data");
		dir.mkdir();
		File conf = new File(dir, "config.json");
		try {
			FileOutputStream os = new FileOutputStream(conf);
			JsonWriter json = Json.createWriter(os);
			json.writeObject(builder.build());
			json.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static HashMap<String, Object> jsonToMap(JsonObject json) {
    	HashMap<String, Object> retMap = new HashMap<String, Object>();

        if(json != JsonObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static HashMap<String, Object> toMap(JsonObject object) throws JsonException {
    	HashMap<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keySet().iterator();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JsonArray) {
                value = toList((JsonArray) value);
            }

            else if(value instanceof JsonObject) {
                value = toMap((JsonObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static ArrayList<Object> toList(JsonArray array) {
        ArrayList<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if(value instanceof JsonArray) {
                value = toList((JsonArray) value);
            }

            else if(value instanceof JsonObject) {
                value = toMap((JsonObject) value);
            }
            list.add(value);
        }
        return list;
    }

	public static boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public static boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public static Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	public static Object get(Object key) {
		return map.get(key);
	}

	public static Object getOrDefault(Object key, Object defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	public static Set<String> keySet() {
		return map.keySet();
	}

	public static Object put(String key, Object value) {
		return map.put(key, value);
	}

	public static void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}

	public static Object putIfAbsent(String key, Object value) {
		return map.putIfAbsent(key, value);
	}

	public static boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}

	public static Object remove(Object key) {
		return map.remove(key);
	}

	public static boolean replace(String key, Object oldValue, Object newValue) {
		return map.replace(key, oldValue, newValue);
	}

	public static Object replace(String key, Object value) {
		return map.replace(key, value);
	}

	public static int size() {
		return map.size();
	}

	public static Collection<Object> values() {
		return map.values();
	}

}
