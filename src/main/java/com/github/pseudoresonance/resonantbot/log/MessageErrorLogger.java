package com.github.pseudoresonance.resonantbot.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.pseudoresonance.resonantbot.ResonantBot;

public class MessageErrorLogger {

	private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	
	private final File folder;
	private boolean initialized = false;
	
	public MessageErrorLogger(File folder) {
		this.folder = folder;
	}
	
	public void logError(String header, Throwable e) {
		if (!initialized) {
			if (!folder.exists())
				folder.mkdirs();
			initialized = true;
		}
		File f = new File(folder, LocalDateTime.now().format(df) + ".error");
		try {
			f.createNewFile();
			try (PrintStream ps = new PrintStream(f)) {
				ps.println(header + "\n");
				e.printStackTrace(ps);
			}
		} catch (IOException e1) {
			ResonantBot.getLogger().error("Could not create error log!\n", e1);
		}
	}

}
