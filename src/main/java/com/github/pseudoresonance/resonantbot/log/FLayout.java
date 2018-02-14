package com.github.pseudoresonance.resonantbot.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class FLayout extends LayoutBase<ILoggingEvent> {

	private static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private List<String> stackOptionList = Arrays.asList("full");

	public String doLayout(ILoggingEvent e) {
		StringBuilder builder = new StringBuilder(1000);
		builder.append("[").append(df.format(new Date(e.getTimeStamp()))).append("] ");
		builder.append("[").append(e.getLevel()).append("] ");
		builder.append(e.getFormattedMessage());
		IThrowableProxy proxy = e.getThrowableProxy();
		if (proxy != null) {
			ThrowableProxyConverter converter = new ThrowableProxyConverter();
			converter.setOptionList(stackOptionList);
			converter.start();
			builder.append(converter.convert(e));
			builder.append(CoreConstants.LINE_SEPARATOR);
		}
		builder.append("\n");
		return builder.toString();
	}

	public String getFileHeader() {
		return "ResonantBot\n";
	}

}