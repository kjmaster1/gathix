package com.gathix.bot;

import com.gathix.guild.GuildService;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BotStartupListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(BotStartupListener.class);
    private final GuildService guildService;

    public BotStartupListener(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.info("Gathix is online — logged in as {}",
                event.getJDA().getSelfUser().getAsTag());
        log.info("Connected to {} guild(s)", event.getGuildTotalCount());

        // Register any guilds we're already in that aren't in the database yet
        event.getJDA().getGuilds().forEach(guild ->
                guildService.registerGuild(guild.getIdLong(), guild.getName())
        );
    }
}