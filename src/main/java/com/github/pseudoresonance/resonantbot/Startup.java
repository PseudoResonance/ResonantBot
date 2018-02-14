package com.github.pseudoresonance.resonantbot;

import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;

public class Startup {
	
	private static Logger log = ResonantBot.getLogger();

	protected static String init() {
		String dir = System.getProperty("user.dir");
		File f = new File(dir);
		if (f.isDirectory()) {
			if (f.canWrite()) {
				return dir;
			} else {
				log.error("Can't write to directory " + dir);
			}
		} else {
			log.error(dir + " is an invalid directory!");
		}
		return "";
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
				arg = s.substring(1, s.length());
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
