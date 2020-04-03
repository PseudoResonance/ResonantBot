package com.github.pseudoresonance.resonantbot.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;

import com.github.pseudoresonance.resonantbot.Config;

public class DynamicTable {
	
	private static final ArrayList<DynamicTable> instances = new ArrayList<DynamicTable>();
	
	private static Logger log = null;

	private Backend backend = Config.getBackend();

	private ArrayList<Column> dataTypes = new ArrayList<Column>();
	private ArrayList<Column> serverDataTypes = new ArrayList<Column>();
	
	private HashMap<String, HashMap<String, Object>> data = new HashMap<String, HashMap<String, Object>>();
	
	private String name;
	private String idType;
	
	public static void init(Logger log) {
		DynamicTable.log = log;
	}
	
	public DynamicTable(String name, String idType) {
		this.name = name;
		this.idType = idType;
	}


	/**
	 * Shuts down this instance of {@link DynamicTable}
	 */
	public void shutdown() {
		instances.remove(this);
	}
	
	/**
	 * Shuts down all instances of {@link DynamicTable}
	 */
	public static void shutdownAll() {
		instances.clear();
	}

	public void update() {
		Backend b = Config.getBackend();
		if (!b.equals(backend)) {
			setup();
			data.clear();
		}
	}

	public boolean addColumn(Column col) {
		for (Column column : dataTypes)
			if (column.getName().equalsIgnoreCase(col.getName()))
				return false;
		dataTypes.add(col);
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				for (Column column : serverDataTypes) {
					if (column.getName().equalsIgnoreCase(col.getName())) {
						try (Statement st = c.createStatement()) {
							String key = col.getName();
							String value = col.getType();
							String defaultValue = col.getDefaultValue();
							try {
								if (!defaultValue.equalsIgnoreCase("NULL"))
									defaultValue = "'" + defaultValue + "'";
								st.execute("ALTER TABLE `" + sb.getPrefix() + name + "` MODIFY " + key + " " + value + " DEFAULT " + defaultValue + ";");
								return true;
							} catch (SQLException e) {
								int error = e.getErrorCode();
								if (backend instanceof MySQLBackend) {
									if (error == 1406 || error == 1264 || error == 1265 || error == 1366 || error == 1292) {
										try (Statement st2 = c.createStatement()) {
											String suffix = "_OLD";
											try {
												st2.execute("ALTER TABLE `" + sb.getPrefix() + name + "` CHANGE " + key + " " + key + suffix + " " + value + ";");
												return true;
											} catch (SQLException ex) {
												log.error("Error when renaming column: " + key + " to: " + key + suffix + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
												log.error("SQLError " + ex.getErrorCode() + ": (State: " + ex.getSQLState() + ") - " + ex.getMessage());
												break;
											}
										} catch (SQLException ex) {
											log.error("Error when creating statement in database: " + sb.getName());
											log.error("SQLError " + ex.getErrorCode() + ": (State: " + ex.getSQLState() + ") - " + ex.getMessage());
										}
									}
								}
								log.error("Error when converting column: " + key + " to type: " + value + " with default value: " + defaultValue + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
								log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
						} catch (SQLException e) {
							log.error("Error when creating statement in database: " + sb.getName());
							log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
						}
					}
				}
				try (Statement st = c.createStatement()) {
					String key = col.getName();
					String value = col.getType();
					String defaultValue = col.getDefaultValue();
					try {
						if (!defaultValue.equalsIgnoreCase("NULL"))
							defaultValue = "'" + defaultValue + "'";
						st.execute("ALTER TABLE `" + sb.getPrefix() + name + "` ADD " + key + " " + value + " DEFAULT " + defaultValue + ";");
						return true;
					} catch (SQLException e) {
						log.error("Error when adding column: " + key + " of type: " + value + " with default value: " + defaultValue + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error when creating statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
		return false;
	}

	public void setup() {
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (Statement s = c.createStatement()) {
					ArrayList<Column> newColumns = new ArrayList<Column>();
					ArrayList<Column> editColumns = new ArrayList<Column>();
					ArrayList<Column> oldColumns = new ArrayList<Column>();
					ArrayList<Column> columns = new ArrayList<Column>();
					boolean idCol = false;
					boolean create = false;
					try (ResultSet rs = s.executeQuery("DESCRIBE `" + sb.getPrefix() + name + "`;")) {
						while (rs.next()) {
							String field = rs.getString("Field");
							String type = rs.getString("Type");
							String defaultValue = String.valueOf(rs.getObject("Default"));
							if (field.equalsIgnoreCase("id"))
								idCol = true;
							else
								columns.add(new Column(field, type, defaultValue));
						}
					} catch (SQLException e) {
						if (e.getErrorCode() == 1146) {
							create = true;
						} else {
							log.error("Error when getting table description in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
							log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
						}
					}
					serverDataTypes = columns;
					if (create) {
						try (Statement st = c.createStatement()) {
							try {
								st.execute("CREATE TABLE IF NOT EXISTS `" + sb.getPrefix() + name + "` (`id` " + idType + " PRIMARY KEY);");
							} catch (SQLException e) {
								log.error("Error when creating table: " + sb.getPrefix() + name + " in database: " + sb.getName());
								log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
						} catch (SQLException e) {
							log.error("Error when creating statement in database: " + sb.getName());
							log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
						}
						try (Statement st = c.createStatement()) {
							for (Column col : dataTypes) {
								String key = col.getName();
								String value = col.getType();
								String defaultValue = col.getDefaultValue();
								try {
									st.execute("ALTER TABLE `" + sb.getPrefix() + name + "` ADD " + key + " " + value + " DEFAULT " + defaultValue + ";");
								} catch (SQLException e) {
									log.error("Error when adding column: " + key + " of type: " + value + " with default value: " + defaultValue + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
									log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
								}
							}
						} catch (SQLException e) {
							log.error("Error when creating statement in database: " + sb.getName());
							log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
						}
					} else {
						if (idCol) {
							for (Column col : dataTypes) {
								boolean found = false;
								for (Column testCol : columns) {
									if (testCol.equals(col)) {
										found = true;
										break;
									} else if (testCol.getName().equalsIgnoreCase(col.getName())) {
										editColumns.add(col);
										found = true;
										break;
									}
								}
								if (!found)
									newColumns.add(col);
							}
							try (Statement st = c.createStatement()) {
								for (Column col : editColumns) {
									String key = col.getName();
									String value = col.getType();
									String defaultValue = col.getDefaultValue();
									try {
										st.execute("ALTER TABLE `" + sb.getPrefix() + name + "` MODIFY " + key + " " + value + " DEFAULT " + defaultValue + ";");
									} catch (SQLException e) {
										int error = e.getErrorCode();
										if (backend instanceof MySQLBackend) {
											if (error == 1406 || error == 1264 || error == 1265 || error == 1366 || error == 1292) {
												oldColumns.add(col);
												newColumns.add(col);
											}
										}
										log.error("Error when converting column: " + key + " to type: " + value + " with default value: " + defaultValue + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
										log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
									}
								}
							} catch (SQLException e) {
								log.error("Error when creating statement in database: " + sb.getName());
								log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
							try (Statement st = c.createStatement()) {
								for (Column col : oldColumns) {
									String key = col.getName();
									String value = col.getType();
									String suffix = "_OLD";
									try {
										st.execute("ALTER TABLE `" + sb.getPrefix() + name + "` CHANGE " + key + " " + key + suffix + " " + value + ";");
									} catch (SQLException e) {
										log.error("Error when renaming column: " + key + " to: " + key + suffix + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
										log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
									}
								}
							} catch (SQLException e) {
								log.error("Error when creating statement in database: " + sb.getName());
								log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
							try (Statement st = c.createStatement()) {
								for (Column col : newColumns) {
									String key = col.getName();
									String value = col.getType();
									String defaultValue = col.getDefaultValue();
									try {
										st.execute("ALTER TABLE `" + sb.getPrefix() + name + "` ADD " + key + " " + value + " DEFAULT " + defaultValue + ";");
									} catch (SQLException e) {
										log.error("Error when adding column: " + key + " of type: " + value + " with default value: " + defaultValue + " in table: " + sb.getPrefix() + name + " in database: " + sb.getName());
										log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
									}
								}
							} catch (SQLException e) {
								log.error("Error when creating statement in database: " + sb.getName());
								log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
						} else {
							log.error("Selected backend contains incorrectly formatted table: " + sb.getPrefix() + name + "!");
							System.exit(1);
						}
					}
				} catch (SQLException e) {
					log.error("Error when creating statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
	}

	public void setSettings(String id, HashMap<String, Object> values) {
		HashMap<String, Object> original;
		HashMap<String, Object> changed = new HashMap<String, Object>();
		if (data.containsKey(id)) {
			original = data.get(id);
			if (original == null) {
				original = get(id);
			}
		} else {
			original = get(id);
		}
		if (original == null) {
			original = new HashMap<String, Object>();
		}
		for (String key : values.keySet()) {
			Object o = values.get(key);
			if (original.containsKey(key)) {
				Object test = original.get(key);
				if (test != null)
					if (test.equals(o))
						continue;
			}
			changed.put(key, o);
			original.put(key, o);
		}
		if (data.containsKey(id))
			data.put(id, original);
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				for (String key : changed.keySet()) {
					try (PreparedStatement ps = c.prepareStatement("INSERT INTO `" + sb.getPrefix() + name + "` (`id`,`" + key + "`) VALUES (?,?) ON DUPLICATE KEY UPDATE `" + key + "`=?;")) {
						Object value = changed.get(key);
						ps.setString(1, id);
						ps.setObject(2, value);
						ps.setObject(3, value);
						try {
							ps.execute();
						} catch (SQLException e) {
							log.error("Error updating id: " + id + " from table: " + sb.getPrefix() + name + " in key: " + key + " with value: " + String.valueOf(value) + " in database: " + sb.getName());
							log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
						}
					} catch (SQLException e) {
						log.error("Error when preparing statement in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
	}

	public void setSetting(String id, String key, Object value) {
		HashMap<String, Object> original;
		if (data.containsKey(id)) {
			original = data.get(id);
			if (original == null) {
				original = get(id);
			}
		} else {
			original = get(id);
		}
		if (original == null) {
			original = new HashMap<String, Object>();
		}
		if (original.containsKey(key)) {
			Object test = original.get(key);
			if (test != null)
				if (test.equals(value))
					return;
		}
		original.put(key, value);
		if (data.containsKey(id))
			data.put(id, original);
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("INSERT INTO `" + sb.getPrefix() + name + "` (`id`,`" + key + "`) VALUES (?,?) ON DUPLICATE KEY UPDATE `" + key + "`=?;")) {
					ps.setString(1, id);
					ps.setObject(2, value);
					ps.setObject(3, value);
					try {
						ps.execute();
					} catch (SQLException e) {
						log.error("Error updating id: " + id + " from table: " + sb.getPrefix() + name + " in key: " + key + " with value: " + String.valueOf(value) + " in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error when preparing statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
	}

	public Object getSetting(String id, String key) {
		HashMap<String, Object> temp = data.get(id);
		if (temp != null && !(temp.isEmpty()))
			return temp.get(key);
		else
			return getSingle(id, key);
	}

	public HashMap<String, Object> getSettings(String id) {
		HashMap<String, Object> temp = data.get(id);
		if (temp != null && !(temp.isEmpty()))
			return temp;
		else {
			temp = get(id);
			temp.put(id, temp);
			return temp;
		}
	}

	private Object getSingle(String id, String key) {
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("SELECT " + key + " FROM `" + sb.getPrefix() + name + "` WHERE `id`=? LIMIT 1;")) {
					ps.setString(1, id);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							Object o = rs.getObject(1);
							return o;
						}
					} catch (SQLException e) {
						log.error("Error when getting key: " + key + " from id: " + id + " from table: " + sb.getPrefix() + name + " in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error when preparing statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
		return null;
	}

	private HashMap<String, Object> get(String id) {
		if (backend instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) backend;
			BasicDataSource data = sb.getDataSource();
			try (Connection c = data.getConnection()) {
				try (PreparedStatement ps = c.prepareStatement("SELECT * FROM `" + sb.getPrefix() + name + "` WHERE `id`=? LIMIT 1;")) {
					ps.setString(1, id);
					try (ResultSet rs = ps.executeQuery()) {
						ResultSetMetaData md = rs.getMetaData();
						int columns = md.getColumnCount();
						HashMap<String, Object> result = new HashMap<String, Object>(columns);
						if (rs.next()) {
							for (int i = 1; i <= columns; i++) {
								result.put(md.getColumnName(i), rs.getObject(i));
							}
							return result;
						}
					} catch (SQLException e) {
						log.error("Error when getting id: " + id + " from table: " + sb.getPrefix() + name + " in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error when preparing statement in database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			} catch (SQLException e) {
				log.error("Error while accessing database: " + sb.getName());
				log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
			}
		}
		return null;
	}
	
	public void migrateBackends(Backend origin, Backend destination) {
		HashMap<String, HashMap<String, Object>> values = getBackend(origin);
		setBackend(destination, values);
	}

	private void setBackend(Backend b, HashMap<String, HashMap<String, Object>> values) {
		if (b instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) b;
			if (sb instanceof MySQLBackend) {
				MySQLBackend mb = (MySQLBackend) sb;
				try (Connection c = DriverManager.getConnection(mb.getURL(),mb.getUsername(),mb.getPassword())) {
					for (String id : values.keySet()) {
						HashMap<String, Object> tempData = values.get(id);
						for (String key : tempData.keySet()) {
							try (PreparedStatement ps = c.prepareStatement("INSERT INTO `" + sb.getPrefix() + name + "` (`id`,`" + key + "`) VALUES (?,?) ON DUPLICATE KEY UPDATE `" + key + "`=?;")) {
								Object value = tempData.get(key);
								ps.setString(1, id);
								ps.setObject(2, value);
								ps.setObject(3, value);
								try {
									ps.execute();
								} catch (SQLException e) {
									log.error("Error updating id: " + id + " from table: " + sb.getPrefix() + name + " in key: " + key + " with value: " + String.valueOf(value) + " in database: " + sb.getName());
									log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
								}
							} catch (SQLException e) {
								log.error("Error when preparing statement in database: " + sb.getName());
								log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
						}
					}
				} catch (SQLException e) {
					log.error("Error while accessing database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			}
		}
	}

	private HashMap<String, HashMap<String, Object>> getBackend(Backend b) {
		HashMap<String, HashMap<String, Object>> allData = new HashMap<String, HashMap<String, Object>>();
		if (b instanceof SQLBackend) {
			SQLBackend sb = (SQLBackend) b;
			if (sb instanceof MySQLBackend) {
				MySQLBackend mb = (MySQLBackend) sb;
				try (Connection c = DriverManager.getConnection(mb.getURL(),mb.getUsername(),mb.getPassword())) {
					try (Statement st = c.createStatement()) {
						try (ResultSet rs = st.executeQuery("SELECT * FROM `" + sb.getPrefix() + name + "`;")) {
							ResultSetMetaData md = rs.getMetaData();
							int columns = md.getColumnCount();
							while (rs.next()) {
								HashMap<String, Object> result = new HashMap<String, Object>(columns);
								String id = "";
								for (int i = 1; i <= columns; i++) {
									if (md.getColumnName(i).equalsIgnoreCase("id"))
										id = rs.getString(i);
									else
										result.put(md.getColumnName(i), rs.getObject(i));
								}
								allData.put(id, result);
							}
						} catch (SQLException e) {
							log.error("Error when getting data from table: " + sb.getPrefix() + name + " in database: " + sb.getName());
							log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
						}
					} catch (SQLException e) {
						log.error("Error when creating statement in database: " + sb.getName());
						log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
					}
				} catch (SQLException e) {
					log.error("Error while accessing database: " + sb.getName());
					log.error("SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
				}
			}
		}
		return null;
	}
	
	public void saveYaml(final YamlConfig c) {
		/**exPool.execute(new Runnable() {
			@Override
			public void run() {
				while (locks.contains(c.getConfigurationFile().getAbsolutePath()) == true) {
					try {
						locks.wait(1000);
					} catch (InterruptedException e) {}
				}
				try {
					locks.add(c.getConfigurationFile().getAbsolutePath());
					c.saveWithComments();
				} catch (IOException e) {
					log.error("Error while saving file: " + c.getConfigurationFile(), e);
				} finally {
					locks.remove(c.getConfigurationFile().getAbsolutePath());
					locks.notifyAll();
				}
			}
		});**/
	}

}
