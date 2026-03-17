package com.gathix.commands.moderation;

import com.gathix.moderation.ModerationService;
import com.gathix.moderation.Warning;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
public class ModerationCommands extends ListenerAdapter {

    private final ModerationService moderationService;

    public ModerationCommands(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    public static List<CommandData> getCommands() {
        return List.of(
                Commands.slash("warn", "Warn a member")
                        .addOption(OptionType.USER, "user", "The user to warn", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the warning", true)
                        .setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(Permission.MODERATE_MEMBERS)),

                Commands.slash("warnings", "View warnings for a member")
                        .addOption(OptionType.USER, "user", "The user to check", true)
                        .setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(Permission.MODERATE_MEMBERS)),

                Commands.slash("kick", "Kick a member from the server")
                        .addOption(OptionType.USER, "user", "The user to kick", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the kick", true)
                        .setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(Permission.KICK_MEMBERS)),

                Commands.slash("ban", "Ban a member from the server")
                        .addOption(OptionType.USER, "user", "The user to ban", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the ban", true)
                        .addOption(OptionType.INTEGER, "delete_days",
                                "Days of messages to delete (0-7)", false)
                        .setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(Permission.BAN_MEMBERS)),

                Commands.slash("unban", "Unban a user")
                        .addOption(OptionType.STRING, "user_id", "The user ID to unban", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the unban", true)
                        .setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(Permission.BAN_MEMBERS))
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "warn" -> handleWarn(event);
            case "warnings" -> handleWarnings(event);
            case "kick" -> handleKick(event);
            case "ban" -> handleBan(event);
            case "unban" -> handleUnban(event);
        }
    }

    private void handleWarn(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        Member target = event.getOption("user").getAsMember();
        String reason = event.getOption("reason").getAsString();

        if (target == null) {
            event.reply("Could not find that user in this server.")
                    .setEphemeral(true).queue();
            return;
        }

        if (target.isOwner()) {
            event.reply("You cannot warn the server owner.")
                    .setEphemeral(true).queue();
            return;
        }

        Warning warning = moderationService.warnUser(
                event.getGuild().getIdLong(),
                target.getIdLong(),
                event.getUser().getIdLong(),
                reason
        );

        long warningCount = moderationService.getWarningCount(
                event.getGuild().getIdLong(),
                target.getIdLong()
        );

        event.replyEmbeds(
                moderationService.buildModEmbed("WARN",
                                target.getIdLong(),
                                event.getUser().getIdLong(),
                                reason, Color.YELLOW)
                        .addField("Total Warnings",
                                String.valueOf(warningCount), true)
                        .build()
        ).queue();
    }

    private void handleWarnings(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.reply("Could not find that user.").setEphemeral(true).queue();
            return;
        }

        List<Warning> warnings = moderationService.getWarnings(
                event.getGuild().getIdLong(),
                target.getIdLong()
        );

        if (warnings.isEmpty()) {
            event.reply(target.getEffectiveName() + " has no warnings.")
                    .setEphemeral(true).queue();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < warnings.size(); i++) {
            Warning w = warnings.get(i);
            sb.append("**#").append(i + 1).append("** — ")
                    .append(w.getReason())
                    .append(" (<@").append(w.getModeratorId()).append(">)\n");
        }

        event.replyEmbeds(
                new net.dv8tion.jda.api.EmbedBuilder()
                        .setTitle("Warnings for " + target.getEffectiveName())
                        .setDescription(sb.toString())
                        .setColor(Color.ORANGE)
                        .build()
        ).setEphemeral(true).queue();
    }

    private void handleKick(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        Member target = event.getOption("user").getAsMember();
        String reason = event.getOption("reason").getAsString();

        if (target == null) {
            event.reply("Could not find that user.").setEphemeral(true).queue();
            return;
        }

        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.reply("I don't have permission to kick that user.")
                    .setEphemeral(true).queue();
            return;
        }

        moderationService.kickUser(
                event.getGuild(),
                target.getIdLong(),
                event.getUser().getIdLong(),
                reason
        );

        event.replyEmbeds(
                moderationService.buildModEmbed("KICK",
                                target.getIdLong(),
                                event.getUser().getIdLong(),
                                reason, Color.RED)
                        .build()
        ).queue();
    }

    private void handleBan(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        Member target = event.getOption("user").getAsMember();
        String reason = event.getOption("reason").getAsString();
        int deleteDays = event.getOption("delete_days") != null
                ? (int) event.getOption("delete_days").getAsLong() : 0;

        if (target == null) {
            event.reply("Could not find that user.").setEphemeral(true).queue();
            return;
        }

        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.reply("I don't have permission to ban that user.")
                    .setEphemeral(true).queue();
            return;
        }

        moderationService.banUser(
                event.getGuild(),
                target.getIdLong(),
                event.getUser().getIdLong(),
                reason,
                deleteDays
        );

        event.replyEmbeds(
                moderationService.buildModEmbed("BAN",
                                target.getIdLong(),
                                event.getUser().getIdLong(),
                                reason, Color.RED)
                        .build()
        ).queue();
    }

    private void handleUnban(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        String userIdStr = event.getOption("user_id").getAsString();
        String reason = event.getOption("reason").getAsString();

        try {
            long userId = Long.parseLong(userIdStr);
            moderationService.unbanUser(
                    event.getGuild(),
                    userId,
                    event.getUser().getIdLong(),
                    reason
            );

            event.replyEmbeds(
                    moderationService.buildModEmbed("UNBAN",
                                    userId,
                                    event.getUser().getIdLong(),
                                    reason, Color.GREEN)
                            .build()
            ).queue();
        } catch (NumberFormatException e) {
            event.reply("Invalid user ID — must be a numeric Discord ID.")
                    .setEphemeral(true).queue();
        }
    }
}