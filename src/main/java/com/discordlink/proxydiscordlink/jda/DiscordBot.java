package com.discordlink.proxydiscordlink.jda;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
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
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            jda = builder.build();
            jda.awaitReady();
            DiscordBot.guild = getJDA().getGuilds().get(0);

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
                    DiscordBot.guild.getTextChannelById(channelid).addReactionById(messageid, Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.link-emoji"))).queue();
                    DiscordBot.guild.getTextChannelById(channelid).addReactionById(messageid, Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.sync-emoji"))).queue();
                }
            }

            DiscordBot.guild.upsertCommand("reiatsu","Mostra i reiatsu posseduti").queue();
            DiscordBot.guild.upsertCommand("areiatsu","Administrator commands")
                    .addOption(OptionType.STRING,"action","<add | remove | set >",true)
                    .addOption(OptionType.STRING,"userid","Id dell'utente su cui operare",true)
                    .addOption(OptionType.INTEGER,"amount","Numero di reiatsu",true)
                    .queue();
            DiscordBot.guild.upsertCommand("showreiatsu","Mostra i reiatsu di un utente")
                    .addOption(OptionType.STRING,"userid","Utente",true)
                    .queue();

        } catch (LoginException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
