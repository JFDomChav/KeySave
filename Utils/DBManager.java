package com.JFDomChav.KeySave.Utils;    

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

public class DBManager {
    private final String DB_NAME = "KeySave";
    private final String DB_FOLDER = "Passwords";
    
    // Function to check if a previously created database exists, if not then create it
    public boolean startDB(){
        // Verify if exists a previous data base
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table';";
        String url = System.getProperty("user.dir")+"/DatabaseStructure.json";
        try(
            ResultSet res = this.select(sql);
            InputStream is = new FileInputStream(url);
            JsonReader reader = Json.createReader(is);
        ){
            // If not exists, then create one
            if((res == null) || (!res.next())){
                // Open the DB structure JSON
                JsonObject obj = reader.readObject();
                JsonArray tablesArray = obj.getJsonArray("Tables");
                String sqlCreate = "";
                // Iterate table by table
                for (JsonObject table: tablesArray.getValuesAs(JsonObject.class)) {
                    String tableName = table.getString("Name");
                    sqlCreate += "CREATE TABLE IF NOT EXISTS "+tableName+"(";
                    // Iterate column by column
                    JsonArray columns = table.getJsonArray("Columns");
                    for (JsonObject column: columns.getValuesAs(JsonObject.class)) {
                        String columnName = column.getString("Name");
                        sqlCreate += columnName+" "+column.getString("Type");
                        // Verify if length and options are null, if not add values
                        if(column.get("Length") != JsonValue.NULL){
                            sqlCreate += "("+column.getString("Length")+")";
                        }
                        if(column.get("Options") != JsonValue.NULL){
                            sqlCreate += " "+column.getString("Options")+",";
                        }else{
                            sqlCreate += ",";
                        }
                    }
                    // Add primary and foreign keys
                    JsonArray PKS = table.getJsonArray("Primary keys");
                    // Iterate PK's array
                    for(JsonString PK: PKS.getValuesAs(JsonString.class)){
                        String value = PK.getString();
                        sqlCreate += "PRIMARY KEY("+value+"),";
                    }
                    JsonArray FKS = table.getJsonArray("Foreign keys");
                    // Iterate FK's array
                    for(JsonObject FK: FKS.getValuesAs(JsonObject.class)){
                        sqlCreate += "FOREIGN KEY("+FK.getString("Name")+
                                ") REFERENCES "+FK.getString("Table referenced")
                                +"("+FK.getString("Name referenced")+"),";
                    }
                    // Delete the last coma and finish the create table instruction;
                    sqlCreate = sqlCreate.substring(0, sqlCreate.length()-1) + ");\n";
                }
                // Execute the statement
            }
        }catch(SQLException e){
            // Manage the SQL exception
            return false;
        } catch (IOException ex) {
            // Manage the file exception
            return false;
        }
        return true;
    }
    
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