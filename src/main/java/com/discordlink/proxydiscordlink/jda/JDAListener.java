package com.discordlink.proxydiscordlink.jda;

import com.discordlink.proxydiscordlink.ProxyDiscordLink;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
        if(member == null) return;
        MessageReaction reaction = event.getReaction();

        if(reaction.getEmoji().equals(Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.link-emoji"))))
        {

            Category cat = DiscordBot.guild.getCategoryById(ProxyDiscordLink.config.getString("category-link-id"));
            TextChannel textChannel = cat.createTextChannel(member.getNickname())
                    .addPermissionOverride(DiscordBot.guild.getPublicRole(),null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(member,EnumSet.of(Permission.VIEW_CHANNEL),null)
                    .addPermissionOverride(DiscordBot.guild.getRoleById(ProxyDiscordLink.config.getString("staff-role")), EnumSet.of(Permission.VIEW_CHANNEL),null)
                    .complete();
            String message = ProxyDiscordLink.config.getString("link-player-message");
            if(message.contains("%mention%")) message = message.replace("%mention%","<@"+member.getId()+">");
            textChannel.sendMessage(message).complete();
            ProxyDiscordLink.linkChannel.put(member.getId(), textChannel.getId());
            reaction.removeReaction().complete();
        }

        if(reaction.getEmoji().equals(Emoji.fromUnicode(ProxyDiscordLink.config.getString("link-panel.sync-emoji"))))
        {
            if(!ProxyDiscordLink.data.exists(member.getId())) return;
            ProxyDiscordLink.sync(ProxyDiscordLink.data.getUUID(member.getId()));
            reaction.removeReaction().complete();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Member member = event.getMember();
        if(member == null) return;
        if(event.getMessage().getCategory().getId().equals(ProxyDiscordLink.config.getString("category-link-id")))
        {
            if(ProxyDiscordLink.linkChannel.containsKey(member.getId()))
            {
                TextChannel textChannel = event.getGuild().getTextChannelById(ProxyDiscordLink.linkChannel.get(member.getId()));
                String message = event.getMessage().getContentRaw();
                if(!ProxyDiscordLink.data.existsName(message))
                {
                    textChannel.sendMessage(ProxyDiscordLink.config.getString("invalid-username")).complete();
                    return;
                }
                ProxyDiscordLink.data.saveDiscordID(message,member.getId());
                ProxyDiscordLink.linkChannel.remove(member.getId(),textChannel.getId());
                textChannel.delete().complete();
                return;
            }
        }

        if(ProxyDiscordLink.data.exists(member.getId()))
        {
            if(ProxyDiscordLink.config.contains("chats"))
            {
                for(String key : ProxyDiscordLink.config.getConfigurationSection("chats").getKeys(false))
                {
                    if(event.getMessage().getChannel().getId().equals(ProxyDiscordLink.config.getString("chats."+key)))
                    {
                        ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText("§f[§bDiscord§f] "+ ProxyDiscordLink.data.getUsername(ProxyDiscordLink.data.getUUID(member.getId())) + " §7»§f " + event.getMessage().getContentRaw()));
                    }
                }
            }
        }

    }

}
