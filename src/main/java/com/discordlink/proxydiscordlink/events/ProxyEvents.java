package com.discordlink.proxydiscordlink.events;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import com.discordlink.proxydiscordlink.jda.DiscordBot;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ProxyEvents implements Listener
{

    @EventHandler
    public void onPostLogin(PostLoginEvent event)
    {
        ProxiedPlayer pp = event.getPlayer();
        if(!ProxyDiscordLink.data.exists(pp.getUniqueId()))
        {
            ProxyDiscordLink.data.saveAllData(pp.getUniqueId(),pp.getName(),"-1",0);
        }
        if(!ProxyDiscordLink.data.getDiscordID(pp.getUniqueId()).equals("-1"))
        {
            ProxyServer.getInstance().getScheduler().runAsync(ProxyDiscordLink.getInstance(),()-> ProxyDiscordLink.sync(pp.getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(ChatEvent event)
    {
        if(!(event.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer pp = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        if(ProxyDiscordLink.config.contains("chats."+pp.getServer().getInfo().getName()))
        {
            String id = ProxyDiscordLink.config.getString("chats."+pp.getServer().getInfo().getName());
            DiscordBot.guild.getTextChannelById(id).sendMessage("["+pp.getServer().getInfo().getName()+"] "+pp.getName()+" » " + message).complete();
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText("§f[§bDiscord§f] "+pp.getName() + " §7»§f " + message));
            event.setCancelled(true);
        }
    }

}
