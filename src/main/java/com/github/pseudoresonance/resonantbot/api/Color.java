package com.github.pseudoresonance.resonantbot.api;

public enum Color {

	RESET("\u001B[0m"),
	BLACK("\u001B[30m"),
	RED("\u001B[31m"),
	GREEN("\u001B[32m"),
	YELLOW("\u001B[33m"),
	BLUE("\u001B[34m"),
	MAGENTA("\u001B[35m"),
	CYAN("\u001B[36m"),
	WHITE("\u001B[37m"),
	BRIGHT_BLACK("\u001B[1;30m"),
	BRIGHT_RED("\u001B[1;31m"),
	BRIGHT_GREEN("\u001B[1;32m"),
	BRIGHT_YELLOW("\u001B[1;33m"),
	BRIGHT_BLUE("\u001B[1;34m"),
	BRIGHT_MAGENTA("\u001B[1;35m"),
	BRIGHT_CYAN("\u001B[1;36m"),
	BRIGHT_WHITE("\u001B[1;37m"),
	BLACK_BACKGROUND("\u001B[40m"),
	RED_BACKGROUND("\u001B[41m"),
	GREEN_BACKGROUND("\u001B[42m"),
	YELLOW_BACKGROUND("\u001B[43m"),
	BLUE_BACKGROUND("\u001B[44m"),
	MAGENTA_BACKGROUND("\u001B[45m"),
	CYAN_BACKGROUND("\u001B[46m"),
	WHITE_BACKGROUND("\u001B[47m"),
	BRIGHT_BLACK_BACKGROUND("\u001B[100m"),
	BRIGHT_RED_BACKGROUND("\u001B[101m"),
	BRIGHT_GREEN_BACKGROUND("\u001B[102m"),
	BRIGHT_YELLOW_BACKGROUND("\u001B[103m"),
	BRIGHT_BLUE_BACKGROUND("\u001B[104m"),
	BRIGHT_MAGENTA_BACKGROUND("\u001B[105m"),
	BRIGHT_CYAN_BACKGROUND("\u001B[106m"),
	BRIGHT_WHITE_BACKGROUND("\u001B[107m");
	
	private String code;
	
	Color(String code) {
		this.code = code;
	}
	
	public String toString() {
		return this.code;
	}
	
}
