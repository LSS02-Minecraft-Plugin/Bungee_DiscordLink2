package com.discordlink.proxydiscordlink;

import com.discordlink.proxydiscordlink.data.DataStorage;
import com.discordlink.proxydiscordlink.jda.DiscordBot;
import com.discordlink.proxydiscordlink.pterodactyl.Pterodactyl;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public final class ProxyDiscordLink extends Plugin {

    public static HashMap<String,String> linkingRoles;
    public static Pterodactyl ptero;
    public static DataStorage data;
    public static DiscordBot bot;
    public static YamlFile config;

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
        ProxyDiscordLink.config = loadConfiguration("config.yml");
        ProxyDiscordLink.bot = new DiscordBot(ProxyDiscordLink.config.getString("token-bot"));
        ProxyDiscordLink.ptero = new Pterodactyl(ProxyDiscordLink.config.getString("ptero-url"),ProxyDiscordLink.config.getString("ptero-api-key"));
        ProxyDiscordLink.linkingRoles = new HashMap<>();
        if(ProxyDiscordLink.config.contains("roles"))
        {
            for(String key : ProxyDiscordLink.config.getConfigurationSection("roles").getKeys(false))
            {
                ProxyDiscordLink.linkingRoles.put(key,ProxyDiscordLink.config.getString("roles."+key));
            }
        }
    }

    @Override
    public void onDisable()
    {
        ProxyDiscordLink.data.closeConnection();
    }
}
