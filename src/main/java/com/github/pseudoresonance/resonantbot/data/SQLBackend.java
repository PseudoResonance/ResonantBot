package com.github.pseudoresonance.resonantbot.data;

import org.apache.commons.dbcp2.BasicDataSource;

public abstract class SQLBackend extends Backend {
	
	public abstract BasicDataSource getDataSource();
	
	public abstract String getPrefix();

}
