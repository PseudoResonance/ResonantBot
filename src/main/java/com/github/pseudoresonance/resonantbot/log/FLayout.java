package com.github.pseudoresonance.resonantbot.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class FLayout extends LayoutBase<ILoggingEvent> {

	private static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	@Override
	public String doLayout(ILoggingEvent e) {
		StringBuilder builder = new StringBuilder(1000);
		builder.append("[").append(df.format(new Date(e.getTimeStamp()))).append("] ");
		builder.append("[").append(e.getLevel()).append("] ");
		builder.append(e.getFormattedMessage()).append("\n");
		return builder.toString();
	}

	public String getFileHeader() {
		return "ResonantBot\n";
	}

}