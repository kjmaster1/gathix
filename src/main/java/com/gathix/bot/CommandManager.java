package com.gathix.bot;

import com.gathix.commands.moderation.ModerationCommands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CommandManager extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

    @Value("${gathix.dev-guild-id:}")
    private String devGuildId;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var commands = new ArrayList<CommandData>();
        commands.add(Commands.slash("ping", "Check if Gathix is alive"));
        commands.addAll(ModerationCommands.getCommands());

        if (!devGuildId.isEmpty()) {
            // Register to specific guild instantly — for development
            event.getJDA().getGuildById(devGuildId)
                    .updateCommands()
                    .addCommands(commands)
                    .queue(cmds -> log.info("Registered {} command(s) to dev guild",
                            cmds.size()));
        } else {
            // Register globally — for production (up to 1 hour propagation)
            event.getJDA().updateCommands()
                    .addCommands(commands)
                    .queue(cmds -> log.info("Registered {} command(s) globally",
                            cmds.size()));
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            event.reply("Pong! Gathix is online.").queue();
        }
    }
}