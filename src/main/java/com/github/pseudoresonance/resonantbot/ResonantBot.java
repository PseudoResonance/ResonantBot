package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.security.auth.login.LoginException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pseudoresonance.resonantbot.api.DiscordBot;
import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.data.Data;
import com.github.pseudoresonance.resonantbot.language.LanguageManager;
import com.github.pseudoresonance.resonantbot.listeners.ConnectionListener;
import com.github.pseudoresonance.resonantbot.listeners.GuildListener;
import com.github.pseudoresonance.resonantbot.listeners.MessageListener;
import com.github.pseudoresonance.resonantbot.listeners.ReadyListener;
import com.github.pseudoresonance.resonantbot.permissions.PermissionGroup;
import com.github.pseudoresonance.resonantbot.permissions.PermissionManager;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class ResonantBot implements DiscordBot {
	
	public static String name = "ResonantBot";

	private Logger log;

	private CommandLine arguments;
	private File directory;
	private File langDirectory;
	private Properties versionInfo;
	
	private static DiscordBot bot;
	private ShardManager jda;

	protected boolean isBotJarFile = false;
	protected boolean botJarFileChecked = false;
	
	public static void main(String[] args) {
		bot = new ResonantBot();
		((ResonantBot) bot).startup(args);
	}
	
	public static DiscordBot getBot() {
		return bot;
	}
	
	private void startup(String[] args) {
		parseArgs(args);
		init();
		launch();
		registerHooks();
	}

	private void init() {
		log = LoggerFactory.getLogger(name);
		initVersionInfo();
		initDirectory();
		log.info("Using directory: " + directory);
		log.info("Starting up ResonantBot version: " + getVersionInfo().getProperty("version"));
		log.debug("Initializing config");
		copyFileFromJar("config.yml");
		Config.loadConfig();
		log.debug("Setting up permissions");
		PermissionManager.init();
		log.debug("Loading data");
		Data.init();
		Data.setUserPermissions(Config.getOwner(), PermissionGroup.BOT_OWNER);
		log.debug("Using default language: " + Config.getLang());
		initLanguageDirectory();
		log.debug("Completed initialization");
	}
	
	private void launch() {
		log.debug("Launching bot");
		String token = Config.getToken();
		if (arguments.hasOption("token"))
			token = arguments.getOptionValue("token");
		if (token == null || token.equals("")) {
			log.error("Please set a bot token in the config!");
			System.exit(1);
		}
		try {
			jda = DefaultShardManagerBuilder.createDefault(Config.getToken()).setMaxReconnectDelay(32).setActivity(Config.getActivity()).build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		log.debug("Registering events");
		jda.addEventListener(new MessageListener(), new ReadyListener(), new ConnectionListener(), new GuildListener());
		log.debug("Initializing command manager");
		CommandManager.init();
		log.debug("Initializing plugin loader");
		new File(directory, "plugins").mkdir();
		PluginClassLoader.init();
		log.info("Loading plugins");
		PluginManager.reload();
	}

	private void parseArgs(String[] args) {
		Options options = new Options();
		Option directory = new Option("d", "directory", true, "Bot Directory");
		options.addOption(directory);
		Option token = new Option("t", "token", true, "Bot Token");
		options.addOption(token);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			arguments = cmd;
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp(name, options);
			System.exit(1);
		}
	}

	private void initDirectory() {
		String dir = System.getProperty("user.dir");
		if (arguments.hasOption("directory"))
			dir = arguments.getOptionValue("directory");
		File f = new File(dir);
		try {
			f.mkdirs();
			System.setProperty("user.dir", f.getAbsolutePath());
			directory = f;
			return;
		} catch (SecurityException e) {
			log.error("Can't write to directory: " + dir);
		}
		log.error("Invalid directory: " + dir);
		System.exit(1);
		return;
	}
	
	private void initLanguageDirectory() {
		langDirectory = new File(directory, "localization");
		langDirectory.mkdir();
		LanguageManager.copyDefaultLanguageFiles(false);
		boolean found = false;
		for (File f : langDirectory.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".lang")) {
				found = true;
			}
		}
		if (!found) {
			log.error("Could not find default language files! Please download a fresh copy of this bot! Shutting down!");
			System.exit(1);
		}
	}
	
	private void registerHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Shutting down!");
				ArrayList<String> names = new ArrayList<String>();
				for (Plugin p : PluginManager.getPlugins()) {
					names.add(p.getName());
				}
				for (String n : names) {
					PluginManager.unload(n);
				}
				Data.shutdown();
				log.info("Plugins unloaded!");
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Logging out!");
				if (jda != null) {
					jda.shutdown();
				}
			}
		});
	}
	
	public File copyFileFromJar(String path) {
		return copyFileFromJar(path, false);
	}
	
	public File copyFileFromJar(String path, File dest) {
		return copyFileFromJar(path, dest, false);
	}
	
	public File copyFileFromJar(String path, boolean override) {
		return copyFileFromJar(path, new File(directory, path), override);
	}
	
	public File copyFileFromJar(String path, File dest, boolean override) {
		if ((botJarFileChecked && isBotJarFile) || ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("jar")) {
			try {
				File jf = new File(ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				try (JarFile jar = new JarFile(jf)) {
					try (InputStream is = jar.getInputStream(jar.getEntry(path))) {
						if (is != null) {
							String name = dest.getName();
							if (!dest.exists() || override) {
								Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
								log.debug("Copying " + name + " to " + dest.getAbsolutePath());
								return dest;
							} else
								return dest;
						}
					} catch (IOException | NullPointerException e) {}
				} catch (IOException e) {}
			} catch (URISyntaxException e1) {}
		}
		if (!isBotJarFile) {
			botJarFileChecked = true;
			String str = ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toString();
			File dirTemp = new File(str.substring(6, str.length())).getParentFile().getParentFile();
			File dir = new File(dirTemp, "/src/main/resources/");
			if (dir.exists()) {
				File f = new File(dir, path);
				if (!dest.exists() || override) {
					try {
						Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						log.debug("Copying " + f.getName() + " to " + dest.getAbsolutePath());
						return dest;
					} catch (IOException e) {}
				} else
					return dest;
			}
		}
		return null;
	}
	
	public YamlConfiguration getLanguage(String name) {
		return getLanguage(name, false);
	}
	
	public YamlConfiguration getLanguage(String name, boolean overwrite) {
		return YamlConfiguration.loadConfiguration(copyFileFromJar("localization/" + name + ".lang", overwrite));
	}

	@Override
	public void reconnect() {
		jda.restart();
	}

	@Override
	public File getDirectory() {
		return directory;
	}

	@Override
	public File getLanguageDirectory() {
		return langDirectory;
	}

	@Override
	public ShardManager getJDA() {
		return jda;
	}

	@Override
	public Logger getLogger() {
		return log;
	}
	
	@Override
	public Properties getVersionInfo() {
		return versionInfo;
	}
	
	private void initVersionInfo() {
		InputStream resourceAsStream = ResonantBot.class.getResourceAsStream("/version.properties");
		versionInfo = new Properties();
		try {
			versionInfo.load( resourceAsStream );
		} catch (Exception e) {
			log.error("Unable to get version information!");
			e.printStackTrace();
		}
	}

}
