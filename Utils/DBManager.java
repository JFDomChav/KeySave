package com.JFDomChav.KeySave.Utils;    

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class DBManager {
    private final String DB_NAME = "KeySave";
    private final String DB_FOLDER = "Passwords";
    
    // Function to create a DB connection
    private Connection getConnection() throws SQLException{
        // Get path
        String path = System.getProperty("user.dir");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:"+path+"/"+this.DB_FOLDER+"/"+this.DB_NAME+".db");
        return conn;
    }
    
    // Function to executeUpdate a insert into DB
    /*
    table: name of the thable in DB
    args: HashMap that attempts to resemble an associative array.
          Its keys are the name of table columns and will have their
          value associated with them.
    RETURNS:
    0: all OK
    -1: error
    */
    public int insert(String table, Map<String, String> args){
        // Build the SQL code with the data obtained
        String sql = "INSERT INTO "+table+"( ";
        // Variable to store the values, taking advantage of the foreach
        String values = "";
        // Add the columns stored in args
        String[] affectedColumns = new String[args.size()];
        int i=0;
        for (Map.Entry<String, String> entry : args.entrySet()){
            sql += entry.getKey()+",";
            values += entry.getValue()+",";
            affectedColumns[i]=entry.getKey();
            i++;
        }
        // Delete the last coma and put a parenthesis
        sql = sql.substring(0, sql.length()-1)+") VALUES (";
        values = values.substring(0, values.length()-1)+")";
        // Concatenate to end the SQL expression
        sql = sql+values;
        // Insert into databse
        try(
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
        ){
            stmt.executeUpdate(sql);
        }catch(SQLException e){
            // Manage exceptions
            return -1;
        }
        return 0;
    }
    // Function to update into DB
    public int update(String table, Map<String,String> args, String condition){
         // Build the SQL code with the data obtained
        String sql = "UPDATE "+table+" SET ";
        // Add the update values stored in args
        String[] affectedColumns = new String[args.size()];
        int i=0;
        for (Map.Entry<String, String> entry : args.entrySet()){
            sql += entry.getKey()+" = "+entry.getValue()+",";
            affectedColumns[i] = entry.getKey();
            i++;
        }
        // Delete the last coma and add the condition
        sql = sql.substring(0, sql.length()-1)+" WHERE "+condition;
        // Insert into databse
        try(
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
        ){
            stmt.executeUpdate(sql, affectedColumns);
        }catch(SQLException e){
            // Manage exceptions
            return -1;
        }
        return 0;
    }
    // Function to delete into DB
    public int delete(String table, String condition){
        // Build SQL
        String sql = "DELETE FROM "+table+" WHERE "+condition;
        // Insert into databse
        try(
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
        ){
            stmt.execute(sql);
        }catch(SQLException e){
            // Manage exceptions
            return -1;
        }
        return 0;
    }
    // Functio to select into DB
    /*
    sql: SQL query code
    */
    public ResultSet select(String sql){
        try(
            Connection conn = this.getConnection();
            Statement stmt = conn.createStatement();
        ){
            return stmt.executeQuery(sql);
        }catch(SQLException e){
            // Manage exceptions
            return null;
        }
    }
}