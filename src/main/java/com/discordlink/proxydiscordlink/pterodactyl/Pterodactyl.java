package com.discordlink.proxydiscordlink.pterodactyl;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import com.stanjg.ptero4j.PteroUserAPI;
import com.stanjg.ptero4j.controllers.user.UserServersController;
import net.md_5.bungee.api.ProxyServer;

import java.util.UUID;

public class Pterodactyl
{

    private final UserServersController controller;

    public Pterodactyl(String url,String apikey)
    {
        if(url.equals("empty"))
        {
            System.err.println("Errore durante la lettura dell'url del tuo pannello pterodactyl. Inserisci qui il tuo url per usare il plugin");
            ProxyServer.getInstance().stop();
        }
        if(apikey.equals("empty"))
        {
            System.err.println("Errore durante la lettura della client api key. Inseriscilo per usare il plugin");
            ProxyServer.getInstance().stop();
        }
        controller = new PteroUserAPI(url,apikey).getServersController();
    }

    public void executeCommand(String server_id,String cmd,String name,String group)
    {
        if(cmd.contains("%player%")) cmd = cmd.replace("%player%",name);
        if(cmd.contains("%group%") && (group != null)) cmd = cmd.replace("%group%",group);
        controller.getServer(server_id).sendCommand(cmd);
    }

    public void executeCommands(UUID uuid,String group,ExecCmdType type)
    {
        if(!ProxyDiscordLink.config.contains("servers")) return;
        if(group == null) return;
        String name = ProxyDiscordLink.data.getUsername(uuid);
        for(String server_id : ProxyDiscordLink.config.getStringList("servers"))
        {
            if(type == ExecCmdType.LINK)
            {
                if(ProxyDiscordLink.config.getStringList("link-commands").isEmpty()) continue;
                for(String cmd : ProxyDiscordLink.config.getStringList("link-commands"))
                {
                    executeCommand(server_id,cmd,name,null);
                }
            }
            if(type == ExecCmdType.UNLINK)
            {
                if(ProxyDiscordLink.config.getStringList("unlink-commands").isEmpty()) continue;
                for(String cmd : ProxyDiscordLink.config.getStringList("unlink-commands"))
                {
                    executeCommand(server_id,cmd,name,null);
                }
            }
            if(type == ExecCmdType.ADD)
            {
                String cmd = ProxyDiscordLink.config.getString("addGroupCommand");
                executeCommand(server_id,cmd,name,group);
            }
            if(type == ExecCmdType.REMOVE)
            {
                String cmd = ProxyDiscordLink.config.getString("removeGroupCommand");
                executeCommand(server_id,cmd,name,group);
            }
        }
    }

}
