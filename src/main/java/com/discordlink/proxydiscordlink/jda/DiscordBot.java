package com.discordlink.proxydiscordlink.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
            jda.awaitReady();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
