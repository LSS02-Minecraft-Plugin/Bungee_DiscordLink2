package com.discordlink.proxydiscordlink.jda;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.md_5.bungee.api.ProxyServer;

import javax.security.auth.login.LoginException;

public class DiscordBot {

    public static Guild guild;
    private JDA jda;

    public DiscordBot(String token_bot)
    {
        if(token_bot.equals("empty"))
        {
            System.err.println("Errore durante il caricamento del bot discord. Inserisci il token");
            ProxyServer.getInstance().stop();
        }
        setup(token_bot);
        DiscordBot.guild = getJDA().getGuilds().get(0);
    }

    public JDA getJDA()
    {
        return jda;
    }

    private void setup(String token)
    {
        try
        {
            JDABuilder builder = JDABuilder.createDefault(token);
            builder.addEventListeners(new JDAListener());
            jda = builder.build().awaitReady();

            if(ProxyDiscordLink.config.getString("category-link-id").equals("empty"))
            {
                System.err.println("Errore durante la lettura della categoria di linking. Imposta la categoria per poter usare il plugin");
                ProxyServer.getInstance().stop();
            }

            if(ProxyDiscordLink.config.getString("staff-role").equals("empty"))
            {
                System.err.println("Errore durante la lettura del ruolo staff per l'accesso alle camere di linking. Impostare il valore per usare il plugin");
                ProxyServer.getInstance().stop();
            }

            String channelid = ProxyDiscordLink.config.getString("link-panel.channelid");
            if(!channelid.equals("empty"))
            {
                String messageid = ProxyDiscordLink.config.getString("link-panel.messageid");
                if(!messageid.equals("empty"))
                {
                    Guild guild = jda.getGuilds().get(0);
                    guild.getTextChannelById(channelid).addReactionById(messageid, Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.link-emoji"))).complete();
                    guild.getTextChannelById(channelid).addReactionById(messageid, Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.sync-emoji"))).complete();
                }
            }

        } catch (LoginException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
