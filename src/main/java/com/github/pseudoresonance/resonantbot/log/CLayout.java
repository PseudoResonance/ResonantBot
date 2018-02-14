package com.github.pseudoresonance.resonantbot.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.github.pseudoresonance.resonantbot.Color;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class CLayout extends LayoutBase<ILoggingEvent> {

	private static final DateFormat df = new SimpleDateFormat("HH:mm:ss");
	
	private List<String> stackOptionList = Arrays.asList("full");

	public String doLayout(ILoggingEvent e) {
		StringBuffer buf = new StringBuffer(128);
		buf.append(Color.RESET + "" + Color.BRIGHT_WHITE).append(df.format(new Date(e.getTimeStamp()))).append(": ");
		Level l = e.getLevel();
		if (l.equals(Level.ALL)) {
			buf.append(Color.BRIGHT_WHITE + "ALL" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_WHITE + e.getFormattedMessage());
		} else if (l.equals(Level.DEBUG)) {
			buf.append(Color.BRIGHT_YELLOW + "DEBUG" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_WHITE + e.getFormattedMessage());
		} else if (l.equals(Level.ERROR)) {
			buf.append(Color.BRIGHT_RED + "ERROR" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_RED + e.getFormattedMessage());
		} else if (l.equals(Level.INFO)) {
			buf.append(Color.BRIGHT_WHITE + "INFO" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_WHITE + e.getFormattedMessage());
		} else if (l.equals(Level.OFF)) {
			buf.append(Color.BRIGHT_WHITE + "OFF" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_WHITE + e.getFormattedMessage());
		} else if (l.equals(Level.TRACE)) {
			buf.append(Color.BRIGHT_YELLOW + "TRACE" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_WHITE + e.getFormattedMessage());
		} else if (l.equals(Level.WARN)) {
			buf.append(Color.BRIGHT_YELLOW + "WARN" + Color.RESET + Color.BRIGHT_WHITE + ": " + Color.BRIGHT_YELLOW + e.getFormattedMessage());
		}
		IThrowableProxy proxy = e.getThrowableProxy();
		if (proxy != null) {
			ThrowableProxyConverter converter = new ThrowableProxyConverter();
			converter.setOptionList(stackOptionList);
			converter.start();
			buf.append(converter.convert(e));
			buf.append(CoreConstants.LINE_SEPARATOR);
		}
		buf.append("\n" + Color.RESET);
		return buf.toString();
	}

	public String getFileHeader() {
		return Color.BRIGHT_MAGENTA + "ResonantBot\n";
	}

}