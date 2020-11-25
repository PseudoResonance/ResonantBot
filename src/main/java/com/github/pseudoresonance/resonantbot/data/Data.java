package com.github.pseudoresonance.resonantbot.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.ResonantBot;
import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.permissions.PermissionGroup;

public class Data {

	private static Logger log = ResonantBot.getBot().getLogger();

	private static Backend backend = null;

	private static Map<Plugin, ArrayList<DynamicTable>> tables = new HashMap<Plugin, ArrayList<DynamicTable>>();
	private static DynamicTable bot = null;
	private static DynamicTable users = null;
	private static DynamicTable guilds = null;

	private static HashMap<Long, PermissionGroup> permissionsCache = new HashMap<Long, PermissionGroup>();
	private static HashMap<Long, DualHashBidiMap<PermissionGroup, Long>> roleCache = new HashMap<Long, DualHashBidiMap<PermissionGroup, Long>>();
	private static HashMap<Long, String> prefixCache = new HashMap<Long, String>();
	private static HashMap<Long, String> languageCache = new HashMap<Long, String>();

	public static void init() {
		log.debug("Initializing data storage");
		Backend b = Config.getBackend();
		backend = b;
		b.setup();

		bot = new DynamicTable("bot", "VARCHAR(64)");
		bot.setup();
		addTable(null, bot);
		bot.addColumn(new Column("value", "VARCHAR(128)"));

		users = new DynamicTable("users", "BIGINT UNSIGNED");
		users.setup();
		addTable(null, users);
		users.addColumn(new Column("permission_group", "VARCHAR(64)", "DEFAULT"));

		guilds = new DynamicTable("guilds", "BIGINT UNSIGNED");
		guilds.setup();
		addTable(null, guilds);
		guilds.addColumn(new Column("language", "VARCHAR(8)", "en-US"));
		guilds.addColumn(new Column("prefix", "VARCHAR(32)", "NULL"));
		guilds.addColumn(new Column("group_roles", "TEXT", null));
	}

	public static void updateBackend() {
		Backend b = Config.getBackend();
		if (!b.equals(backend)) {
			if (b instanceof SQLBackend) {
				SQLBackend sb = (SQLBackend) b;
				sb.setup();
			}
		}
		for (ArrayList<DynamicTable> tableList : tables.values()) {
			for (DynamicTable table : tableList) {
				table.update();
			}
		}
	}

	public static void addTable(Plugin plugin, DynamicTable table) {
		ArrayList<DynamicTable> tableList = tables.get(plugin);
		if (tableList == null) {
			tableList = new ArrayList<DynamicTable>();
			tables.put(plugin, tableList);
		}
		tableList.add(table);
	}

	public static void removeTables(Plugin plugin) {
		ArrayList<DynamicTable> tableList = tables.remove(plugin);
		if (tableList != null) {
			for (DynamicTable table : tableList) {
				table.shutdown();
			}
		}
	}

	public static void shutdown() {
		DynamicTable.shutdownAll();
	}

	public static void migrate(Backend from, Backend to) {
		for (ArrayList<DynamicTable> tableList : tables.values()) {
			for (DynamicTable table : tableList) {
				table.migrateBackends(from, to);
			}
		}
	}

	public static void setRolePermission(long guildId, long roleId, PermissionGroup permission) {
		DualHashBidiMap<PermissionGroup, Long> guildMap = roleCache.get(guildId);
		if (guildMap == null) {
			guildMap = new DualHashBidiMap<PermissionGroup, Long>();
			roleCache.put(guildId, guildMap);
		}
		guildMap.put(permission, roleId);
		setGuildSetting(guildId, "group_roles", concatenateGuildMap(guildMap));
	}

	public static PermissionGroup getRolePermission(long guildId, long roleId) {
		DualHashBidiMap<PermissionGroup, Long> guildMap = roleCache.get(guildId);
		if (guildMap == null) {
			guildMap = new DualHashBidiMap<PermissionGroup, Long>();
			roleCache.put(guildId, guildMap);
		}
		return guildMap.getKey(roleId);
	}

	public static DualHashBidiMap<PermissionGroup, Long> getGuildRoles(long guildId) {
		if (roleCache.size() == 0)
			getGuildInfo();
		return roleCache.get(guildId);
	}

	public static void removeRolePermission(long guildId, long roleId) {
		DualHashBidiMap<PermissionGroup, Long> guildMap = roleCache.get(guildId);
		if (guildMap == null) {
			guildMap = new DualHashBidiMap<PermissionGroup, Long>();
			roleCache.put(guildId, guildMap);
		}
		guildMap.removeValue(roleId);
		setGuildSetting(guildId, "group_roles", concatenateGuildMap(guildMap));
	}

	private static String concatenateGuildMap(DualHashBidiMap<PermissionGroup, Long> guildMap) {
		String ret = "";
		for (Entry<PermissionGroup, Long> entry : guildMap.entrySet())
			ret += entry.getKey() + "=" + entry.getValue() + ";";
		ret = ret.substring(0, ret.length() - 1);
		return ret;
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

	public static void setUserPermissions(Long id, PermissionGroup group) {
		permissionsCache.put(id, group);
		setUserSetting(id, "permission_group", group.toString().toUpperCase());
	}

	public static void setGuildPrefix(Long id, String prefix) {
		prefixCache.put(id, prefix);
		setGuildSetting(id, "prefix", prefix);
	}

	public static void setGuildLanguage(Long id, String language) {
		languageCache.put(id, language);
		setGuildSetting(id, "language", language);
	}

	public static PermissionGroup getUserPermissions(Long id) {
		if (permissionsCache.size() == 0)
			getUserInfo();
		PermissionGroup ret = permissionsCache.get(id);
		return (ret == null) ? PermissionGroup.DEFAULT : ret;
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
		HashMap<Long, PermissionGroup> permissions = new HashMap<Long, PermissionGroup>();
		Backend backend = Config.getBackend();
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("SELECT id,permission_group FROM `" + sb.getPrefix() + "users`;")) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Long id = rs.getLong(1);
							PermissionGroup permission = PermissionGroup.valueOf(rs.getString(2));
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
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("SELECT id,language,prefix,group_roles FROM `" + sb.getPrefix() + "guilds`;")) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Long id = rs.getLong(1);
							String language = rs.getString(2);
							String prefix = rs.getString(3);
							String roles = rs.getString(4);
							DualHashBidiMap<PermissionGroup, Long> roleMap = new DualHashBidiMap<PermissionGroup, Long>();
							if (roles != null) {
								String[] splitRoles = roles.split(";");
								for (String s : splitRoles) {
									String[] split = s.split("=");
									if (split.length >= 2)
										roleMap.put(PermissionGroup.valueOf(split[0].toUpperCase()), Long.valueOf(split[1]));
								}
							}
							roleCache.put(id, roleMap);
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
