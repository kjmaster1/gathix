package com.gathix.guild;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class GuildEventListener extends ListenerAdapter {

    private final GuildService guildService;

    public GuildEventListener(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        guildService.registerGuild(
                event.getGuild().getIdLong(),
                event.getGuild().getName()
        );
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        guildService.removeGuild(event.getGuild().getIdLong());
    }
}