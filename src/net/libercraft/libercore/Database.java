package net.libercraft.libercore;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Database {
	public JavaPlugin plugin;
    public abstract List<String> getTables();
    public abstract List<String> getColumns(String table);
    
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;
    
    private int state = 0; // 0 = closed, 1 = open, 2 = closing
    
    public static String datadb(JavaPlugin plugin) {
    	return plugin.getConfig().getString("SQLite.Filename", "data");
    }
    
    public Database() {
    	this.plugin = LiberCore.instance;
    	load();
    }
    
    public void open(Database instance, String sql) {
    	open(instance, sql, null);
    }
    
    public void open(Database instance, String sql, List<Object> values) {
    	try {
    		
    		// If connection is closed
    		if (state == 0 || conn.isClosed()) {
	    		conn = instance.getConnection();
	    		state = 1;
    		}
			
			rs = null;
    		
    		// Prepare statement
    		ps = conn.prepareStatement(sql);
    		
    		// Execute statement
    		if (sql.startsWith("SELECT"))
    			rs = ps.executeQuery();
    		else {
    			if (values != null)
	    			for (int i = 0; i < values.size(); i++)
	    				put(i, values.get(i));
    			ps.executeUpdate();
    		}
    		
    	} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
    	}
    }
    
    public void close() {
    	state = 2;
    	
    	new BukkitRunnable() {
    		@Override
    		public void run() {
    			if (state == 2)
	    			try {
	    	            if (ps != null)
	    	                ps.close();
	    	            if (conn != null)
	    	                conn.close();
	        			state = 0;
	    	        } catch (SQLException ex) {
	    	        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
	    	        }
    		}
    	}.runTaskLater(LiberCore.get(), 10);
    }
    
    public void insert(Database instance, String table, List<String> columns, List<Object> values) {
    	int length = columns.size();
    	String sql = "INSERT INTO "+table+"(";
    	
    	if (length != values.size())
    		return;
    	
    	// Build SQL statement with columns and ?'s for values
    	for (String c:columns)
    		sql += c + ", ";
    	sql = sql.substring(0, sql.length() - 2);
    	sql += ") VALUES(";
    	for (int i = 0; i < length; i++)
    		sql += "?, ";
    	sql = sql.substring(0, sql.length() - 2);
    	sql += ")";
    	
    	open(instance, sql, values);
		close();
    }
    
    public void update(Database instance, String table, String replaceColumn, Object replaceValue, String conditionColumn, Object conditionValue) {
		String sql = "UPDATE "+table+" SET "+replaceColumn+" = ? WHERE "+conditionColumn+" = ?";
		open(instance, sql, Arrays.asList(new Object[] {replaceValue, conditionValue}));
		close();
    }
    
    public void replace(Database instance, String table, List<String> columns, List<Object> values) {
    	int length = columns.size();
    	String sql = "REPLACE INTO "+table+"(";
    	
    	if (length != values.size())
    		return;
    	
    	// Build SQL statement with columns and ?'s for values
    	for (String c:columns)
    		sql += c + ", ";
    	sql = sql.substring(0, sql.length() - 2);
    	sql += ") VALUES(";
    	for (int i = 0; i < length; i++)
    		sql += "?, ";
    	sql = sql.substring(0, sql.length() - 2);
    	sql += ")";
    	
    	open(instance, sql, values);
		close();
    }
    
    public void delete(Database instance, String table, String column, Object value) {
    	String sql = "DELETE FROM "+table+" WHERE "+column+" = "+value.toString();
    	open(instance, sql);
    	close();
    }

    // get data from specific column, based on key/value condition
    public Object getFromKey(Database instance, String table, String key, Object value, String returnColumn) {
    	String sql = "SELECT * FROM "+table+" WHERE "+key+" = '"+value.toString()+"'";
    	Object result = null;
    	
    	open(instance, sql);
    	try {
    		if (rs == null)
    			return null;
			while (rs.next()) {
    			result = rs.getString(returnColumn);
			}
		} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
    	close();
    	
    	return result;
    }
    
    // get data from last column that passes all conditions
    public Object getFromCondition(Database instance, String table, String returnColumn, Condition... conditions) {
    	String sql = "SELECT * FROM "+table;
    	Object result = null;
    	
    	open(instance, sql);
    	try {
    		if (rs == null)
    			return null;
    		results:
				while (rs.next()) {
					if (conditions != null)
		    			for (Condition c:conditions) 
							if (!passCondition(c, rs))
								continue results;
	    				
	    			result = rs.getString(returnColumn);
				}
		} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
    	close();
    	
    	return result;
    }
    
    public List<Object> getFromConditions(Database instance, String table, List<Condition> conditions, int conditionsHit, String... columns) {
		String sql = "SELECT * FROM "+table;
    	List<Object> list = new ArrayList<>();
		
		open(instance, sql);
		try {
    		if (rs == null)
    			return null;
			while (rs.next()) {
				int hits = 0;
				if (conditions != null)
					for (Condition con:conditions) {
						if (passCondition(con, rs))
							hits++;
						if (hits >= conditionsHit)
							for (String col:columns)
								list.add(rs.getObject(col));
					}
			}
		} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		close();
    	return list;
    }
    
    // get all data that passes all conditions
    public List<Object> getList(Database instance, String table, String column, Condition... conditions) {
    	String sql = "SELECT * FROM "+table;
    	List<Object> list = new ArrayList<>();
    	
    	open(instance, sql);
    	try {
    		if (rs == null)
    			return null;
    		results:
	    		while (rs.next()) {
	    			if (conditions != null)
		    			for (Condition c:conditions) {
		    				if (!passCondition(c, rs))
		    					continue results;
		    			}
	    			
	    			list.add(rs.getObject(column));
	    		}
    	} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } 
    	close();
    	
    	return list;
    }
    
    
    public List<Object> getList(Database instance, String table, String column) {
    	return getList(instance, table, column, null, null, null);
    }
    
    public List<Object> getRandom(Database instance, String table, String column, int limit) {
		String sql = "SELECT * FROM "+table+" \r\n" +	"ORDER BY RANDOM() LIMIT "+limit+"\r\n";
    	List<Object> result = new ArrayList<>();
    	
    	open(instance, sql);
    	try {
    		if (rs == null)
    			return null;
			while (rs.next()) {
    			result.add(rs.getString(column));
			}
		} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
    	close();
    	
    	return result;
    }
    
    public List<Object> getOrder(Database instance, String table, String column, String orderColumn, String direction, int limit) {
    	String sql = "SELECT * FROM "+table+" ORDER BY "+orderColumn+" "+direction+" LIMIT "+limit;
    	List<Object> result = new ArrayList<>();
    	
    	open(instance, sql);
    	try {
    		while (rs.next()) {
    			result.add(rs.getString(column));
    		}
    	} catch (SQLException ex) {
    		
    	}
    	close();
    	
    	return result;
    }
    
    // check if result passes list of conditions
    private boolean passCondition(Condition c, ResultSet rs) {
    	if (c == null)
    		return true;
    	
    	Class<? extends Object> type = c.value.getClass();
    	
    	try {
    		// Make sure result is not null
        	if (type.equals(String.class))
        		if (rs.getString(c.column) == null)
        			return false;
        	
    		if (c.condition.equals("=")) {
	    		if (type.equals(String.class))
	    			if (rs.getString(c.column).equals((String)c.value))
	    				return true;
	    		
	    		// if statements for other types
	    		
	    		// condition not met
	    		return false;
	    	} else if (c.condition.equals("<")) {
	    		if (type.equals(Integer.class))
	    			if (rs.getInt(c.column) < ((Integer)c.value))
	    				return true;
	    		
	    		// if statements for other types
	    		
	    		// condition not met
	    		return false;
	    	} else if (c.condition.equals(">")) {
	    		if (type.equals(Integer.class))
	    			if (rs.getInt(c.column) > ((Integer)c.value))
	    				return true;
	    		
	    		// if statements for other types
	    		
	    		// condition not met
	    		return false;
	    	} else if (c.condition.equals("=!^")) {
	    		if (type.equals(String.class))
	    			if (rs.getString(c.column).equalsIgnoreCase((String)c.value))
	    				return true;
	    		return false;
	    	}
    	} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
    	}
    	
    	return false;
    }
    
    private void put(int i, Object value) {
		try {
			if (value == null)
				ps.setString((i+1), null);
			else if (value.getClass().equals(String.class))
				ps.setString((i+1), (String)value);
			else if (value.getClass().equals(Integer.class))
				ps.setInt((i+1), (Integer)value);
			else if (value.getClass().equals(Long.class))
				ps.setLong((i+1), (Long)value);
			else if (value.getClass().equals(Float.class))
				ps.setFloat((i+1), (Float)value);
			else if (value.getClass().equals(Boolean.class))
				ps.setBoolean((i+1), (Boolean)value);
			else
				System.out.println("Value type not supported by Database yet. Please update put() method in Database.java");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void load() {
        for (String t:getTables()) {
	        try {
	            Connection c = getConnection();
	            Statement s = c.createStatement();
	            s.executeUpdate("CREATE TABLE IF NOT EXISTS "+t+"("+getColumns(t).get(0)+");");
	            s.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        updateTable(plugin, t);
        }
    }
    
    public Connection getConnection() {
    	File pluginFolder = plugin.getDataFolder();
    	if (!pluginFolder.exists())
    		pluginFolder.mkdirs();
    	
    	File dataFile = new File(pluginFolder, datadb(plugin) + ".db");
    	if (!dataFile.exists())
    		try {
    			dataFile.createNewFile();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	
    	try {
    		Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
    		return connection;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static class Error {
        public static void execute(JavaPlugin plugin, Exception ex){
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }
        public static void close(JavaPlugin plugin, Exception ex){
            plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
        }
    }
    
    public static class Errors {
        public static String sqlConnectionExecute(){
            return "Couldn't execute MySQL statement: ";
        }
        public static String sqlConnectionClose(){
            return "Failed to close MySQL connection: ";
        }
        public static String noSQLConnection(){
            return "Unable to retreive MYSQL connection: ";
        }
        public static String noTableFound(){
            return "Database Error: No Table Found";
        }
    }
    
    public void updateTable(JavaPlugin plugin, String table) {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
		List<String> oldTableColumns = new ArrayList<String>();
    	
    	try {
    		conn = getConnection();
    		ps = conn.prepareStatement("PRAGMA table_info("+table+")");
    		rs = ps.executeQuery();
    		while (rs.next()) {
    			oldTableColumns.add(rs.getString("name"));
    		}
    	} catch (SQLException ex) {
        	plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            	plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    	
    	try {
    		conn = getConnection();
    		ps = conn.prepareStatement("ALTER TABLE "+table+" RENAME TO TempOldTable;");
    		ps.executeUpdate();
    		ps = conn.prepareStatement(getCreateTableString(table));
    		ps.executeUpdate();
    		ps = conn.prepareStatement(getRepopulateTableString(table, oldTableColumns));
    		ps.executeUpdate();
    		ps = conn.prepareStatement("DROP TABLE TempOldTable;");
    		ps.executeUpdate();
    	} catch (SQLException ex) {
        	plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
    	} finally {
    		try {
    			if (ps != null)
    				ps.close();
    			if (conn != null)
    				conn.close();
    		} catch (SQLException ex) {
            	plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
    		}
    	}
    }
    
    public String getCreateTableString(String table) {
    	String string = "CREATE TABLE IF NOT EXISTS " + table + "(";
    	for (String column:getColumns(table)) {
    		string += column + ",";
    	}
    	return string.substring(0, string.length() - 1) + ");";
    }
    
    public String getRepopulateTableString(String table, List<String> oldTableColumns) {
    	Map<String, String> renamedColumns = new HashMap<String, String>();
    	renamedColumns.put("beacon", "fountain");
    	
    	String string = "INSERT INTO "+table+"(";
    	String string2 = "SELECT ";
    	
    	for (String c:oldTableColumns) {
    		String name = c;
    		if (renamedColumns.containsKey(c))
    			name = renamedColumns.get(c);

    		string += name + ",";
    		string2 += c + ",";
    	}
    	
    	if (string.length() > 1)
    		string = string.substring(0, string.length() - 1) + ") ";
    	if (string2.length() > 1)
    		string2 = string2.substring(0, string2.length() - 1) + " from TempOldTable;";
    	return string + string2;
    }
    
    public static class Condition {
    	String column;
    	String condition;
    	Object value;
    	public Condition(String column, String condition, Object value) {
    		this.column = column;
    		this.condition = condition;
    		this.value = value;
    	}
    }
}
