package com.github.pseudoresonance.resonantbot.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.dbcp2.BasicDataSource;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.Config;

public class Data {
	
	private static Logger log = null;
	
	private static Backend backend = null;

	private static DynamicTable bot = null;
	private static DynamicTable users = null;
	private static DynamicTable guilds = null;

	private static HashMap<Long, String> permissionsCache = new HashMap<Long, String>();
	private static HashMap<Long, String> prefixCache = new HashMap<Long, String>();
	private static HashMap<Long, String> languageCache = new HashMap<Long, String>();
	
	public static void init(Logger log) {
		Data.log = log;
		Data.log.debug("Initializing data storage");
		DynamicTable.init(log);
		Backend b = Config.getBackend();
		backend = b;
		if (b instanceof FileBackend)
			((FileBackend) b).getFolder().mkdirs();
		else if (b instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) b;
			sb.setup();
		}
		bot = new DynamicTable("bot", "VARCHAR(64)");
		bot.setup();
		bot.addColumn(new Column("value", "VARCHAR(128)"));
		users = new DynamicTable("users", "BIGINT UNSIGNED");
		users.setup();
		users.addColumn(new Column("permission_group", "VARCHAR(64)", "DEFAULT"));
		guilds = new DynamicTable("guilds", "BIGINT UNSIGNED");
		guilds.setup();
		guilds.addColumn(new Column("language", "VARCHAR(8)", "en-US"));
		guilds.addColumn(new Column("prefix", "VARCHAR(32)", "|"));
	}
	
	public static void updateBackend() {
		Backend b = Config.getBackend();
		if (!b.equals(backend)) {
			if (b instanceof FileBackend)
				((FileBackend) b).getFolder().mkdirs();
			else if (b instanceof SQLBackend) {
				SQLBackend sb = (SQLBackend) b;
				sb.setup();
			}
		}
		bot.update();
		users.update();
		guilds.update();
	}
	
	public static void shutdown() {
		DynamicTable.shutdownAll();
	}
	
	public static void migrate(Backend from, Backend to) {
		bot.migrateBackends(from, to);
		users.migrateBackends(from, to);
		guilds.migrateBackends(from, to);
	}
	
	public static void addUserColumn(Column col) {
		users.addColumn(col);
	}
	
	public static void addGuildColumn(Column col) {
		guilds.addColumn(col);
	}
	
	public static void setBotSetting(String key, Object val) {
		bot.setSetting(key, "value", val);
	}
	
	public static Object getBotSetting(String key) {
		return bot.getSetting(key, "value");
	}
	
	public static void setUserSetting(String id, String key, Object val) {
		users.setSetting(id, key, val);
	}
	
	public static void setUserSetting(Long id, String key, Object val) {
		setUserSetting(String.valueOf(id), key, val);
	}
	
	public static void setUserSettings(String id, HashMap<String, Object> settings) {
		users.setSettings(id, settings);
	}
	
	public static void setUserSettings(Long id, HashMap<String, Object> settings) {
		setUserSettings(String.valueOf(id), settings);
	}
	
	public static Object getUserSetting(String id, String key) {
		return users.getSetting(id, key);
	}
	
	public static Object getUserSetting(Long id, String key) {
		return getUserSetting(String.valueOf(id), key);
	}
	
	public static HashMap<String, Object> getUserSettings(String id) {
		return users.getSettings(id);
	}
	
	public static HashMap<String, Object> getUserSettings(Long id) {
		return getUserSettings(String.valueOf(id));
	}
	
	public static void setGuildSetting(String id, String key, Object val) {
		guilds.setSetting(id, key, val);
	}
	
	public static void setGuildSetting(Long id, String key, Object val) {
		setGuildSetting(String.valueOf(id), key, val);
	}
	
	public static void setGuildSettings(String id, HashMap<String, Object> settings) {
		guilds.setSettings(id, settings);
	}
	
	public static void setGuildSettings(Long id, HashMap<String, Object> settings) {
		setGuildSettings(String.valueOf(id), settings);
	}
	
	public static Object getGuildSetting(String id, String key) {
		return guilds.getSetting(id, key);
	}
	
	public static Object getGuildSetting(Long id, String key) {
		return getGuildSetting(String.valueOf(id), key);
	}
	
	public static HashMap<String, Object> getGuildSettings(String id) {
		return guilds.getSettings(id);
	}
	
	public static HashMap<String, Object> getGuildSettings(Long id) {
		return getGuildSettings(String.valueOf(id));
	}
	
	public static void setUserPermissions(Long id, String group) {
		permissionsCache.put(id, group);
		setUserSetting(id, "permission_group", group);
	}
	
	public static void setGuildPrefix(Long id, String prefix) {
		prefixCache.put(id, prefix);
		setGuildSetting(id, "prefix", prefix);
	}
	
	public static void setGuildLanguage(Long id, String language) {
		languageCache.put(id, language);
		setGuildSetting(id, "language", language);
	}
	
	public static String getUserPermissions(Long id) {
		if (permissionsCache.size() == 0)
			getGuildInfo();
		String ret = permissionsCache.get(id);
		return (ret == null) ? "DEFAULT" : ret;
	}
	
	public static String getGuildPrefix(Long id) {
		if (prefixCache.size() == 0)
			getGuildInfo();
		String ret = prefixCache.get(id);
		return (ret == null || ret.equals("")) ? Config.getPrefix() : ret;
	}
	
	public static String getGuildLanguage(Long id) {
		if (languageCache.size() == 0)
			getGuildInfo();
		String ret = languageCache.get(id);
		return (ret == null || ret.equals("")) ? Config.getLang() : ret;
	}
	
	public static HashMap<Long, String> getGuildLanguages() {
		return languageCache;
	}
	
	public static void getUserInfo() {
		HashMap<Long, String> permissions = new HashMap<Long, String>();
		Backend backend = Config.getBackend();
		if (backend instanceof FileBackend) {
			FileBackend fb = (FileBackend) backend;
			File folder = new File(fb.getFolder(), "users");
			if (!folder.exists()) {
				permissionsCache = permissions;
				return;
			}
			for (File f : folder.listFiles()) {
				try {
					if (f.isFile()) {
						YamlConfig c = new YamlConfig(f);
						c.load();
						String permission = c.getString("permission_group");
						String name = f.getName().substring(0, f.getName().length() - 4);
						Long id = Long.valueOf(name);
						permissions.put(id, permission);
					}
				} catch (SecurityException e) {
					log.error("No permission to access: " + folder.getAbsolutePath());
				} catch (IOException | InvalidConfigurationException e) {
					log.error("Could not load: " + f.getPath(), e);
				}
			}
		} else if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("SELECT id,permission_group FROM `" + sb.getPrefix() + "users`;")) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Long id = rs.getLong(1);
							String permission = rs.getString(2);
							permissions.put(id, permission);
						}
					} catch (SQLException e) {
						log.error("Error when getting keys: permission_group from table: " + sb.getPrefix() + "users in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error when preparing statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
		permissionsCache = permissions;
	}
	
	public static void getGuildInfo() {
		HashMap<Long, String> languages = new HashMap<Long, String>();
		HashMap<Long, String> prefixes = new HashMap<Long, String>();
		Backend backend = Config.getBackend();
		if (backend instanceof FileBackend) {
			FileBackend fb = (FileBackend) backend;
			File folder = new File(fb.getFolder(), "guilds");
			if (!folder.exists()) {
				languageCache = languages;
				prefixCache = prefixes;
				return;
			}
			for (File f : folder.listFiles()) {
				try {
					if (f.isFile()) {
						YamlConfig c = new YamlConfig(f);
						c.load();
						String prefix = c.getString("prefix");
						String language = c.getString("language");
						String name = f.getName().substring(0, f.getName().length() - 4);
						Long id = Long.valueOf(name);
						languages.put(id, language);
						prefixes.put(id, prefix);
					}
				} catch (SecurityException e) {
					log.error("No permission to access: " + folder.getAbsolutePath());
				} catch (IOException | InvalidConfigurationException e) {
					log.error("Could not load: " + f.getPath(), e);
				}
			}
		} else if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("SELECT id,language,prefix FROM `" + sb.getPrefix() + "guilds`;")) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Long id = rs.getLong(1);
							String language = rs.getString(2);
							String prefix = rs.getString(3);
							languages.put(id, language);
							prefixes.put(id, prefix);
						}
					} catch (SQLException e) {
						log.error("Error when getting keys: language,prefix from table: " + sb.getPrefix() + "guilds in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error when preparing statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
		languageCache = languages;
		prefixCache = prefixes;
	}

}
