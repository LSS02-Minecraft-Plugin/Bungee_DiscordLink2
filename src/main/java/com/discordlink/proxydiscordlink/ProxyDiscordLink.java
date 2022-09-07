package com.discordlink.proxydiscordlink;

import com.discordlink.proxydiscordlink.data.DataStorage;
import com.discordlink.proxydiscordlink.events.ProxyEvents;
import com.discordlink.proxydiscordlink.jda.DiscordBot;
import com.discordlink.proxydiscordlink.pterodactyl.ExecCmdType;
import com.discordlink.proxydiscordlink.pterodactyl.Pterodactyl;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

public final class ProxyDiscordLink extends Plugin {

    public static HashMap<String,String> linkingRoles;
    public static HashMap<String,String> linkChannel;
    private static ProxyDiscordLink instance;
    public static Pterodactyl ptero;
    public static DataStorage data;
    public static DiscordBot bot;
    public static YamlFile config;

    public static ProxyDiscordLink getInstance()
    {
        return ProxyDiscordLink.instance;
    }

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
        ProxyDiscordLink.instance = this;
        ProxyDiscordLink.linkingRoles = new HashMap<>();
        ProxyDiscordLink.linkChannel = new HashMap<>();
        ProxyDiscordLink.data = new DataStorage(loadConfiguration("mysql.yml"));
        ProxyDiscordLink.config = loadConfiguration("config.yml");
        ProxyDiscordLink.bot = new DiscordBot(ProxyDiscordLink.config.getString("token-bot"));
        ProxyDiscordLink.ptero = new Pterodactyl(ProxyDiscordLink.config.getString("ptero-url"),ProxyDiscordLink.config.getString("ptero-api-key"));
        if(ProxyDiscordLink.config.contains("roles"))
        {
            for(String key : ProxyDiscordLink.config.getConfigurationSection("roles").getKeys(false))
            {
                ProxyDiscordLink.linkingRoles.put(key,ProxyDiscordLink.config.getString("roles."+key));
            }
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this,new ProxyEvents());
    }

    @Override
    public void onDisable()
    {
        if(ProxyDiscordLink.data != null) ProxyDiscordLink.data.closeConnection();
        if(ProxyDiscordLink.bot != null) ProxyDiscordLink.bot.getJDA().shutdownNow();
    }

    public static void sync(UUID uuid)
    {
        if(ProxyDiscordLink.data.getDiscordID(uuid).equals("-1")) return;
        ProxyServer.getInstance().getScheduler().runAsync(ProxyDiscordLink.getInstance(),() -> {
            Member member = DiscordBot.guild.getMemberById(ProxyDiscordLink.data.getDiscordID(uuid));
            for(String rolename : ProxyDiscordLink.linkingRoles.keySet())
            {
                if(member.getRoles().contains(DiscordBot.guild.getRoleById(ProxyDiscordLink.linkingRoles.get(rolename))))
                {
                    ProxyDiscordLink.ptero.executeCommands(uuid,rolename, ExecCmdType.ADD);
                }
            }
        });
    }
}
