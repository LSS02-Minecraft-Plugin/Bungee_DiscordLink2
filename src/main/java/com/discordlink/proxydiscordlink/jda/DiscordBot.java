package com.discordlink.proxydiscordlink.jda;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.md_5.bungee.api.ProxyServer;

import javax.security.auth.login.LoginException;

public class DiscordBot {

    private JDA jda;

    public DiscordBot(String token_bot)
    {
        if(token_bot.equals("empty"))
        {
            System.err.println("Errore durante il caricamento del bot discord. Inserisci il token");
            ProxyServer.getInstance().stop();
        }
        setup(token_bot);
    }

    public JDA getJDA()
    {
        return jda;
    }

    private void setup(String token)
    {
        try
        {
            jda = JDABuilder.createDefault(token).build();
            jda.addEventListener(new JDAListener());
            jda.awaitReady();

            String channelid = ProxyDiscordLink.config.getString("link-panel.channelid");
            if(!channelid.equals("empty"))
            {
                String messageid = ProxyDiscordLink.config.getString("link-panel.messageid");
                if(!messageid.equals("empty"))
                {
                    Guild guild = jda.getGuilds().get(0);
                    guild.getTextChannelById(channelid).addReactionById(messageid, Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.link-emoji")));
                    guild.getTextChannelById(channelid).addReactionById(messageid, Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.sync-emoji")));
                }
            }

        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
