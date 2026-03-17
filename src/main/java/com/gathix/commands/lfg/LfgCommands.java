package com.gathix.commands.lfg;

import com.gathix.lfg.LfgPost;
import com.gathix.lfg.LfgService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class LfgCommands extends ListenerAdapter {

    private final LfgService lfgService;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public LfgCommands(LfgService lfgService) {
        this.lfgService = lfgService;
    }

    public static List<CommandData> getCommands() {
        return List.of(
                Commands.slash("lfg", "Looking for group system")
                        .addSubcommands(
                                new SubcommandData("post",
                                        "Post a new LFG request")
                                        .addOption(OptionType.STRING, "game",
                                                "Game you want to play", true)
                                        .addOption(OptionType.STRING, "description",
                                                "What are you looking for?", true)
                                        .addOption(OptionType.INTEGER, "players",
                                                "Number of players needed (1-9)",
                                                true),
                                new SubcommandData("list",
                                        "View open LFG posts in this server"),
                                new SubcommandData("join",
                                        "Join an LFG post")
                                        .addOption(OptionType.INTEGER, "id",
                                                "The LFG post ID to join", true),
                                new SubcommandData("close",
                                        "Close your LFG post")
                                        .addOption(OptionType.INTEGER, "id",
                                                "The LFG post ID to close", true)
                        )
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("lfg")) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        switch (subcommand) {
            case "post" -> handlePost(event);
            case "list" -> handleList(event);
            case "join" -> handleJoin(event);
            case "close" -> handleClose(event);
        }
    }

    private void handlePost(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        String game = event.getOption("game").getAsString();
        String description = event.getOption("description").getAsString();
        long players = event.getOption("players").getAsLong();

        if (players < 1 || players > 9) {
            event.reply("Players needed must be between 1 and 9.")
                    .setEphemeral(true).queue();
            return;
        }

        LfgPost post = lfgService.createPost(
                event.getGuild().getIdLong(),
                event.getUser().getIdLong(),
                game,
                description,
                (int) players
        );

        event.replyEmbeds(buildPostEmbed(post, event.getUser().getAsTag())
                .build()).queue();
    }

    private void handleList(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        List<LfgPost> posts = lfgService.getOpenPosts(
                event.getGuild().getIdLong());

        if (posts.isEmpty()) {
            event.reply("No open LFG posts right now. " +
                            "Use `/lfg post` to create one!")
                    .setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Open LFG Posts")
                .setColor(new Color(88, 101, 242));

        for (LfgPost post : posts) {
            String value = String.format(
                    "<@%d> — %s\n**Spots:** %d/%d remaining | **Expires:** %s",
                    post.getUserId(),
                    post.getDescription(),
                    post.getSpotsRemaining(),
                    post.getPlayersNeeded(),
                    post.getExpiresAt().format(FORMATTER)
            );
            embed.addField("**#" + post.getId() + "** — " + post.getGame(),
                    value, false);
        }

        embed.setFooter("Use /lfg join <id> to join a post");
        event.replyEmbeds(embed.build()).queue();
    }

    private void handleJoin(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        long postId = event.getOption("id").getAsLong();

        try {
            LfgPost post = lfgService.joinPost(postId,
                    event.getUser().getIdLong());

            String message = String.format(
                    "<@%d> joined **%s** (Post #%d). %d spot(s) remaining.",
                    event.getUser().getIdLong(),
                    post.getGame(),
                    post.getId(),
                    post.getSpotsRemaining()
            );

            if (post.getStatus().equals("FULL")) {
                message += "\n\n🎮 **This group is now full!**";
            }

            event.reply(message).queue();

        } catch (IllegalArgumentException e) {
            event.reply("LFG post #" + postId + " not found.")
                    .setEphemeral(true).queue();
        } catch (IllegalStateException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleClose(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        long postId = event.getOption("id").getAsLong();

        try {
            lfgService.closePost(postId, event.getUser().getIdLong());
            event.reply("LFG post #" + postId + " has been closed.")
                    .setEphemeral(true).queue();
        } catch (IllegalArgumentException e) {
            event.reply("LFG post #" + postId + " not found.")
                    .setEphemeral(true).queue();
        } catch (IllegalStateException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private EmbedBuilder buildPostEmbed(LfgPost post, String username) {
        return new EmbedBuilder()
                .setTitle("🎮 LFG — " + post.getGame())
                .setColor(new Color(88, 101, 242))
                .setDescription(post.getDescription())
                .addField("Posted by", username, true)
                .addField("Players needed",
                        post.getPlayersNeeded() + " player(s)", true)
                .addField("Post ID", "#" + post.getId(), true)
                .addField("Expires", post.getExpiresAt().format(FORMATTER)
                        + " (2 hours)", true)
                .setFooter("Use /lfg join " + post.getId() + " to join");
    }
}