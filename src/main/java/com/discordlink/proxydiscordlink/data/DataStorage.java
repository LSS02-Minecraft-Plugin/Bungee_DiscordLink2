package com.discordlink.proxydiscordlink.data;

import net.md_5.bungee.api.ProxyServer;
import org.simpleyaml.configuration.file.YamlFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DataStorage
{

    private Connection connection;
    private final YamlFile mysql;
    private String table_name;
    private String uuid_field;
    private String username_field;
    private String discordID_field;
    private String balance_field;

    public DataStorage(YamlFile mysql_data)
    {
        this.mysql = mysql_data;
        setup();
    }

    public void closeConnection()
    {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countRows(ResultSet result)
    {
        try {
            if(result.last()) return result.getRow();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public boolean exists(String discordID)
    {
        try
        {
            ResultSet result = connection.prepareStatement("SELECT "+discordID_field+" FROM "+table_name+" WHERE "+discordID_field+"=\""+discordID+"\";", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();
            if(countRows(result) == 1) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean existsName(String username)
    {
        try
        {
            ResultSet result = connection.prepareStatement("SELECT "+username_field+" FROM "+table_name+" WHERE "+username_field+"=\""+username+"\";", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();
            if(countRows(result) == 1) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean exists(UUID uuid)
    {
        try
        {
            ResultSet result = connection.prepareStatement("SELECT "+uuid_field+" FROM "+table_name+" WHERE "+uuid_field+"=\""+uuid.toString()+"\";", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();
            if(countRows(result) == 1) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public int getBalance(String discordID)
    {
        try{
            ResultSet result = connection.prepareStatement("SELECT "+discordID_field+" FROM "+table_name+" WHERE "+discordID_field+"=\""+discordID+"\";", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE).executeQuery();
            int rows = countRows(result);
            if(rows == 1)
            {
                return result.getInt(balance_field);
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public int getBalance(UUID uuid)
    {
        try{
            ResultSet result = connection.prepareStatement("SELECT "+balance_field+" FROM "+table_name+" WHERE "+uuid_field+"=\""+uuid.toString()+"\";", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE).executeQuery();
            int rows = countRows(result);
            if(rows == 1)
            {
                return result.getInt(balance_field);
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public String getDiscordID(UUID uuid)
    {
        try{
            ResultSet result = connection.prepareStatement("SELECT "+discordID_field+" FROM "+table_name+" WHERE "+uuid_field+"=\""+uuid.toString()+"\";", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE).executeQuery();
            int rows = countRows(result);
            if(rows == 1)
            {
                String ret = result.getString(discordID_field);
                if(!ret.equals("-1")) return ret;
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public String getUsername(UUID uuid)
    {
        try{
            ResultSet result = connection.prepareStatement("SELECT "+username_field+" FROM "+table_name+" WHERE "+uuid_field+"=\""+uuid.toString()+"\";", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE).executeQuery();
            int rows = countRows(result);
            if(rows == 1)
            {
                return result.getString(discordID_field);
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public UUID getUUID(String discordID)
    {
        try{
            ResultSet result = connection.prepareStatement("SELECT "+uuid_field+" FROM "+table_name+" WHERE "+discordID_field+"=\""+discordID+"\";", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE).executeQuery();
            int rows = countRows(result);
            if(rows == 1)
            {
                return UUID.fromString(result.getString(uuid_field));
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public UUID getUUIDfN(String username)
    {
        try{
            ResultSet result = connection.prepareStatement("SELECT "+uuid_field+" FROM "+table_name+" WHERE "+username_field+"=\""+username+"\";", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE).executeQuery();
            int rows = countRows(result);
            if(rows == 1)
            {
                return UUID.fromString(result.getString(uuid_field));
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void saveAllData(UUID uuid,String username,String discordID,int balance)
    {
        try{
            if(exists(uuid))
            {
                saveDiscordID(uuid,discordID);
                saveBalance(uuid,balance);
            }
            else
            {
                connection.prepareStatement("INSERT INTO "+table_name+" VALUES(\""+uuid.toString()+"\",\""+username+"\",\""+discordID+"\","+balance+");", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void saveBalance(UUID uuid, int balance)
    {
        try{
            if(exists(uuid))
            {
                connection.prepareStatement("UPDATE "+table_name+" SET "+balance_field+"=\""+ balance +"\" WHERE "+uuid_field+"=\""+uuid.toString()+"\";", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
            else
            {
                connection.prepareStatement("INSERT INTO "+table_name+" VALUES(\""+uuid.toString()+"\",\""+getUsername(uuid)+"\",\""+getDiscordID(uuid)+"\","+balance+");", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void saveDiscordID(UUID uuid, String discordID)
    {
        try{
            if(exists(uuid))
            {
                connection.prepareStatement("UPDATE "+table_name+" SET "+discordID_field+"=\""+discordID+"\" WHERE "+uuid_field+"=\""+uuid.toString()+"\";", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
            else
            {
                connection.prepareStatement("INSERT INTO "+table_name+" VALUES(\""+uuid.toString()+"\",\""+getUsername(uuid)+"\",\""+discordID+"\","+getBalance(uuid)+");", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void saveDiscordID(String username, String discordID)
    {
        try{
            if(existsName(username))
            {
                connection.prepareStatement("UPDATE "+table_name+" SET "+discordID_field+"=\""+discordID+"\" WHERE UUID=\""+getUUIDfN(username)+"\";", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
            else
            {
                connection.prepareStatement("INSERT INTO "+table_name+" VALUES(\""+getUUIDfN(username)+"\",\""+username+"\",\""+discordID+"\","+getBalance(getUUIDfN(username))+");", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeUpdate();
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void setup()
    {
        try
        {
            if(mysql.getString("password").equals("empty"))
            {
                System.err.println("Errore durante l'inizializzazione del plugin. Imposta mysql per poterlo usare o disabilita il plugin");
                ProxyServer.getInstance().stop();
            }
            String driver = "jdbc:mysql://%host%:%port%/%database%%options%";
            driver = driver.replace("%host%",mysql.getString("host"));
            driver = driver.replace("%port%",mysql.getString("port"));
            driver = driver.replace("%database%",mysql.getString("database"));
            if(!mysql.getString("options").equals(""))
            {
                driver = driver.replace("%options%","?"+mysql.getString("options"));
            }
            else
            {
                driver = driver.replace("%options%","");
            }
            connection = DriverManager.getConnection(driver,mysql.getString("user"),mysql.getString("password"));
            table_name = mysql.getString("table_name");
            uuid_field = mysql.getString("uuid_field");
            username_field = mysql.getString("username_field");
            discordID_field = mysql.getString("discordid_field");
            balance_field = mysql.getString("balance_field");
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table_name+"("+uuid_field+" VARCHAR(36) NOT NULL UNIQUE,"+discordID_field+" VARCHAR(20) UNIQUE,"+balance_field+" Integer);").execute();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}
