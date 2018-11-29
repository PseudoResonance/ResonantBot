package com.github.pseudoresonance.resonantbot.data;

import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class MySQLBackend extends SQLBackend {

	private BasicDataSource dataSource;

	private boolean enabled = false;

	private final String name;
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String database;
	private final String prefix;
	private final boolean ssl;
	private final String url;

	public MySQLBackend(String name, String host, int port, String username, String password, String database, String prefix, boolean ssl) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
		this.prefix = prefix;
		this.ssl = ssl;
		if (ssl)
			this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=true";
		else
			this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
	}

	public BasicDataSource getDataSource() {
		if (enabled) {
			if (dataSource == null) {
				BasicDataSource ds = new BasicDataSource();
				ds.setUrl(this.url);
				ds.setUsername(this.username);
				ds.setPassword(this.password);
				ds.setMinIdle(1);
				ds.setMaxIdle(10);
				ds.setMaxOpenPreparedStatements(100);
				dataSource = ds;
			}
			return dataSource;
		} else
			return null;
	}

	public void setup() {
		enabled = true;
		getDataSource();
	}

	public void stop() {
		enabled = false;
		if (dataSource != null) {
			try {
				dataSource.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			dataSource = null;
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public String getName() {
		return this.name;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getDatabase() {
		return this.database;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public boolean getSSL() {
		return this.ssl;
	}

	public String getURL() {
		return this.url;
	}

	public boolean equals(Backend obj) {
		if (obj instanceof MySQLBackend) {
			MySQLBackend b = (MySQLBackend) obj;
			if (b.getHost().equals(this.host) && b.getPort() == this.port && b.getUsername().equals(this.username) && b.getPassword().equals(this.password) && b.getDatabase().equals(this.database) && b.getPrefix().equals(this.prefix)) {
				return true;
			}
		}
		return false;
	}

}
