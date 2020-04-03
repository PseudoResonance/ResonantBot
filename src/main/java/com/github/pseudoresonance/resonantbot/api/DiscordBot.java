package com.github.pseudoresonance.resonantbot.api;

import java.io.File;
import java.util.Properties;

import org.simpleyaml.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import net.dv8tion.jda.api.sharding.ShardManager;

public interface DiscordBot {
	
	public void reconnect();

	public File getDirectory();
	
	public File getLanguageDirectory();
	
	public ShardManager getJDA();
	
	public Logger getLogger();
	
	public YamlConfiguration getLanguage(String name);
	
	public YamlConfiguration getLanguage(String name, boolean overwrite);
	
	public File copyFileFromJar(String path);
	
	public File copyFileFromJar(String path, File dest);
	
	public File copyFileFromJar(String path, boolean override);
	
	public File copyFileFromJar(String path, File dest, boolean override);
	
	public Properties getVersionInfo();
	
}
