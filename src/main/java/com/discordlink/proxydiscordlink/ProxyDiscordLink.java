package com.discordlink.proxydiscordlink;

import com.discordlink.proxydiscordlink.data.DataStorage;
import com.discordlink.proxydiscordlink.jda.DiscordBot;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public final class ProxyDiscordLink extends Plugin {

    public static HashMap<String,String> linkingRoles;
    public static DataStorage data;
    public static DiscordBot bot;
    private YamlFile config;

    public YamlFile loadConfiguration(String name)
    {
        return loadConfiguration(getDataFolder(),name);
    }

    public YamlFile loadConfiguration(File folder,String name)
    {
        YamlFile yaml = null;
        try
        {
            if(!folder.exists()) folder.mkdir();
            File file = new File(folder,name);
            if(!file.exists()) Files.copy(getResourceAsStream(name),file.toPath());
            yaml = new YamlFile(file);
            yaml.loadWithComments();
            return yaml;
        }catch(IOException | InvalidConfigurationException e)
        {
            System.err.println("Errore durante il caricamento della configurazione " + name);
        }
        return yaml;
    }

    @Override
    public void onEnable()
    {
        ProxyDiscordLink.data = new DataStorage(loadConfiguration("mysql.yml"));
        config = loadConfiguration("config.yml");
        ProxyDiscordLink.bot = new DiscordBot(config.getString("token-bot"));
        ProxyDiscordLink.linkingRoles = new HashMap<>();
        if(config.contains("roles"))
        {
            for(String key : config.getConfigurationSection("roles").getKeys(false))
            {
                ProxyDiscordLink.linkingRoles.put(key,config.getString("roles."+key));
            }
        }
    }

    @Override
    public void onDisable()
    {
        data.closeConnection();
    }
}
