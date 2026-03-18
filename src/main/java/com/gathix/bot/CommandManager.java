package com.gathix.bot;

import com.gathix.commands.lfg.LfgCommands;
import com.gathix.commands.moderation.ModerationCommands;
import com.gathix.commands.stats.StatsCommands;
import com.gathix.commands.tournament.TournamentCommands;
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
        commands.add(Commands.slash("help", "View all Gathix commands and how to use them"));
        commands.addAll(ModerationCommands.getCommands());
        commands.addAll(StatsCommands.getCommands());
        commands.addAll(LfgCommands.getCommands());
        commands.addAll(TournamentCommands.getCommands());

        if (!devGuildId.isEmpty()) {
            // Register to specific guild instantly — for development
            event.getJDA().getGuildById(devGuildId).updateCommands().addCommands(commands).queue(cmds -> log.info("Registered {} command(s) to dev guild", cmds.size()));
        } else {
            // Register globally — for production (up to 1 hour propagation)
            event.getJDA().updateCommands().addCommands(commands).queue(cmds -> log.info("Registered {} command(s) globally", cmds.size()));
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping" -> event.reply("Pong! Gathix is online.").queue();
            case "help" -> handleHelp(event);
        }
    }

    private void handleHelp(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new net.dv8tion.jda.api.EmbedBuilder()
                .setTitle("Gathix — Command Reference")
                .setColor(new java.awt.Color(88, 101, 242))
                .setDescription("All commands use Discord slash commands — type `/` to see them.")
                .addField("Moderation",
                        "`/warn` `/warnings` `/kick` `/ban` `/unban` `/timeout` `/untimeout`", false)
                .addField("Game Stats",
                        "`/stats steam <username>` — Look up a Steam profile", false)
                .addField("Looking for Group",
                        "`/lfg post` `/lfg list` `/lfg join` `/lfg close`", false)
                .addField("Tournaments",
                        "`/tournament list` `/tournament register` `/tournament bracket`\n" +
                                "`/tournament-admin create` `/tournament-admin start` `/tournament-admin result`",
                        false)
                .addField("General",
                        "`/ping` — Check if Gathix is online\n`/help` — Show this message", false)
                .setFooter("Gathix — Built for gaming communities | github.com/kjmaster1/gathix")
                .build()
        ).setEphemeral(true).queue();
    }
}