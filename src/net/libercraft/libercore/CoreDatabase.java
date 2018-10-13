package net.libercraft.libercore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;

import net.libercraft.libercore.interfaces.Module;
import net.libercraft.libercore.managers.MessageManager.Ticket;

public class CoreDatabase extends Database {
	private static Database instance;

	public CoreDatabase() {
		super();
		instance = this;
	}

	@Override
	public List<String> getTables() {
		List<String> tables = new ArrayList<String>();
		tables.add("tickets");
		return tables;
	}
	
	@Override
    public List<String> getColumns(String table) {
    	List<String> columns = new ArrayList<String>();
    	switch (table) {
    	case "tickets":
    		columns.add("uuid TEXT NOT NULL");
    		columns.add("message TEXT NOT NULL");
    		columns.add("type TEXT NOT NULL");
    	break;
    	default:
    		return null;
    	}
    	return columns;
    }
	
	public static List<Ticket> loadTickets() {
		List<Ticket> list = new ArrayList<Ticket>();
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	
    	try {
    		conn = instance.getConnection();
    		ps = conn.prepareStatement("SELECT * FROM tickets");
    		rs = ps.executeQuery();
    		while (rs.next()) {
    			OfflinePlayer player = LiberCore.get().getServer().getOfflinePlayer(UUID.fromString(rs.getString("uuid")));
    			String message = rs.getString("message");
    			Module plugin = (Module) LiberCore.get().getServer().getPluginManager().getPlugin(rs.getString("plugin"));
    			list.add(new Ticket(player, message, plugin));
    		}
    	} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    	return list;
	}
	
	public static void saveTicket(Ticket ticket) {
		Connection conn = null;
    	PreparedStatement ps = null;
    	
    	try {
    		conn = instance.getConnection();
    		ps = conn.prepareStatement("INSERT INTO tickets(uuid,message,type) VALUES (?,?,?)");
    		
    		ps.setString(1, ticket.recipient.getUniqueId().toString());
    		ps.setString(2, ticket.message);
    		ps.setString(3, ticket.plugin.name());
    		ps.executeUpdate();
    	} catch (SQLException ex) {
        	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            	LiberCore.get().getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return; 
	}
}