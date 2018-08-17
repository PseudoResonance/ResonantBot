package com.github.pseudoresonance.resonantbot;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.simpleyaml.configuration.file.YamlConfiguration;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Language {
	
	private static HashMap<String, YamlConfiguration> yamls = new HashMap<String, YamlConfiguration>();
	private static HashMap<Long, String> guildLangs = new HashMap<Long, String>();
	
	private final static Pattern dateFormatPattern = Pattern.compile("\\{\\$date\\$\\}");
	private final static Pattern dateTimeFormatPattern = Pattern.compile("\\{\\$dateTime\\$\\}");
	private final static Pattern timeFormatPattern = Pattern.compile("\\{\\$time\\$\\}");
	
	private final static Pattern escapePattern = Pattern.compile("([*_~`\\\\])");
	private final static Pattern prefixPattern = Pattern.compile("[^ -\"$-.0-?A-\\[\\]\\^a-~]");
	
	public static void updateAllLang() {
		yamls.clear();
		for (String name : guildLangs.values()) {
			if (!yamls.containsKey(name)) {
				updateGuildLang(name);
			}
		}
		if (!yamls.containsKey(Config.getLang())) {
			updateGuildLang(Config.getLang());
		}
		if (!yamls.containsKey("en-US")) {
			updateGuildLang("en-US");
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
		yamls.put(Config.getLang(), yaml);
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
		return getMessage(Config.getLang(), key, args);
	}
	
	public static String getMessage(MessageReceivedEvent e, String key, Object... args) {
		Long id = 0L;
		if (e.getChannelType() == ChannelType.PRIVATE) {
			id = e.getPrivateChannel().getIdLong();
		} else {
			id = e.getGuild().getIdLong();
		}
		String lang = guildLangs.get(id);
		if (lang == null)
			lang = Config.getLang();
		return getMessage(lang, key, args);
	}
	
	public static String getMessage(Long id, String key, Object... args) {
		String lang = guildLangs.get(id);
		if (lang == null)
			lang = Config.getLang();
		return getMessage(lang, key, args);
	}
	
	private static String getMessage(String lang, String key, Object... args) {
		if (lang != null) {
			YamlConfiguration getYaml = yamls.get(lang);
			if (getYaml != null) {
				String ret = getYaml.getString(key, "Localization for `" + key + "` is missing!");
				if (ret.equals("Localization for `" + key + "` is missing!")) {
					if (!lang.equals("en-US")) {
						yamls.get("en-US").getString(key, "Localization for `" + key + "` is missing!");
					}
				}
				for (int i = 0; i < args.length; i++) {
					ret = Pattern.compile("\\{\\$" + (i + 1) + "\\$\\}").matcher(ret).replaceFirst(args[i].toString());
				}
				ret = dateFormatPattern.matcher(ret).replaceFirst(getYaml.getString("dateFormatExplanation"));
				ret = dateTimeFormatPattern.matcher(ret).replaceFirst(getYaml.getString("dateTimeFormatExplanation"));
				ret = timeFormatPattern.matcher(ret).replaceFirst(getYaml.getString("timeFormatExplanation"));
				return ret;
			}
		}
		return "Localization is missing!";
	}
	
	public static YamlConfiguration getLang() {
		return yamls.get(Config.getLang());
	}
	
	public static String getDateFormat() {
		return getDateFormat(Config.getLang());
	}
	
	public static String getDateFormat(Long guildID) {
		String lang = guildLangs.get(guildID);
		return getDateFormat(lang);
	}
	
	public static String getDateFormat(String lang) {
		if (lang != null) {
			YamlConfiguration yaml = yamls.get(lang);
			if (yaml != null) {
				return yaml.getString("dateFormat", "");
			}
		}
		return "";
	}
	
	public static String getDateTimeFormat() {
		return getDateTimeFormat(Config.getLang());
	}
	
	public static String getDateTimeFormat(Long guildID) {
		String lang = guildLangs.get(guildID);
		return getDateTimeFormat(lang);
	}
	
	public static String getDateTimeFormat(String lang) {
		if (lang != null) {
			YamlConfiguration yaml = yamls.get(lang);
			if (yaml != null) {
				return yaml.getString("dateTimeFormat", "");
			}
		}
		return "";
	}
	
	public static String getTimeFormat() {
		return getTimeFormat(Config.getLang());
	}
	
	public static String getTimeFormat(Long guildID) {
		String lang = guildLangs.get(guildID);
		return getTimeFormat(lang);
	}
	
	public static String getTimeFormat(String lang) {
		if (lang != null) {
			YamlConfiguration yaml = yamls.get(lang);
			if (yaml != null) {
				return yaml.getString("timeFormat", "");
			}
		}
		return "";
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
	
	public static String escape(Object toEscape) {
		String esc = toEscape.toString();
		Matcher m = escapePattern.matcher(esc);
		String ret = m.replaceAll("\\\\$1");
		return ret;
	}
	
	public static boolean isValidPrefix(String prefix) {
		return !prefixPattern.matcher(prefix).find();
	}

}
