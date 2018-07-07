package com.github.pseudoresonance.resonantbot;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.simpleyaml.configuration.file.YamlConfiguration;

public class Language {
	
	private static YamlConfiguration yaml = null;
	private static HashMap<String, YamlConfiguration> yamls = new HashMap<String, YamlConfiguration>();
	private static HashMap<Long, String> guildLangs = new HashMap<Long, String>();
	
	private final static Pattern pattern = Pattern.compile("\\{\\$\\$\\}");
	private final static Pattern dateFormatPattern = Pattern.compile("\\{\\$date\\$\\}");
	private final static Pattern dateTimeFormatPattern = Pattern.compile("\\{\\$dateTime\\$\\}");
	private final static Pattern timeFormatPattern = Pattern.compile("\\{\\$time\\$\\}");
	
	private static String dateFormat = "";
	private static String dateTimeFormat = "";
	private static String timeFormat = "";
	
	public static void updateAllLang() {
		updateLang(Config.getLang());
		yamls.clear();
		for (String name : guildLangs.values()) {
			if (!yamls.containsKey(name)) {
				updateGuildLang(name);
			}
		}
	}
	
	public static void setGuildLang(Long guildId, String name) {
		guildLangs.put(guildId, name);
		if (!yamls.containsKey(name)) {
			updateGuildLang(name);
		}
	}
	
	public static void unsetGuildLang(Long guildId) {
		guildLangs.remove(guildId);
	}
	
	public static String getGuildLang(Long guildId) {
		String lang = guildLangs.get(guildId);
		if (lang == null)
			lang = Config.getLang();
		return lang;
	}
	
	protected static void setLang(YamlConfiguration yaml) {
		Language.yaml = yaml;
		dateFormat = yaml.getString("dateFormat", "");
		dateTimeFormat = yaml.getString("dateTimeFormat", "");
		timeFormat = yaml.getString("timeFormat", "");
	}
	
	public static void updateLang(String name, boolean overwrite) {
		YamlConfiguration yaml = ResonantBot.getLanguage(name, overwrite);
		yaml = PluginManager.updateLanguage(yaml, overwrite);
		setLang(yaml);
	}
	
	public static void updateLang(String name) {
		updateLang(name, false);
	}
	
	public static void resetLang(String name) {
		YamlConfiguration yaml = ResonantBot.getLanguage(name, true);
		yamls.put(name, PluginManager.updateGuildLanguage(name, yaml, true));
	}
	
	public static void updateGuildLang(String name, boolean overwrite) {
		YamlConfiguration yaml = ResonantBot.getLanguage(name, overwrite);
		yamls.put(name, PluginManager.updateGuildLanguage(name, yaml, overwrite));
	}
	
	public static void updateGuildLang(String name) {
		updateGuildLang(name, false);
	}
	
	public static String getMessage(String key, Object... args) {
		if (yaml != null) {
			String ret = yaml.getString(key, "Localization for `" + key + "` is missing!");
			if (ret.equals("Localization for `" + key + "` is missing!")) {
				if (!Config.getLang().equals("en-US")) {
					yamls.get("en-US").getString(key, "Localization for `" + key + "` is missing!");
				}
			}
			for (int i = 0; i < args.length; i++) {
				ret = pattern.matcher(ret).replaceFirst(args[i].toString());
			}
			ret = dateFormatPattern.matcher(ret).replaceFirst(yaml.getString("dateFormatExplanation"));
			ret = dateTimeFormatPattern.matcher(ret).replaceFirst(yaml.getString("dateTimeFormatExplanation"));
			ret = timeFormatPattern.matcher(ret).replaceFirst(yaml.getString("timeFormatExplanation"));
			return ret;
		} else
			return "Localization is missing!";
	}
	
	public static String getMessage(Long guildID, String key, Object... args) {
		YamlConfiguration yaml = Language.yaml;
		String lang = guildLangs.get(guildID);
		if (lang != null) {
			YamlConfiguration getYaml = yamls.get(lang);
			if (getYaml != null) {
				yaml = getYaml;
				String ret = yaml.getString(key, "Localization for `" + key + "` is missing!");
				if (ret.equals("Localization for `" + key + "` is missing!")) {
					if (!lang.equals("en-US")) {
						yamls.get("en-US").getString(key, "Localization for `" + key + "` is missing!");
					}
				}
				for (int i = 0; i < args.length; i++) {
					ret = pattern.matcher(ret).replaceFirst(args[i].toString());
				}
				ret = dateFormatPattern.matcher(ret).replaceFirst(yaml.getString("dateFormatExplanation"));
				ret = dateTimeFormatPattern.matcher(ret).replaceFirst(yaml.getString("dateTimeFormatExplanation"));
				ret = timeFormatPattern.matcher(ret).replaceFirst(yaml.getString("timeFormatExplanation"));
				return ret;
			}
		}
		if (yaml != null) {
			String ret = yaml.getString(key, "Localization for `" + key + "` is missing!");
			if (ret.equals("Localization for `" + key + "` is missing!")) {
				if (!Config.getLang().equals("en-US")) {
					yamls.get("en-US").getString(key, "Localization for `" + key + "` is missing!");
				}
			}
			for (int i = 0; i < args.length; i++) {
				ret = pattern.matcher(ret).replaceFirst(args[i].toString());
			}
			ret = dateFormatPattern.matcher(ret).replaceFirst(yaml.getString("dateFormatExplanation"));
			ret = dateTimeFormatPattern.matcher(ret).replaceFirst(yaml.getString("dateTimeFormatExplanation"));
			ret = timeFormatPattern.matcher(ret).replaceFirst(yaml.getString("timeFormatExplanation"));
			return ret;
		} else
			return "Localization is missing!";
	}
	
	public static YamlConfiguration getLang() {
		return yaml;
	}
	
	public static String getDateFormat() {
		return dateFormat;
	}
	
	public static String getDateFormat(Long guildID) {
		String lang = guildLangs.get(guildID);
		if (lang != null) {
			YamlConfiguration yaml = yamls.get(lang);
			if (yaml != null) {
				return yaml.getString("dateFormat", dateFormat);
			}
		}
		return dateFormat;
	}
	
	public static String getDateTimeFormat() {
		return dateTimeFormat;
	}
	
	public static String getDateTimeFormat(Long guildID) {
		String lang = guildLangs.get(guildID);
		if (lang != null) {
			YamlConfiguration yaml = yamls.get(lang);
			if (yaml != null) {
				return yaml.getString("dateTimeFormat", dateTimeFormat);
			}
		}
		return dateTimeFormat;
	}
	
	public static String getTimeFormat() {
		return timeFormat;
	}
	
	public static String getTimeFormat(Long guildID) {
		String lang = guildLangs.get(guildID);
		if (lang != null) {
			YamlConfiguration yaml = yamls.get(lang);
			if (yaml != null) {
				return yaml.getString("timeFormat", timeFormat);
			}
		}
		return timeFormat;
	}
	
	public static HashMap<Long, String> getGuildLangs() {
		return guildLangs;
	}
	
	protected static void setGuildLangs(HashMap<Long, String> langs) {
		guildLangs = langs;
		for (String l : guildLangs.values()) {
			if (!yamls.containsKey(l)) {
				updateGuildLang(l);
			}
		}
	}

}
