package com.discordlink.proxydiscordlink.jda;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import com.discordlink.proxydiscordlink.pterodactyl.ExecCmdType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.EnumSet;

public class JDAListener extends ListenerAdapter
{
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event)
    {
        Member member = event.getMember();
        MessageReaction reaction = event.getReaction();

        if(member == null) return;
        if(member.getUser().isBot()) return;

        if(reaction.getEmoji().equals(Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.link-emoji"))))
        {
            if(ProxyDiscordLink.data.exists(member.getId())) return;
            Category cat = DiscordBot.guild.getCategoryById(ProxyDiscordLink.config.getString("category-link-id"));
            TextChannel textChannel = cat.createTextChannel(member.getEffectiveName())
                    .addPermissionOverride(DiscordBot.guild.getPublicRole(),null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(member,EnumSet.of(Permission.VIEW_CHANNEL),null)
                    .addPermissionOverride(DiscordBot.guild.getRoleById(ProxyDiscordLink.config.getString("staff-role")), EnumSet.of(Permission.VIEW_CHANNEL),null)
                    .complete();
            String message = ProxyDiscordLink.config.getString("link-player-message");
            if(message.contains("%mention%")) message = message.replace("%mention%","<@"+member.getId()+">");
            textChannel.sendMessage(message).complete();
            ProxyDiscordLink.linkChannel.put(member.getId(), textChannel.getId());
        }

        if(reaction.getEmoji().equals(Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.sync-emoji"))))
        {
            if(!ProxyDiscordLink.data.exists(member.getId()))
            {
                reaction.removeReaction(member.getUser()).complete();
                return;
            }
            ProxyDiscordLink.sync(ProxyDiscordLink.data.getUUID(member.getId()));

        }
        reaction.removeReaction(member.getUser()).complete();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Member member = event.getMember();

        if(member == null) return;
        if(member.getUser().isBot()) return;

        if(event.getMessage().getCategory().getId().equals(ProxyDiscordLink.config.getString("category-link-id")))
        {
            if(ProxyDiscordLink.linkChannel.containsKey(member.getId()))
            {
                TextChannel textChannel = event.getGuild().getTextChannelById(ProxyDiscordLink.linkChannel.get(member.getId()));
                String message = event.getMessage().getContentStripped();
                if(!ProxyDiscordLink.data.existsName(message))
                {
                    textChannel.sendMessage(ProxyDiscordLink.config.getString("invalid-username")).complete();
                    return;
                }
                ProxyDiscordLink.data.saveDiscordID(message,member.getId());
                if(!ProxyDiscordLink.config.getString("link-role-id").equals("empty"))
                {
                    Role role = DiscordBot.guild.getRoleById(ProxyDiscordLink.config.getString("link-role-id"));
                    DiscordBot.guild.addRoleToMember(member,role).queue();
                }
                ProxyDiscordLink.ptero.executeCommands(ProxyDiscordLink.data.getUUID(member.getId()),null, ExecCmdType.LINK);
                ProxyDiscordLink.sync(ProxyDiscordLink.data.getUUID(member.getId()));
                ProxyDiscordLink.linkChannel.remove(member.getId(),textChannel.getId());
                textChannel.delete().queue();
            }
            return;
        }

        if(ProxyDiscordLink.data.exists(member.getId()))
        {
            if(ProxyDiscordLink.config.contains("chats"))
            {
                String id = "";
                for(String key : ProxyDiscordLink.config.getConfigurationSection("chats").getKeys(false))
                {
                    if(event.getMessage().getChannel().getId().equals(ProxyDiscordLink.config.getString("chats."+key))
                    && (!id.equals(ProxyDiscordLink.config.getString("chats."+key))))
                    {
                        ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText("§8[§bDiscord§8]§f "+ ProxyDiscordLink.data.getUsername(ProxyDiscordLink.data.getUUID(member.getId())) + " §7»§f " + event.getMessage().getContentRaw()));
                    }
                    id = event.getMessage().getChannel().getId();
                }
            }
        }

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        Member member = event.getMember();
        if(member == null) return;
        
        
        if(event.getName().equals("reiatsu"))
        {
            if(member.getUser().isBot()) return;
            int balance = ProxyDiscordLink.data.getBalance(member.getId());
            String message = ProxyDiscordLink.config.getString("discord-reiatsu");
            if(message.contains("%amount%")) message = message.replace("%amount%",String.valueOf(balance));
            event.deferReply(true).setContent(message).queue();
        }

        if(event.getName().equals("showreiatsu"))
        {
            if(!member.hasPermission(Permission.ADMINISTRATOR))
            {
                event.deferReply(true).setContent(ProxyDiscordLink.config.getString("discord-no-permission")).queue();
                return;
            }
            if(member.getUser().isBot()) return;
            String id = event.getOption("userid").getAsString();
            if(!ProxyDiscordLink.data.exists(id))
            {
                event.deferReply(true).setContent(ProxyDiscordLink.config.getString("discord-user-not-found")).queue();
                return;
            }
            event.deferReply(true).setContent(replacePlaceholder(ProxyDiscordLink.config.getString("other-discord-reiatsu"),id,ProxyDiscordLink.data.getBalance(id))).queue();
        }

        if(event.getName().equals("areiatsu"))
        {
            if(!member.hasPermission(Permission.ADMINISTRATOR))
            {
                event.deferReply(true).setContent(ProxyDiscordLink.config.getString("discord-no-permission")).queue();
                return;
            }

            String action = event.getOption("action").getAsString();
            String id = event.getOption("userid").getAsString();

            if(!ProxyDiscordLink.data.exists(id))
            {
                event.deferReply(true).setContent(ProxyDiscordLink.config.getString("discord-user-not-found")).queue();
                return;
            }

            int balance = ProxyDiscordLink.data.getBalance(id);

            int amount = event.getOption("amount").getAsInt();

            if(action.equalsIgnoreCase("add"))
            {
                ProxyDiscordLink.data.saveBalance(ProxyDiscordLink.data.getUUID(id),balance+amount);
                event.deferReply(true).setContent(replacePlaceholder("[+"+String.valueOf(amount)+"] " + ProxyDiscordLink.config.getString("other-discord-reiatsu"),id,balance+amount)).queue();
            }

            if(action.equalsIgnoreCase("remove"))
            {
                if(balance-amount < 0)
                {
                    event.deferReply(true).setContent("I reiatsu da rimuovere devono essere minori o uguali a quelli che il giocatore già possiede").queue();
                    return;
                }
                ProxyDiscordLink.data.saveBalance(ProxyDiscordLink.data.getUUID(id),balance-amount);
                event.deferReply(true).setContent(replacePlaceholder("[-"+String.valueOf(amount)+"] " + ProxyDiscordLink.config.getString("other-discord-reiatsu"),id,balance-amount)).queue();
            }

            if(action.equalsIgnoreCase("set"))
            {
                ProxyDiscordLink.data.saveBalance(ProxyDiscordLink.data.getUUID(id),amount);
                event.deferReply(true).setContent(replacePlaceholder("[set] " + ProxyDiscordLink.config.getString("other-discord-reiatsu"),id,amount)).queue();
            }

        }

    }

    public String replacePlaceholder(String message,String id,int amount)
    {
        if(message.contains("%user%")) message = message.replace("%user%",ProxyDiscordLink.data.getUsername(ProxyDiscordLink.data.getUUID(id)));
        if(message.contains("%amount%")) message = message.replace("%amount%",String.valueOf(amount));
        return message;
    }

}
