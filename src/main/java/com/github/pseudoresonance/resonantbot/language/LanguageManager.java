package com.github.pseudoresonance.resonantbot.language;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.LocaleUtils;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;

import com.github.pseudoresonance.resonantbot.Config;
import com.github.pseudoresonance.resonantbot.PluginManager;
import com.github.pseudoresonance.resonantbot.ResonantBot;
import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.data.Data;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LanguageManager {
	
	private static final ConcurrentHashMap<String, Language> languages = new ConcurrentHashMap<String, Language>();
	
	private static String defaultLanguage = Config.getLang();

	private final static Pattern escapePattern = Pattern.compile("([*_~\\|`\\$\\\\])");
	private final static Pattern codeEscapePattern = Pattern.compile("(`)");
	private final static Pattern prefixPattern = Pattern.compile("[^ -\"$-.0-?A-\\[\\]\\^a-~]");
	
	private static boolean botJarFileChecked = false;
	private static boolean isBotJarFile = false;
	
	/**
	 * Returns the default language
	 * 
	 * @return Default language
	 */
	public static Language getLanguage() {
		return getLanguage(defaultLanguage);
	}
	
	/**
	 * Returns a list of all supported languages
	 * 
	 * @return Set of supported languages
	 */
	public static Set<String> getLanguageList() {
		return languages.keySet();
	}
	
	/**
	 * Returns the set language of the guild or PM
	 * 
	 * @param e Message received event
	 * @return Set language
	 */
	public static Language getLanguage(MessageReceivedEvent e) {
		return getLanguage(Data.getGuildLanguage((e.getChannelType() == ChannelType.PRIVATE ? e.getPrivateChannel() : e.getGuild()).getIdLong()));
	}
	
	/**
	 * Returns the set language of the guild or PM
	 * 
	 * @param id Guild or PM channel ID
	 * @return Set language
	 */
	public static Language getLanguage(long id) {
		return getLanguage(Data.getGuildLanguage(id));
	}
	
	/**
	 * Returns the given language
	 * 
	 * @param lang Name of language to get
	 * @return Language
	 */
	public static Language getLanguage(String lang) {
		Language language = languages.get(lang.toLowerCase());
		if (language == null)
			language = languages.get(defaultLanguage);
		return language;
	}
	
	/**
	 * Register language with given name
	 * 
	 * @param lang Language name
	 * @param language {@link Language}
	 * @return Whether or not registration was successful
	 */
	public static boolean registerLanguage(String lang, Language language) {
		if (!languages.containsKey(lang.toLowerCase())) {
			languages.put(lang.toLowerCase(), language);
			return true;
		}
		return false;
	}
	
	/**
	 * Sets default language
	 * 
	 * @param lang Default language to set
	 */
	public static void setDefaultLanguage(String lang) {
		defaultLanguage = lang;
		if (getLanguage(lang) == null)
			registerLanguage(lang, new Language(lang));
	}
	
	/**
	 * Reloads the given language files
	 * 
	 * @param lang Language to reload
	 * @return Whether or not language was reloaded
	 */
	public static boolean reloadLanguage(String lang) {
		Locale locale = new Locale.Builder().setLanguageTag(lang).build();
		if (!LocaleUtils.isAvailableLocale(locale))
			return false;
		lang = locale.toLanguageTag();
		Language language = languages.get(lang.toLowerCase());
		if (language == null)
			language = new Language(lang);
		else
			language.resetLanguageMap();
		boolean foundLanguage = false;
		if (ResonantBot.getBot().getLanguageDirectory().exists()) {
			for (File f : ResonantBot.getBot().getLanguageDirectory().listFiles()) {
				if (f.getName().length() > 5 && f.getName().substring(0, f.getName().length() - 5).equalsIgnoreCase(lang)) {
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
					HashMap<String, String> langMap = new HashMap<String, String>();
					for (String namespace : yaml.getKeys(false)) {
						ConfigurationSection cs = yaml.getConfigurationSection(namespace);
						if (cs != null)
							for (String key : cs.getKeys(false))
								langMap.put(namespace + "." + key, cs.getString(key));
					}
					foundLanguage = true;
					language.addLanguageMap(langMap);
					break;
				}
			}
		}
		for (Plugin p : PluginManager.getPlugins()) {
			File langDir = new File(p.getFolder(), "localization");
			if (langDir.exists()) {
				for (File f : langDir.listFiles()) {
					if (f.getName().length() > 5 && f.getName().substring(0, f.getName().length() - 5).equalsIgnoreCase(lang)) {
						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
						HashMap<String, String> langMap = new HashMap<String, String>();
						for (String namespace : yaml.getKeys(false)) {
							if (namespace.equals("date")) {
								continue;
							}
							ConfigurationSection cs = yaml.getConfigurationSection(namespace);
							if (cs != null)
								for (String key : cs.getKeys(false))
									langMap.put(namespace + "." + key, cs.getString(key));
						}
						foundLanguage = true;
						language.addLanguageMap(langMap);
						break;
					}
				}
			}
		}
		if (foundLanguage)
			registerLanguage(lang, language);
		return foundLanguage;
	}
	
	/**
	 * Returns proper language name
	 * 
	 * @param lang Language name to test
	 * @return Proper language name or <code>null</code> if invalid
	 */
	public static String getValidLanguageName(String lang) {
		Locale locale = new Locale.Builder().setLanguageTag(lang).build();
		if (!LocaleUtils.isAvailableLocale(locale))
			return null;
		return locale.toLanguageTag();
	}
	
	/**
	 * Resets the given language files
	 * 
	 * @param lang Language to reset
	 * @return Whether or not language was reset
	 */
	public static boolean resetLanguage(String lang) {
		Locale locale = new Locale.Builder().setLanguageTag(lang).build();
		if (!LocaleUtils.isAvailableLocale(locale))
			return false;
		lang = locale.toLanguageTag();
		Language language = languages.get(lang.toLowerCase());
		if (language == null)
			language = new Language(lang);
		else
			language.resetLanguageMap();
		boolean foundLanguage = false;
		ResonantBot.getBot().getLogger().info("Copying default language files: " + lang);
		if ((botJarFileChecked && isBotJarFile) || ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("jar")) {
			try {
				File jf = new File(ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				try (JarFile jar = new JarFile(jf)) {
					Enumeration<? extends ZipEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						isBotJarFile = true;
						botJarFileChecked = true;
						ZipEntry entry = entries.nextElement();
						if (entry.getName().startsWith("localization/") && entry.getName().toLowerCase().endsWith(lang.toLowerCase() + ".lang")) {
							try (InputStream is = jar.getInputStream(entry)) {
								if (is != null) {
									String name = entry.getName().substring(13, entry.getName().length());
									File dest = new File(ResonantBot.getBot().getLanguageDirectory(), name);
									Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
									ResonantBot.getBot().getLogger().debug("Copying " + name + " to " + dest.getAbsolutePath());
									foundLanguage = true;
								}
							} catch (IOException | NullPointerException e) {
							}
							break;
						}
					}
				} catch (IOException e) {
				}
			} catch (URISyntaxException e1) {
			}
		}
		if (!isBotJarFile) {
			botJarFileChecked = true;
			String str = ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toString();
			File dirTemp = new File(str.substring(6, str.length())).getParentFile().getParentFile();
			File dir = new File(dirTemp, "/src/main/resources/localization/");
			if (dir.exists()) {
				for (File f : dir.listFiles()) {
					if (f.isFile() && f.getName().toLowerCase().endsWith(lang.toLowerCase() + ".lang")) {
						File dest = new File(ResonantBot.getBot().getLanguageDirectory(), f.getName());
						try {
							Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
							ResonantBot.getBot().getLogger().debug("Copying " + f.getName() + " to " + dest.getAbsolutePath());
							foundLanguage = true;
						} catch (IOException e) {
						}
						break;
					}
				}
			}
		}
		for (Plugin p : PluginManager.getPlugins()) {
			URL url = p.getClass().getProtectionDomain().getCodeSource().getLocation();
			File langDir = new File(p.getFolder(), "localization");
			if (url != null) {
				try (ZipFile jar = new ZipFile(new File(url.toURI()))) {
					Enumeration<? extends ZipEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (entry.getName().startsWith("localization/") && entry.getName().toLowerCase().endsWith(lang.toLowerCase() + ".lang")) {
							try (InputStream is = jar.getInputStream(entry)) {
								if (is != null) {
									langDir.mkdirs();
									String name = entry.getName().substring(13);
									File dest = new File(langDir, name);
									Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
									foundLanguage = true;
								}
							} catch (IOException e) {
							}
							break;
						}
					}
				} catch (IOException | URISyntaxException e) {
					ResonantBot.getBot().getLogger().error("Could not get plugin jar to fetch locale files!");
					e.printStackTrace();
				}
			}
		}
		ResonantBot.getBot().getLogger().debug("Language files copied: " + lang);
		if (ResonantBot.getBot().getLanguageDirectory().exists()) {
			for (File f : ResonantBot.getBot().getLanguageDirectory().listFiles()) {
				if (f.getName().length() > 5 && f.getName().substring(0, f.getName().length() - 5).equalsIgnoreCase(lang)) {
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
					HashMap<String, String> langMap = new HashMap<String, String>();
					for (String namespace : yaml.getKeys(false)) {
						ConfigurationSection cs = yaml.getConfigurationSection(namespace);
						if (cs != null)
							for (String key : cs.getKeys(false))
								langMap.put(namespace + "." + key, cs.getString(key));
					}
					foundLanguage = true;
					language.addLanguageMap(langMap);
					break;
				}
			}
		}
		for (Plugin p : PluginManager.getPlugins()) {
			File langDir = new File(p.getFolder(), "localization");
			if (langDir.exists()) {
				for (File f : langDir.listFiles()) {
					if (f.getName().length() > 5 && f.getName().substring(0, f.getName().length() - 5).equalsIgnoreCase(lang)) {
						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
						HashMap<String, String> langMap = new HashMap<String, String>();
						for (String namespace : yaml.getKeys(false)) {
							if (namespace.equals("date")) {
								continue;
							}
							ConfigurationSection cs = yaml.getConfigurationSection(namespace);
							if (cs != null)
								for (String key : cs.getKeys(false))
									langMap.put(namespace + "." + key, cs.getString(key));
						}
						foundLanguage = true;
						language.addLanguageMap(langMap);
						break;
					}
				}
			}
		}
		if (foundLanguage)
			registerLanguage(lang, language);
		return foundLanguage;
	}
	
	/**
	 * Copies default bot language files to bot language folder
	 * 
	 * @param overwrite Whether or not to overwrite old language files
	 */
	public static void copyDefaultLanguageFiles(boolean overwrite) {
		ResonantBot.getBot().getLogger().info("Copying default language files");
		if ((botJarFileChecked && isBotJarFile) || ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("jar")) {
			try {
				File jf = new File(ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				try (JarFile jar = new JarFile(jf)) {
					Enumeration<? extends ZipEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						isBotJarFile = true;
						botJarFileChecked = true;
						ZipEntry entry = entries.nextElement();
						if (entry.getName().startsWith("localization/") && entry.getName().endsWith(".lang")) {
							try (InputStream is = jar.getInputStream(entry)) {
								if (is != null) {
									String name = entry.getName().substring(13, entry.getName().length());
									File dest = new File(ResonantBot.getBot().getLanguageDirectory(), name);
									if (!dest.exists() || overwrite) {
										Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
										ResonantBot.getBot().getLogger().debug("Copying " + name + " to " + dest.getAbsolutePath());
									}
								}
							} catch (IOException | NullPointerException e) {
							}
						}
					}
				} catch (IOException e) {
				}
			} catch (URISyntaxException e1) {
			}
		}
		if (!isBotJarFile) {
			botJarFileChecked = true;
			String str = ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toString();
			File dirTemp = new File(str.substring(6, str.length())).getParentFile().getParentFile();
			File dir = new File(dirTemp, "/src/main/resources/localization/");
			if (dir.exists()) {
				for (File f : dir.listFiles()) {
					if (f.isFile() && f.getName().endsWith(".lang")) {
						File dest = new File(ResonantBot.getBot().getLanguageDirectory(), f.getName());
						try {
							if (!dest.exists() || overwrite) {
								Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
								ResonantBot.getBot().getLogger().debug("Copying " + f.getName() + " to " + dest.getAbsolutePath());
							}
						} catch (IOException e) {
						}
					}
				}
			}
		}
		ResonantBot.getBot().getLogger().debug("Language files copied");
		if (ResonantBot.getBot().getLanguageDirectory().exists()) {
			for (File f : ResonantBot.getBot().getLanguageDirectory().listFiles()) {
				if (f.getName().length() > 5) {
					String lang = f.getName().substring(0, f.getName().length() - 5);
					Locale locale = new Locale.Builder().setLanguageTag(lang).build();
					if (!LocaleUtils.isAvailableLocale(locale)) {
						ResonantBot.getBot().getLogger().error("Invalid locale file: " + f.getName() + " Unknown locale!");
						continue;
					}
					lang = locale.toLanguageTag();
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
					Language language = languages.get(lang.toLowerCase());
					if (language == null) {
						language = new Language(lang);
						registerLanguage(lang, language);
					}
					HashMap<String, String> langMap = new HashMap<String, String>();
					for (String namespace : yaml.getKeys(false)) {
						ConfigurationSection cs = yaml.getConfigurationSection(namespace);
						if (cs != null)
							for (String key : cs.getKeys(false))
								langMap.put(namespace + "." + key, cs.getString(key));
					}
					language.addLanguageMap(langMap);
				} else
					ResonantBot.getBot().getLogger().error("Invalid locale file: " + f.getName() + " Unknown locale!");
			}
		}
	}
	
	/**
	 * Copies default plugin language files to plugin folder
	 * 
	 * @param plugin Plugin whose language files to copy
	 * @param overwrite Whether or not to overwrite old language files
	 */
	public static void copyDefaultPluginLanguageFiles(Plugin plugin, boolean overwrite) {
		URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
		File langDir = new File(plugin.getFolder(), "localization");
		if (url != null) {
			try (ZipFile jar = new ZipFile(new File(url.toURI()))) {
				Enumeration<? extends ZipEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.getName().startsWith("localization/") && entry.getName().endsWith(".lang")) {
						try (InputStream is = jar.getInputStream(entry)) {
							if (is != null) {
								langDir.mkdirs();
								String name = entry.getName().substring(13);
								File dest = new File(langDir, name);
								if (!dest.exists() || overwrite) {
									Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
								}
							}
						} catch (IOException e) {
							ResonantBot.getBot().getLogger().error("Error while fetching loacle file: " + entry.getName().substring(13));
							e.printStackTrace();
						}
					}
				}
			} catch (IOException | URISyntaxException e) {
				ResonantBot.getBot().getLogger().error("Could not get plugin jar to fetch locale files!");
				e.printStackTrace();
			}
		}
		if (langDir.exists()) {
			for (File f : langDir.listFiles()) {
				if (f.getName().length() > 5) {
					String lang = f.getName().substring(0, f.getName().length() - 5);
					Locale locale = new Locale.Builder().setLanguageTag(lang).build();
					if (!LocaleUtils.isAvailableLocale(locale)) {
						ResonantBot.getBot().getLogger().error("Invalid locale file: " + f.getName() + " Unknown locale!");
						continue;
					}
					lang = locale.toLanguageTag();
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
					Language language = languages.get(lang.toLowerCase());
					if (language == null) {
						language = new Language(lang);
						registerLanguage(lang, language);
					}
					HashMap<String, String> langMap = new HashMap<String, String>();
					for (String namespace : yaml.getKeys(false)) {
						if (namespace.equals("date")) {
							continue;
						}
						ConfigurationSection cs = yaml.getConfigurationSection(namespace);
						if (cs != null)
							for (String key : cs.getKeys(false))
								langMap.put(namespace + "." + key, cs.getString(key));
					}
					language.addLanguageMap(langMap);
				} else
					ResonantBot.getBot().getLogger().error("Invalid locale file: " + f.getName() + " Unknown locale!");
			}
		}
	}
	
	/**
	 * Escape input for output to Discord chat
	 * 
	 * @param toEscape Object to escape
	 * @return Escaped string
	 */
	public static String escape(Object toEscape) {
		String esc = toEscape.toString();
		Matcher m = escapePattern.matcher(esc);
		String ret = m.replaceAll("\\\\$1");
		return ret;
	}
	
	/**
	 * Escape input for output to Discord chat in code blocks
	 * 
	 * @param toEscape Object to escape
	 * @return Escaped string
	 */
	public static String codeEscape(Object toEscape) {
		String esc = toEscape.toString();
		Matcher m = codeEscapePattern.matcher(esc);
		String ret = m.replaceAll("\\\\$1");
		return ret;
	}
	
	/**
	 * Checks whether or not a prefix is valid
	 * 
	 * @param prefix Prefix to test
	 * @return Whether or not prefix is valid
	 */
	public static boolean isValidPrefix(String prefix) {
		return !prefixPattern.matcher(prefix).find();
	}

}
