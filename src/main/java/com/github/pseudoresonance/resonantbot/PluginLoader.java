package com.github.pseudoresonance.resonantbot;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class PluginLoader extends URLClassLoader {
	
	private static Logger log = ResonantBot.getLogger();
	
	Command command;

	public PluginLoader(File file, ClassLoader loader) throws MalformedURLException {
		super(new URL[] {file.toURI().toURL()}, loader);
        Validate.notNull(loader, "Loader cannot be null");
		try {
			Class<?> jarClass;
			String main = "";
			String command = "";
			try {
				InputStream in = getInputStream(file, "command.json");
				JsonReader jr = Json.createReader(in);
				JsonObject jo = jr.readObject();
				jr.close();
				in.close();
				main = jo.getString("Main");
				command = jo.getString("Command");
				jarClass = Class.forName(main, true, this);
				Class<? extends Command> commandClass;
				commandClass = jarClass.asSubclass(Command.class);
				this.command = commandClass.newInstance();
			} catch (IOException ex) {
				log.error("Cannot find main class `" + main + "` for Command: " + command);
			} catch (ClassCastException ex) {
				log.error("main class `" + main + "` does not extend Command for Command: " + command);
			}
		} catch (IllegalAccessException ex) {
			log.error("No public constructor");
		} catch (InstantiationException ex) {
			log.error("Abnormal plugin type");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static InputStream getInputStream(File zip, String entry) throws IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zip));
		for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
			if (e.getName().equals(entry)) {
				return zin;
			}
		}
		throw new EOFException("Cannot find " + entry);
	}

}
