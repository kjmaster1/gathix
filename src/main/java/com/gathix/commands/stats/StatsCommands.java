package com.gathix.commands.stats;

import com.fasterxml.jackson.databind.JsonNode;
import com.gathix.stats.SteamService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
public class StatsCommands extends ListenerAdapter {

    private final SteamService steamService;

    public StatsCommands(SteamService steamService) {
        this.steamService = steamService;
    }

    public static List<CommandData> getCommands() {
        return List.of(
                Commands.slash("stats", "Look up game stats for a player")
                        .addSubcommands(
                                new SubcommandData("steam",
                                        "Look up a Steam profile")
                                        .addOption(OptionType.STRING, "username",
                                                "Steam username or vanity URL", true)
                        )
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats")) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        if (subcommand.equals("steam")) {
            handleSteamStats(event);
        }
    }

    private void handleSteamStats(SlashCommandInteractionEvent event) {
        String username = event.getOption("username").getAsString();

        event.deferReply().queue();

        // Determine the Steam64 ID
        String steamId;

        // If input is already a Steam64 ID (17 digit number starting with 7656)
        if (username.matches("\\d{17}") && username.startsWith("7656")) {
            steamId = username;
        } else {
            // Try resolving as a vanity URL
            steamId = steamService.resolveVanityUrl(username);
            if (steamId == null) {
                event.getHook().editOriginal(
                        "Could not find a Steam profile for `" + username + "`.\n\n" +
                                "**Tips:**\n" +
                                "- Use your **Steam64 ID** (17-digit number) for guaranteed results\n" +
                                "- Find your Steam64 ID at https://steamidfinder.com\n" +
                                "- Or set a custom URL in Steam: **Profile → Edit Profile → Custom URL**"
                ).queue();
                return;
            }
        }

        JsonNode player = steamService.getPlayerSummary(steamId);
        if (player == null) {
            event.getHook().editOriginal(
                    "Found a Steam ID but could not load the profile. " +
                            "Make sure the profile is set to **public**."
            ).queue();
            return;
        }

        JsonNode ownedGames = steamService.getOwnedGames(steamId);
        JsonNode recentGames = steamService.getRecentGames(steamId);

        String personaName = player.path("personaname").asText("Unknown");
        String profileUrl = player.path("profileurl").asText("");
        String avatarUrl = player.path("avatarfull").asText("");
        int personaState = player.path("personastate").asInt(0);
        String status = steamService.getPersonaState(personaState);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(personaName + "'s Steam Profile", profileUrl)
                .setThumbnail(avatarUrl)
                .setColor(new Color(27, 40, 56)) // Steam dark blue
                .addField("Status", status, true);

        // Owned games
        if (ownedGames != null && ownedGames.has("game_count")) {
            int gameCount = ownedGames.path("game_count").asInt();
            long totalMinutes = 0;
            JsonNode games = ownedGames.path("games");
            if (games.isArray()) {
                for (JsonNode game : games) {
                    totalMinutes += game.path("playtime_forever").asLong(0);
                }
            }
            long totalHours = totalMinutes / 60;
            embed.addField("Games Owned", String.valueOf(gameCount), true);
            embed.addField("Total Playtime", totalHours + " hours", true);
        }

        // Recently played games
        if (recentGames != null && recentGames.has("games")) {
            JsonNode games = recentGames.path("games");
            if (games.isArray() && games.size() > 0) {
                StringBuilder recent = new StringBuilder();
                for (JsonNode game : games) {
                    String gameName = game.path("name").asText("Unknown");
                    long minutes = game.path("playtime_2weeks").asLong(0);
                    long hours = minutes / 60;
                    long mins = minutes % 60;
                    recent.append("**").append(gameName).append("** — ")
                            .append(hours).append("h ").append(mins).append("m\n");
                }
                embed.addField("Recently Played (2 weeks)", recent.toString(), false);
            }
        }

        embed.setFooter("Steam ID: " + steamId);
        event.getHook().editOriginalEmbeds(embed.build()).queue();
    }
}