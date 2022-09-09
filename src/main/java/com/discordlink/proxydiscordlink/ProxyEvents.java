package com.discordlink.proxydiscordlink;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import com.discordlink.proxydiscordlink.jda.DiscordBot;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class ProxyEvents implements Listener
{

    @EventHandler
    public void onPostLogin(PostLoginEvent event)
    {
        ProxiedPlayer pp = event.getPlayer();
        if(!ProxyDiscordLink.data.exists(pp.getUniqueId()))
        {
            ProxyDiscordLink.data.saveAllData(pp.getUniqueId(),pp.getName(),"-1",0);
            return;
        }

        ProxyServer.getInstance().getScheduler().schedule(ProxyDiscordLink.getInstance(),()->{
            if(!ProxyDiscordLink.data.getDiscordID(pp.getUniqueId()).equals("-1"))
            {
                ProxyDiscordLink.sync(pp.getUniqueId());
            }
        },15, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onChat(ChatEvent event)
    {
        if(!(event.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer pp = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        if(message.startsWith("/")) return;
        if(ProxyDiscordLink.config.contains("chats."+pp.getServer().getInfo().getName()))
        {
            String id = ProxyDiscordLink.config.getString("chats."+pp.getServer().getInfo().getName());
            DiscordBot.guild.getTextChannelById(id).sendMessage("["+pp.getServer().getInfo().getName()+"] "+pp.getName()+" » " + message).queue();
        }
    }

}
