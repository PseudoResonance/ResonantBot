package com.github.pseudoresonance.resonantbot.language;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Language {

	private final String lang;
	private final ConcurrentHashMap<String, String> languageMap = new ConcurrentHashMap<String, String>();

	private static Pattern dateFormatPattern = Pattern.compile("\\{\\$date\\.date\\$\\}");
	private static Pattern dateTimeFormatPattern = Pattern.compile("\\{\\$date\\.dateTime\\$\\}");
	private static Pattern timeFormatPattern = Pattern.compile("\\{\\$date\\.time\\$\\}");
	private static Pattern pattern = Pattern.compile("\\{\\$([0-9]+)\\$\\}");

	private Locale locale = null;

	private DateTimeFormatter dateFormat = null;
	private DateTimeFormatter dateTimeFormat = null;
	private DateTimeFormatter timeFormat = null;

	private static Pattern relativeFormatPattern = Pattern.compile("\\{\\$1\\$\\}");

	private boolean relativeFormatAscending = false;
	private String relativeNanoseconds = "";
	private String relativeMilliseconds = "";
	private String relativeSeconds = "";
	private String relativeMinutes = "";
	private String relativeHours = "";
	private String relativeDays = "";
	private String relativeMonths = "";
	private String relativeYears = "";
	private String relativeNanosecondsSingular = "";
	private String relativeMillisecondsSingular = "";
	private String relativeSecondsSingular = "";
	private String relativeMinutesSingular = "";
	private String relativeHoursSingular = "";
	private String relativeDaysSingular = "";
	private String relativeMonthsSingular = "";
	private String relativeYearsSingular = "";

	/**
	 * Constructs new {@link Language} with the given name
	 * 
	 * @param lang Localization name
	 */
	public Language(String lang) {
		this.lang = lang;
	}

	/**
	 * Returns language name
	 * 
	 * @return Localization name
	 */
	public String getName() {
		return lang;
	}

	/**
	 * Gets raw localization message at the given key
	 * 
	 * @param key Localization key
	 * @return Raw localization message
	 */
	public String getUnprocessedMessage(String key) {
		return languageMap.get(key);
	}

	/**
	 * Gets processed localization message at the given key with the given parameters
	 * 
	 * @param key Localization key
	 * @param args Arguments to pass into message
	 * @return Localized message
	 */
	public String getMessage(String key, Object... args) {
		String msg = languageMap.get(key);
		if (msg == null)
			msg = LanguageManager.getLanguage().getUnprocessedMessage(key);
		if (msg != null) {
			StringBuilder sb = new StringBuilder();
			Matcher match = pattern.matcher(msg);
			int msgI = 0;
			int i = 0;
			while (match.find()) {
				i = Integer.valueOf(match.group(1));
				i--;
				sb.append(msg.substring(msgI, match.start()));
				if (i < args.length && i >= 0)
					sb.append(args[i].toString());
				else
					sb.append(match.group(0));
				msgI = match.end();
			}
			sb.append(msg.substring(msgI, msg.length()));
			msg = sb.toString();
			msg = dateFormatPattern.matcher(msg).replaceAll(languageMap.get("date.dateFormatHumanReadable"));
			msg = dateTimeFormatPattern.matcher(msg).replaceAll(languageMap.get("date.dateTimeFormatHumanReadable"));
			msg = timeFormatPattern.matcher(msg).replaceAll(languageMap.get("date.timeFormatHumanReadable"));
			return msg;
		}
		return "Error: Localization for " + key + " is missing!";
	}

	/**
	 * Formats date
	 * 
	 * @param date {@link LocalDate} to format
	 * @return Formatted date
	 */
	public String formatDate(LocalDate date) {
		return dateFormat.format(date);
	}

	/**
	 * Formats date
	 * 
	 * @param date {@link Date} to format
	 * @return Formatted date
	 */
	public String formatDate(Date date) {
		return formatDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}

	/**
	 * Formats date-time
	 * 
	 * @param dateTime {@link LocalDateTime} to format
	 * @return Formatted date-time
	 */
	public String formatDateTime(LocalDateTime dateTime) {
		return dateTimeFormat.format(dateTime);
	}

	/**
	 * Formats date-time
	 * 
	 * @param dateTime {@link Date} to format
	 * @return Formatted date-time
	 */
	public String formatDateTime(Date dateTime) {
		return formatDateTime(dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	/**
	 * Formats time
	 * 
	 * @param time {@link LocalTime} to format
	 * @return Formatted time
	 */
	public String formatTime(LocalTime time) {
		return timeFormat.format(time);
	}

	/**
	 * Formats time
	 * 
	 * @param time {@link Date} to format
	 * @return Formatted time
	 */
	public String formatTime(Date time) {
		return formatTime(time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link LocalDateTime} to format
	 * @param addAgo Whether or not to add "ago" to the end of the string
	 * @param minUnit Minimum time unit to display
	 * @param maxUnit Maximum time unit to display
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(LocalDateTime dateTime, boolean addAgo, ChronoUnit minUnit, ChronoUnit maxUnit) {
		if (relativeFormatAscending)
			return timeAgoAscending(dateTime, addAgo, minUnit, maxUnit);
		else
			return timeAgoDescending(dateTime, addAgo, minUnit, maxUnit);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link LocalDateTime} to format
	 * @param minUnit Minimum time unit to display
	 * @param maxUnit Maximum time unit to display
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(LocalDateTime dateTime, ChronoUnit minUnit, ChronoUnit maxUnit) {
		if (relativeFormatAscending)
			return timeAgoAscending(dateTime, true, minUnit, maxUnit);
		else
			return timeAgoDescending(dateTime, true, minUnit, maxUnit);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link Date} to format
	 * @param addAgo Whether or not to add "ago" to the end of the string
	 * @param minUnit Minimum time unit to display
	 * @param maxUnit Maximum time unit to display
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(Date dateTime, boolean addAgo, ChronoUnit minUnit, ChronoUnit maxUnit) {
		return formatTimeAgo(dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), addAgo, minUnit, maxUnit);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link Date} to format
	 * @param minUnit Minimum time unit to display
	 * @param maxUnit Maximum time unit to display
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(Date dateTime, ChronoUnit minUnit, ChronoUnit maxUnit) {
		return formatTimeAgo(dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), minUnit, maxUnit);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link LocalDateTime} to format
	 * @param addAgo Whether or not to add "ago" to the end of the string
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(LocalDateTime dateTime, boolean addAgo) {
		if (relativeFormatAscending)
			return timeAgoAscending(dateTime, addAgo);
		else
			return timeAgoDescending(dateTime, addAgo);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link LocalDateTime} to format
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(LocalDateTime dateTime) {
		if (relativeFormatAscending)
			return timeAgoAscending(dateTime);
		else
			return timeAgoDescending(dateTime);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link Date} to format
	 * @param addAgo Whether or not to add "ago" to the end of the string
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(Date dateTime, boolean addAgo) {
		return formatTimeAgo(dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), addAgo);
	}

	/**
	 * Formats time ago
	 * 
	 * @param dateTime {@link Date} to format
	 * @return Formatted time ago
	 */
	public String formatTimeAgo(Date dateTime) {
		return formatTimeAgo(dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	private String timeAgoAscending(LocalDateTime dateTime, boolean addAgo, ChronoUnit minUnit, ChronoUnit maxUnit) {
		LocalDateTime temp = LocalDateTime.from(dateTime);
		LocalDateTime now = LocalDateTime.now();
		long years = temp.until(now, ChronoUnit.YEARS);
		temp = temp.plusYears(years);
		long months = temp.until(now, ChronoUnit.MONTHS);
		temp = temp.plusMonths(months);
		long days = temp.until(now, ChronoUnit.DAYS);
		temp = temp.plusDays(days);
		long hours = temp.until(now, ChronoUnit.HOURS);
		temp = temp.plusHours(hours);
		long minutes = temp.until(now, ChronoUnit.MINUTES);
		temp = temp.plusMinutes(minutes);
		long seconds = temp.until(now, ChronoUnit.SECONDS);
		temp = temp.plusSeconds(seconds);
		long milliseconds = temp.until(now, ChronoUnit.MILLIS);
		temp = temp.plus(milliseconds, ChronoUnit.MILLIS);
		long nanoseconds = temp.until(now, ChronoUnit.NANOS);
		String sYears = "";
		String sMonths = "";
		String sDays = "";
		String sHours = "";
		String sMinutes = "";
		String sSeconds = "";
		String sMilliseconds = "";
		String sNanoseconds = "";
		if (relativeYears.length() > 0 && isUnitInRange(minUnit, ChronoUnit.YEARS, maxUnit) && (years > 0 || minUnit == ChronoUnit.YEARS))
			if (years == 1 && relativeYearsSingular.length() > 0)
				sYears = relativeFormatPattern.matcher(relativeYearsSingular).replaceFirst(String.valueOf(years)) + " ";
			else
				sYears = relativeFormatPattern.matcher(relativeYears).replaceFirst(String.valueOf(years)) + " ";
		else
			months += years * 12;
		if (relativeMonths.length() > 0 && isUnitInRange(minUnit, ChronoUnit.MONTHS, maxUnit) && (months > 0 || minUnit == ChronoUnit.MONTHS))
			if (months == 1 && relativeMonthsSingular.length() > 0)
				sMonths = relativeFormatPattern.matcher(relativeMonthsSingular).replaceFirst(String.valueOf(months)) + " ";
			else
				sMonths = relativeFormatPattern.matcher(relativeMonths).replaceFirst(String.valueOf(months)) + " ";
		else
			days += (long) (months * 30.436875);
		if (relativeDays.length() > 0 && isUnitInRange(minUnit, ChronoUnit.DAYS, maxUnit) && (days > 0 || minUnit == ChronoUnit.DAYS))
			if (days == 1 && relativeDaysSingular.length() > 0)
				sDays = relativeFormatPattern.matcher(relativeDaysSingular).replaceFirst(String.valueOf(days)) + " ";
			else
				sDays = relativeFormatPattern.matcher(relativeDays).replaceFirst(String.valueOf(days)) + " ";
		else
			hours += days * 24;
		if (relativeHours.length() > 0 && isUnitInRange(minUnit, ChronoUnit.HOURS, maxUnit) && (hours > 0 || minUnit == ChronoUnit.HOURS))
			if (hours == 1 && relativeHoursSingular.length() > 0)
				sHours = relativeFormatPattern.matcher(relativeHoursSingular).replaceFirst(String.valueOf(hours)) + " ";
			else
				sHours = relativeFormatPattern.matcher(relativeHours).replaceFirst(String.valueOf(hours)) + " ";
		else
			minutes += hours * 60;
		if (relativeMinutes.length() > 0 && isUnitInRange(minUnit, ChronoUnit.MINUTES, maxUnit) && (minutes > 0 || minUnit == ChronoUnit.MINUTES))
			if (minutes == 1 && relativeMinutesSingular.length() > 0)
				sMinutes = relativeFormatPattern.matcher(relativeMinutesSingular).replaceFirst(String.valueOf(minutes)) + " ";
			else
				sMinutes = relativeFormatPattern.matcher(relativeMinutes).replaceFirst(String.valueOf(minutes)) + " ";
		else
			seconds += minutes * 60;
		if (relativeSeconds.length() > 0 && isUnitInRange(minUnit, ChronoUnit.SECONDS, maxUnit) && (seconds > 0 || minUnit == ChronoUnit.SECONDS))
			if (seconds == 1 && relativeSecondsSingular.length() > 0)
				sSeconds = relativeFormatPattern.matcher(relativeSecondsSingular).replaceFirst(String.valueOf(seconds)) + " ";
			else
				sSeconds = relativeFormatPattern.matcher(relativeSeconds).replaceFirst(String.valueOf(seconds)) + " ";
		else
			milliseconds += seconds * 1000;
		if (relativeMilliseconds.length() > 0 && isUnitInRange(minUnit, ChronoUnit.MILLIS, maxUnit) && (milliseconds > 0 || minUnit == ChronoUnit.MILLIS))
			if (milliseconds == 1 && relativeMillisecondsSingular.length() > 0)
				sMilliseconds = relativeFormatPattern.matcher(relativeMillisecondsSingular).replaceFirst(String.valueOf(milliseconds)) + " ";
			else
				sMilliseconds = relativeFormatPattern.matcher(relativeMilliseconds).replaceFirst(String.valueOf(milliseconds)) + " ";
		else
			nanoseconds += milliseconds * 1000000;
		if (relativeNanoseconds.length() > 0 && isUnitInRange(minUnit, ChronoUnit.NANOS, maxUnit) && (nanoseconds > 0 || minUnit == ChronoUnit.NANOS))
			if (nanoseconds == 1 && relativeNanosecondsSingular.length() > 0)
				sNanoseconds = relativeFormatPattern.matcher(relativeNanosecondsSingular).replaceFirst(String.valueOf(nanoseconds)) + " ";
			else
				sNanoseconds = relativeFormatPattern.matcher(relativeNanoseconds).replaceFirst(String.valueOf(nanoseconds)) + " ";
		String ret = sNanoseconds + sMilliseconds + sSeconds + sMinutes + sHours + sDays + sMonths + sYears;
		if (addAgo)
			return getMessage("date.relativeAgo", ret.substring(0, ret.length() - 1));
		return ret.substring(0, ret.length() - 1);
	}

	private String timeAgoAscending(LocalDateTime dateTime, boolean addAgo) {
		return timeAgoAscending(dateTime, addAgo, ChronoUnit.SECONDS, ChronoUnit.YEARS);
	}

	private String timeAgoAscending(LocalDateTime dateTime) {
		return timeAgoAscending(dateTime, true, ChronoUnit.SECONDS, ChronoUnit.YEARS);
	}

	private String timeAgoDescending(LocalDateTime dateTime, boolean addAgo, ChronoUnit minUnit, ChronoUnit maxUnit) {
		LocalDateTime temp = LocalDateTime.from(dateTime);
		LocalDateTime now = LocalDateTime.now();
		long years = temp.until(now, ChronoUnit.YEARS);
		temp = temp.plusYears(years);
		long months = temp.until(now, ChronoUnit.MONTHS);
		temp = temp.plusMonths(months);
		long days = temp.until(now, ChronoUnit.DAYS);
		temp = temp.plusDays(days);
		long hours = temp.until(now, ChronoUnit.HOURS);
		temp = temp.plusHours(hours);
		long minutes = temp.until(now, ChronoUnit.MINUTES);
		temp = temp.plusMinutes(minutes);
		long seconds = temp.until(now, ChronoUnit.SECONDS);
		temp = temp.plusSeconds(seconds);
		long milliseconds = temp.until(now, ChronoUnit.MILLIS);
		temp = temp.plus(milliseconds, ChronoUnit.MILLIS);
		long nanoseconds = temp.until(now, ChronoUnit.NANOS);
		String sYears = "";
		String sMonths = "";
		String sDays = "";
		String sHours = "";
		String sMinutes = "";
		String sSeconds = "";
		String sMilliseconds = "";
		String sNanoseconds = "";
		if (relativeYears.length() > 0 && isUnitInRange(minUnit, ChronoUnit.YEARS, maxUnit) && (years > 0 || minUnit == ChronoUnit.YEARS))
			if (years == 1 && relativeYearsSingular.length() > 0)
				sYears = relativeFormatPattern.matcher(relativeYearsSingular).replaceFirst(String.valueOf(years)) + " ";
			else
				sYears = relativeFormatPattern.matcher(relativeYears).replaceFirst(String.valueOf(years)) + " ";
		else
			months += years * 12;
		if (relativeMonths.length() > 0 && isUnitInRange(minUnit, ChronoUnit.MONTHS, maxUnit) && (months > 0 || minUnit == ChronoUnit.MONTHS))
			if (months == 1 && relativeMonthsSingular.length() > 0)
				sMonths = relativeFormatPattern.matcher(relativeMonthsSingular).replaceFirst(String.valueOf(months)) + " ";
			else
				sMonths = relativeFormatPattern.matcher(relativeMonths).replaceFirst(String.valueOf(months)) + " ";
		else
			days += (long) (months * 30.436875);
		if (relativeDays.length() > 0 && isUnitInRange(minUnit, ChronoUnit.DAYS, maxUnit) && (days > 0 || minUnit == ChronoUnit.DAYS))
			if (days == 1 && relativeDaysSingular.length() > 0)
				sDays = relativeFormatPattern.matcher(relativeDaysSingular).replaceFirst(String.valueOf(days)) + " ";
			else
				sDays = relativeFormatPattern.matcher(relativeDays).replaceFirst(String.valueOf(days)) + " ";
		else
			hours += days * 24;
		if (relativeHours.length() > 0 && isUnitInRange(minUnit, ChronoUnit.HOURS, maxUnit) && (hours > 0 || minUnit == ChronoUnit.HOURS))
			if (hours == 1 && relativeHoursSingular.length() > 0)
				sHours = relativeFormatPattern.matcher(relativeHoursSingular).replaceFirst(String.valueOf(hours)) + " ";
			else
				sHours = relativeFormatPattern.matcher(relativeHours).replaceFirst(String.valueOf(hours)) + " ";
		else
			minutes += hours * 60;
		if (relativeMinutes.length() > 0 && isUnitInRange(minUnit, ChronoUnit.MINUTES, maxUnit) && (minutes > 0 || minUnit == ChronoUnit.MINUTES))
			if (minutes == 1 && relativeMinutesSingular.length() > 0)
				sMinutes = relativeFormatPattern.matcher(relativeMinutesSingular).replaceFirst(String.valueOf(minutes)) + " ";
			else
				sMinutes = relativeFormatPattern.matcher(relativeMinutes).replaceFirst(String.valueOf(minutes)) + " ";
		else
			seconds += minutes * 60;
		if (relativeSeconds.length() > 0 && isUnitInRange(minUnit, ChronoUnit.SECONDS, maxUnit) && (seconds > 0 || minUnit == ChronoUnit.SECONDS))
			if (seconds == 1 && relativeSecondsSingular.length() > 0)
				sSeconds = relativeFormatPattern.matcher(relativeSecondsSingular).replaceFirst(String.valueOf(seconds)) + " ";
			else
				sSeconds = relativeFormatPattern.matcher(relativeSeconds).replaceFirst(String.valueOf(seconds)) + " ";
		else
			milliseconds += seconds * 1000;
		if (relativeMilliseconds.length() > 0 && isUnitInRange(minUnit, ChronoUnit.MILLIS, maxUnit) && (milliseconds > 0 || minUnit == ChronoUnit.MILLIS))
			if (milliseconds == 1 && relativeMillisecondsSingular.length() > 0)
				sMilliseconds = relativeFormatPattern.matcher(relativeMillisecondsSingular).replaceFirst(String.valueOf(milliseconds)) + " ";
			else
				sMilliseconds = relativeFormatPattern.matcher(relativeMilliseconds).replaceFirst(String.valueOf(milliseconds)) + " ";
		else
			nanoseconds += milliseconds * 1000000;
		if (relativeNanoseconds.length() > 0 && isUnitInRange(minUnit, ChronoUnit.NANOS, maxUnit) && (nanoseconds > 0 || minUnit == ChronoUnit.NANOS))
			if (nanoseconds == 1 && relativeNanosecondsSingular.length() > 0)
				sNanoseconds = relativeFormatPattern.matcher(relativeNanosecondsSingular).replaceFirst(String.valueOf(nanoseconds)) + " ";
			else
				sNanoseconds = relativeFormatPattern.matcher(relativeNanoseconds).replaceFirst(String.valueOf(nanoseconds)) + " ";
		String ret = sYears + sMonths + sDays + sHours + sMinutes + sSeconds + sMilliseconds + sNanoseconds;
		if (addAgo)
			return getMessage("date.relativeAgo", ret.substring(0, ret.length() - 1));
		return ret.substring(0, ret.length() - 1);
	}

	private String timeAgoDescending(LocalDateTime dateTime, boolean addAgo) {
		return timeAgoDescending(dateTime, addAgo, ChronoUnit.SECONDS, ChronoUnit.YEARS);
	}

	private String timeAgoDescending(LocalDateTime dateTime) {
		return timeAgoDescending(dateTime, true, ChronoUnit.SECONDS, ChronoUnit.YEARS);
	}

	private boolean isUnitInRange(ChronoUnit min, ChronoUnit test, ChronoUnit max) {
		if (test.compareTo(min) >= 0 && test.compareTo(max) <= 0)
			return true;
		return false;
	}
	
	public void resetLanguageMap() {
		languageMap.clear();
	}

	/**
	 * Adds given language key and message map to the language
	 * 
	 * @param map Language key and message map
	 */
	public void addLanguageMap(HashMap<String, String> map) {
		languageMap.putAll(map);
		if (locale == null && languageMap.keySet().contains("date.locale")) {
			locale = Locale.forLanguageTag(languageMap.get("date.locale"));
		}
		if (locale != null && dateFormat == null && languageMap.keySet().contains("date.dateFormat")) {

			dateFormat = DateTimeFormatter.ofPattern(languageMap.get("date.dateFormat"), locale);
		}
		if (locale != null && dateTimeFormat == null && languageMap.keySet().contains("date.dateTimeFormat")) {
			dateTimeFormat = DateTimeFormatter.ofPattern(languageMap.get("date.dateTimeFormat"), locale);
		}
		if (locale != null && timeFormat == null && languageMap.keySet().contains("date.timeFormat")) {
			timeFormat = DateTimeFormatter.ofPattern(languageMap.get("date.timeFormat"), locale);
		}
		if (languageMap.keySet().contains("date.relativeNanoseconds")) {
			relativeNanoseconds = languageMap.get("date.relativeNanoseconds");
		}
		if (languageMap.keySet().contains("date.relativeMilliseconds")) {
			relativeMilliseconds = languageMap.get("date.relativeMilliseconds");
		}
		if (languageMap.keySet().contains("date.relativeSeconds")) {
			relativeSeconds = languageMap.get("date.relativeSeconds");
		}
		if (languageMap.keySet().contains("date.relativeMinutes")) {
			relativeMinutes = languageMap.get("date.relativeMinutes");
		}
		if (languageMap.keySet().contains("date.relativeHours")) {
			relativeHours = languageMap.get("date.relativeHours");
		}
		if (languageMap.keySet().contains("date.relativeDays")) {
			relativeDays = languageMap.get("date.relativeDays");
		}
		if (languageMap.keySet().contains("date.relativeMonths")) {
			relativeMonths = languageMap.get("date.relativeMonths");
		}
		if (languageMap.keySet().contains("date.relativeYears")) {
			relativeYears = languageMap.get("date.relativeYears");
		}
		if (languageMap.keySet().contains("date.relativeNanosecondsSingular")) {
			relativeNanosecondsSingular = languageMap.get("date.relativeNanosecondsSingular");
		}
		if (languageMap.keySet().contains("date.relativeMillisecondsSingular")) {
			relativeMillisecondsSingular = languageMap.get("date.relativeMillisecondsSingular");
		}
		if (languageMap.keySet().contains("date.relativeSecondsSingular")) {
			relativeSecondsSingular = languageMap.get("date.relativeSecondsSingular");
		}
		if (languageMap.keySet().contains("date.relativeMinutesSingular")) {
			relativeMinutesSingular = languageMap.get("date.relativeMinutesSingular");
		}
		if (languageMap.keySet().contains("date.relativeHoursSingular")) {
			relativeHoursSingular = languageMap.get("date.relativeHoursSingular");
		}
		if (languageMap.keySet().contains("date.relativeDaysSingular")) {
			relativeDaysSingular = languageMap.get("date.relativeDaysSingular");
		}
		if (languageMap.keySet().contains("date.relativeMonthsSingular")) {
			relativeMonthsSingular = languageMap.get("date.relativeMonthsSingular");
		}
		if (languageMap.keySet().contains("date.relativeYearsSingular")) {
			relativeYearsSingular = languageMap.get("date.relativeYearsSingular");
		}
		if (languageMap.keySet().contains("date.relativeFormatAscending")) {
			relativeFormatAscending = languageMap.get("date.relativeFormatAscending").toLowerCase().startsWith("t") ? true : false;
		}
	}

}
