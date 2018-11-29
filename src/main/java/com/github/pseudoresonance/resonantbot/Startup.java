package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;

public class Startup {

	private static Logger log = null;
	
	protected static boolean jarFile = false;
	protected static boolean checkedJar = false;

	protected static File init(HashMap<String, String> args) {
		String dir = System.getProperty("user.dir");
		if (args.containsKey("dir")) {
			dir = args.get("dir");
		}
		File f = new File(dir);
		try {
			f.mkdirs();
			System.setProperty("user.dir", f.getAbsolutePath());
			return f;
		} catch (SecurityException e) {
			System.err.println("Can't write to directory: " + dir);
		}
		System.err.println("Invalid directory: " + dir);
		return new File("");
	}
	
	protected static void setLogger(Logger logger) {
		log = logger;
	}
	
	public static void defaultLangs(File directory) {
		defaultLangs(directory, false);
	}
	
	public static void defaultLangs(File directory, boolean overwrite) {
		log.info("Copying default language files");
		if ((checkedJar && jarFile) || ResonantBot.class.getResource("ResonantBot.class").toString().startsWith("jar")) {
			try {
				File jf = new File(ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				try (JarFile jar = new JarFile(jf)) {
					Enumeration<? extends ZipEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						jarFile = true;
						checkedJar = true;
						ZipEntry entry = entries.nextElement();
						if (entry.getName().startsWith("localization/") && entry.getName().endsWith(".lang")) {
							try (InputStream is = jar.getInputStream(entry)) {
								if (is != null) {
									String name = entry.getName().substring(13, entry.getName().length());
									File dest = new File(directory, name);
									if (!dest.exists() || overwrite) {
										Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
										log.debug("Copying " + name + " to " + dest.getAbsolutePath());
									}
								}
							} catch (IOException | NullPointerException e) {}
						}
					}
				} catch (IOException e) {}
			} catch (URISyntaxException e1) {}
		}
		if (!jarFile) {
			checkedJar = true;
			String str = ResonantBot.class.getProtectionDomain().getCodeSource().getLocation().toString();
			File dirTemp = new File(str.substring(6, str.length())).getParentFile().getParentFile();
			File dir = new File(dirTemp, "/src/main/resources/localization/");
			if (dir.exists()) {
				for (File f : dir.listFiles()) {
					if (f.isFile() && f.getName().endsWith(".lang")) {
						File dest = new File(directory, f.getName());
						try {
							if (!dest.exists() || overwrite) {
								Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
								log.debug("Copying " + f.getName() + " to " + dest.getAbsolutePath());
							}
						} catch (IOException e) {}
					}
				}
			}
		}
		log.debug("Language files copied");
	}

	public static HashMap<String, String> parseArgs(String[] args) {
		String build = "";
		String arg = "";
		HashMap<String, String> ret = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (!s.startsWith("-")) {
				build += s + " ";
			} else {
				if (build != "") {
					ret.put(arg, build.substring(0, build.length() - 1));
					arg = "";
					build = "";
				}
				if (s.startsWith("--"))
					arg = s.substring(2, s.length());
				else {
					arg = s.substring(1, s.length());
					switch(arg) {
					case "d":
						arg = "dir";
						break;
					case "t":
						arg = "token";
						break;
					}
				}
			}
		}
		if (build != "") {
			ret.put(arg, build.substring(0, build.length() - 1));
			arg = "";
			build = "";
		}
		return ret;
	}

}
