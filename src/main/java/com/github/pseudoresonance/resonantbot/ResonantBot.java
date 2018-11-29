package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarFile;
import javax.security.auth.login.LoginException;

import org.simpleyaml.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pseudoresonance.resonantbot.api.Plugin;
import com.github.pseudoresonance.resonantbot.data.Data;
import com.github.pseudoresonance.resonantbot.listeners.ConnectionListener;
import com.github.pseudoresonance.resonantbot.listeners.GuildListener;
import com.github.pseudoresonance.resonantbot.listeners.MessageListener;
import com.github.pseudoresonance.resonantbot.listeners.ReadyListener;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

public class ResonantBot {

	private static Logger log;

	private static HashMap<String, String> args;
	private static File directory;
	private static File langDirectory;
	
	private static ShardManager client;
	
	public static void main(String[] args) {
		ResonantBot.args = Startup.parseArgs(args);
		directory = Startup.init(ResonantBot.args);
		log = LoggerFactory.getLogger("ResonantBot");
		log.info("Using directory: " + directory);
		Startup.setLogger(log);
		langDirectory = new File(directory, "localization");
		langDirectory.mkdir();
		Startup.defaultLangs(langDirectory);
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
		initHooks();
		log.debug("Completed initialization");
		log.info("Starting up ResonantBot");
		copyFileFromJar("config.yml");
		Config.init(log);
		log.debug("Updating languages");
		Language.updateAllLang();
		new File(directory, "plugins").mkdir();
		log.debug("Launching bot");
		String token = Config.getToken();
		if (ResonantBot.args.containsKey("token"))
			token = ResonantBot.args.get("token");
		if (token == null || token.equals("")) {
			log.error("Please set a bot token in the config!");
			System.exit(1);
		}
		try {
			client = new DefaultShardManagerBuilder().setMaxReconnectDelay(32).setToken(Config.getToken()).setGame(Config.getGame()).build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		log.debug("Registering events");
		client.addEventListener(new MessageListener(), new ReadyListener(), new ConnectionListener(), new GuildListener());
		log.debug("Loading plugins");
		PluginManager.reload();
	}

	public static File getDir() {
		return directory;
	}

	public static File getLangDir() {
		return langDirectory;
	}

	public static ShardManager getJDA() {
		return client;
	}

	public static Logger getLogger() {
		return log;
	}
	
	private static void initHooks() {
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
				if (client != null) {
					client.shutdown();
				}
			}
		});
	}
	
	public static File copyFileFromJar(String path) {
		return copyFileFromJar(path, false);
	}
	
	public static File copyFileFromJar(String path, File dest) {
		return copyFileFromJar(path, dest, false);
	}
	
	public static File copyFileFromJar(String path, boolean override) {
		return copyFileFromJar(path, new File(directory, path), override);
	}
	
	public static File copyFileFromJar(String path, File dest, boolean override) {
		if ((Startup.checkedJar && Startup.jarFile) || ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("jar")) {
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
		if (!Startup.jarFile) {
			Startup.checkedJar = true;
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
	
	public static YamlConfiguration getLanguage(String name, boolean overwrite) {
		return YamlConfiguration.loadConfiguration(copyFileFromJar("localization/" + name + ".lang", overwrite));
	}
	
	public static YamlConfiguration getLanguage(String name) {
		return getLanguage(name, false);
	}

}
