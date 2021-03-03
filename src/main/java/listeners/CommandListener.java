package listeners;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import managers.QuotesManager;
import managers.RoleManager;
import managers.VoiceChannelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter {

    // static command variables
    private static final String COMMAND_FLAG = "!";

    private static final String HELP_COMMAND = "help";
    private static final String QUOTE_COMMAND = "quote";
    private static final String MAX_USERS_COMMAND = "max-users";
    private static final String TITLE_COMMAND = "title";
    private static final String ROLE_REACTION_ID_COMMAND = "role-reaction-id";
    private static final String ADD_ROLE_EMOTE_LINK = "add-role-emote-link";
    private static final String REMOVE_ROLE_EMOTE_LINK = "remove-role-emote-link";
    private static final String[] ADMIN_COMMANDS_LIST = new String[] {
            ADD_ROLE_EMOTE_LINK,
            REMOVE_ROLE_EMOTE_LINK,
            ROLE_REACTION_ID_COMMAND
    };
    private static final String[] EVERYONE_COMMANDS_LIST = new String[] {
            HELP_COMMAND,
            MAX_USERS_COMMAND,
            QUOTE_COMMAND,
            TITLE_COMMAND
    };

    // static error message variables
    private static final String MUST_BE_IN_VC_MESSAGE = "Must be in a voice channel to use this command.";
    private static final String MUST_BE_IN_ON_DEMAND_CHANNEL = "Must be in a created on-demand voice channel to change the name.";
    private static final String INVALID_ID_MESSAGE = "That is an invalid ID.  To get a valid ID, right click on the " +
            "desired entity and click \"Copy ID\" from the menu.";
    private static final String INVALID_EMOTE_MESSAGE = "That is an invalid emote.  Make sure you typed it correctly or didn't choose " +
            "an emote from a different server.";
    private static final String INVALID_PERMS_MESSAGE = "You do not have permission to use this command!  Ask the server owner to " +
            "help you out.";

    private final QuotesManager qm;
    private final VoiceChannelManager vcm;
    private final RoleManager rm;
    private final EmbedBuilder embedBuilder;

    public CommandListener(QuotesManager qm, VoiceChannelManager vcm, RoleManager rm) {
        this.qm = qm;
        this.vcm = vcm;
        this.rm = rm;
        embedBuilder = new EmbedBuilder();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User user = event.getAuthor();

        if (message.getChannelType().isGuild() && !user.isBot()) {
            Guild guild = event.getGuild();
            Member member = event.getMember();
            if (member == null) return;

            VoiceChannel vc = (member.getVoiceState() == null) ? null : member.getVoiceState().getChannel();
            TextChannel defaultChannel = event.getTextChannel();

            String[] messageArray = message.getContentRaw().split("\\s+");
            String command = messageArray[0];
            String[] args = Arrays.copyOfRange(messageArray, 1, messageArray.length); //O(n)

            switch (command) {

                // admin commands first
                case "!test":
                    if (!member.isOwner()) {
                        defaultChannel.sendMessage(INVALID_PERMS_MESSAGE).queue();
                        return;
                    }
                    System.out.println(extractEmoteId(args[0]));
                    break;

                case COMMAND_FLAG + ROLE_REACTION_ID_COMMAND:
                    if (!member.isOwner()) {
                        defaultChannel.sendMessage(INVALID_PERMS_MESSAGE).queue();
                        return;
                    }
                    if (args.length != 1) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    // we didn't use an id
                    if (Pattern.compile("^[0-9]]").matcher(args[0]).find()) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }

                    rm.syncReactionMessageTable(guild);
                    if (rm.setRoleAssignmentMessage(guild, args[0])) {
                        System.out.println("Role assignment message id set to " + rm.getRoleAssignmentMessageId());
                        defaultChannel.sendMessage("Role assignment message id set to " + rm.getRoleAssignmentMessageId()).queue();
                    } else {
                        System.out.println("Role assignment message id not set.");
                        defaultChannel.sendMessage("Role assignment message id not set.").queue();
                    }
                    break;

                case COMMAND_FLAG + ADD_ROLE_EMOTE_LINK:
                    if (!member.isOwner()) {
                        defaultChannel.sendMessage(INVALID_PERMS_MESSAGE).queue();
                        return;
                    }
                    if (args.length != 2) {
                        defaultChannel.sendMessage("Args mismatch.  Make sure you only have 2 arguments: emote and role.  " +
                                "Type `!help " + REMOVE_ROLE_EMOTE_LINK + "` for usage.").queue();
                        System.out.println("args mismatch");
                        return;
                    }
                    // we didn't use an emote/emoji
                    System.out.println(EmojiManager.isEmoji(args[0]));
                    if (!Pattern.compile("^<:[a-zA-Z0-9_]+:[0-9]+>$").matcher(args[0]).find() &&
                            !EmojiManager.isEmoji(args[0])) {
                        defaultChannel.sendMessage(INVALID_EMOTE_MESSAGE).queue();
                        System.out.println("malformed emote/emoji");
                        return;
                    }
                    // we didn't use '@role' syntax
                    if (!Pattern.compile("^<@&[0-9]+>$").matcher(args[1]).find()) {
                        defaultChannel.sendMessage("Bad role.  Make sure you spelled the role correctly using `@role` syntax.").queue();
                        System.out.println("malformed role");
                        return;
                    }

                    rm.syncRoleReactionEmotesTable(guild);
                    if (rm.addRoleEmoteLink(guild.getRoleById(extractRoleId(args[1])), extractEmoteId(args[0]))) {
                        System.out.println("Added (emote, role) (" + args[0] + ", " + args[1] + ")");
                        defaultChannel.sendMessage("Added (emote, role) (" + args[0] + ", " + args[1] + ")").queue();
                    } else {
                        System.out.println("Add (emote, role) failed.  Already exists.");
                        defaultChannel.sendMessage("That emote-role link already exists.").queue();
                    }
                    break;

                case COMMAND_FLAG + REMOVE_ROLE_EMOTE_LINK:
                    if (!member.isOwner()) {
                        defaultChannel.sendMessage(INVALID_PERMS_MESSAGE).queue();
                        return;
                    }
                    if (args.length != 2) {
                        defaultChannel.sendMessage("Args mismatch.  Make sure you only have 2 arguments: emote and role.  " +
                                "Type `!help " + REMOVE_ROLE_EMOTE_LINK + "` for usage.").queue();
                        System.out.println("args mismatch");
                        return;
                    }
                    // we didn't use an emote
                    if (!Pattern.compile("^<:[a-zA-Z0-9_]+:[0-9]+>$").matcher(args[0]).find() &&
                            !EmojiManager.isEmoji(args[0])) {
                        defaultChannel.sendMessage(INVALID_EMOTE_MESSAGE).queue();
                        System.out.println("malformed emote/emoji");
                        return;
                    }
                    // we didn't use '@role' syntax
                    if (!Pattern.compile("^<@&[0-9]+>$").matcher(args[1]).find()) {
                        defaultChannel.sendMessage("Bad role.  Make sure you spelled the role correctly using `@role` syntax.").queue();
                        System.out.println("malformed role");
                        return;
                    }

                    rm.syncRoleReactionEmotesTable(guild);
                    if (rm.removeRoleEmoteLink(guild.getRoleById(extractRoleId(args[1])), extractEmoteId(args[0]))) {
                        System.out.println("Removed (emote, role) (" + extractEmoteId(args[0]) + ", " + args[1] + ")");
                        defaultChannel.sendMessage("Removed (emote, role) (" + extractEmoteId(args[0]) + ", " + args[1] + ")").queue();
                    } else {
                        System.out.println("Emote-Role remove failed.");
                        defaultChannel.sendMessage("Emote-Role remove failed.").queue();
                    }
                    break;

                // everyone commands
                case COMMAND_FLAG + HELP_COMMAND:
                    // if no args present, dm member complete commands list/description
                    // if args present, dm member description for specified command
                    if (args.length == 0) {
                        String adminCommands = "";
                        String everyoneCommands = "";

                        for (String adminCommand : ADMIN_COMMANDS_LIST) {
                            adminCommands += "\u2022 " + adminCommand + "\n";
                        }
                        adminCommands = adminCommands.substring(0, adminCommands.length()-1);
                        for (String everyoneCommand : EVERYONE_COMMANDS_LIST) {
                            everyoneCommands += "\u2022 " + everyoneCommand + "\n";
                        }
                        everyoneCommands = everyoneCommands.substring(0, everyoneCommands.length()-1);

                        embedBuilder.clear();
                        embedBuilder.setTitle("RandomBot List of Commands");
                        embedBuilder.setDescription("Requested by " + user.getAsMention());
                        embedBuilder.addField("Owner commands", adminCommands, false);
                        embedBuilder.addField("Regular commands", everyoneCommands, false);

                    } else if (args.length == 1) {
                        switch (args[0]) {
                            // admin commands
                            case ROLE_REACTION_ID_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + QUOTE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + ROLE_REACTION_ID_COMMAND + " [message id]",
                                        "Tell RandomBot which message it should listen to for role reactions.",
                                        false);
                                break;

                            case ADD_ROLE_EMOTE_LINK:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + ADD_ROLE_EMOTE_LINK + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + ADD_ROLE_EMOTE_LINK + " [:emote:] [@role]",
                                        "Link a reaction emote/emoji to a role.  Upon successful completion," +
                                                " reacting to the react message with [:emote:] will assign you" +
                                                " the associated role.",
                                        false);
                                break;

                            case REMOVE_ROLE_EMOTE_LINK:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + REMOVE_ROLE_EMOTE_LINK + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + REMOVE_ROLE_EMOTE_LINK + " [:emote:] [@role]",
                                        "Remove link between an emote and its associated role.  Upon successful completion," +
                                                " reacting to the react message with [:emote:] will no longer assign you" +
                                                " to a role.",
                                        false);
                                break;
                                // everyone commands
                            case HELP_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + HELP_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + HELP_COMMAND + " ?[command]",
                                        "If [command] is present, RandomBot will send a message detailing the usage" +
                                                " and applications of [command].  Otherwise, RandomBot will send a message" +
                                                " enumerating all commands available to the user.",
                                        false);
                                break;

                            case QUOTE_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + QUOTE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + QUOTE_COMMAND + " \"[quote]\" ?[author]",
                                        "Add a quote by an author to the server.  If author is absent, RandomBot will use \"Anonymous\" instead.",
                                        false);
                                embedBuilder.addField(COMMAND_FLAG + QUOTE_COMMAND + "",
                                        "Get a random quote from the server and display it as a message.",
                                        false);
                                break;
                            case MAX_USERS_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + MAX_USERS_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + MAX_USERS_COMMAND + " [number]",
                                        "Set user limit on a custom on-demand voice channel.",
                                        false);
                                embedBuilder.addField(COMMAND_FLAG + MAX_USERS_COMMAND + "",
                                        "Reset voice channel user limit on a custom on-demand voice channel to unlimited.",
                                        false);
                                break;
                            case TITLE_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + TITLE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + TITLE_COMMAND + " [new custom title]",
                                        "Set a custom title for a custom on-demand voice channel.",
                                        false);
                                embedBuilder.addField(COMMAND_FLAG + TITLE_COMMAND + "",
                                        "Reset voice channel title to the title determined by RandomBot.",
                                        false);
                                break;
                            default:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot Command Help");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(command + " is not a valid command",
                                        "perhaps you meant:",
                                        false);
                                break;
                        }
                    } else {
                        embedBuilder.clear();
                        embedBuilder.setTitle("RandomBot Command Help");
                        embedBuilder.setDescription("Requested by " + user.getAsMention());
                        embedBuilder.addField("!command is not a valid command",
                                "perhaps you meant:",
                                false);
                    }
                    defaultChannel.sendMessage(embedBuilder.build()).queue();
                    break;

                case COMMAND_FLAG + QUOTE_COMMAND:
                    embedBuilder.clear();
                    if (args.length == 0) { // no args
                        String[] quoteData = qm.getRandomQuote(guild);
                        embedBuilder.addField(quoteData[0], "-" + quoteData[1], false);
                    } else {
                        // quote should be in ""
                        // followed by the author
                        if (!args[0].startsWith("\"")) {
                            defaultChannel.sendMessage("Quote not formatted correctly! (1)").queue();
                            return;
                        }
                        String[] quoteData = new String[2];

                        // get quote
                        String quoteText = "";
                        boolean endQuoteMark = false;
                        int index = 0;
                        for (; index < args.length; index++) {
                            quoteText += args[index] + " ";
                            if (args[index].endsWith("\"")) {
                                endQuoteMark = true;
                                index++;
                                break;
                            }
                        }
                        if (!endQuoteMark) {
                            defaultChannel.sendMessage("Quote not formatted correctly! (2)").queue();
                            return;
                        }
                        quoteText = quoteText.substring(1, quoteText.length() - 2);
                        quoteText = quoteText.replaceAll("\\s+(?=\\s)", "");
                        quoteData[0] = quoteText;

                        // everything after is author.  if nothing after, say anonymous
                        String author = "";
                        for (; index < args.length; index++) {
                            author += args[index] + " ";
                        }

                        author = author.replaceAll("^\\s+|\\s+$|\\s+(?=\\s)", "");
                        if (author.startsWith("-")) author = author.replaceFirst("-", "");
                        if (author.equals("")) author = "Anonymous";
                        quoteData[1] = author;

                        try {
                            qm.addQuote(guild, quoteData);
                        } catch (Exception e) {
                            e.printStackTrace();
                            defaultChannel.sendMessage("Quote add failed.").queue();
                            return;
                        }
                        // build embedded message and send
                        embedBuilder.setTitle("Quote Added");
                        embedBuilder.addField("\"" + quoteData[0] + "\"", "-" + quoteData[1], false);
                        System.out.println("Quote added.");
                    }
                    defaultChannel.sendMessage(embedBuilder.build()).queue();
                    break;

                case COMMAND_FLAG + MAX_USERS_COMMAND:
                    if (vc != null) {
                        if (args.length == 0) {
                            vc.getManager().setUserLimit(0).queue();
                            defaultChannel.sendMessage("Reset user limit.").queue();
                        } else if (args.length == 1) {
                            int maxUsers = 0;
                            try {
                                maxUsers = Integer.parseInt(args[0]);
                            } catch (NumberFormatException ex) {
                                ex.printStackTrace(); // we used malformed arguments :(
                            }
                            if (maxUsers > 0) {
                                if (vcm.isCreatedVoiceChannel(vc)) {
                                    vc.getManager().setUserLimit(maxUsers).queue();
                                    defaultChannel.sendMessage("Set max users to " + maxUsers + "!").queue();
                                } else {
                                    defaultChannel.sendMessage("Can only change user limit of on-demand channels.").queue();
                                }
                            } else {
                                defaultChannel.sendMessage("Number of users must be a positive integer.").queue();
                            }
                        } else {
                            defaultChannel.sendMessage("Incorrect usage of `" + command + "`.").queue();
                        }
                    } else {
                        defaultChannel.sendMessage(MUST_BE_IN_VC_MESSAGE).queue();
                    }
                    break;

                case COMMAND_FLAG + TITLE_COMMAND:
                    if (vc != null) {
                        if (vcm.isCreatedVoiceChannel(vc)) {
                            if (args.length == 0) {
                                // reset naming to standard naming convention
                                vcm.setStandardChannelName(vc);
                                defaultChannel.sendMessage("Restored title.").queue();
                            } else {
                                // set custom title
                                String customTitle = "";
                                for (String word : args) customTitle += word + " ";
                                customTitle = customTitle.substring(0, customTitle.length() - 1);
                                vcm.setCustomChannelName(vc, customTitle);
                                defaultChannel.sendMessage("Changed title to " + customTitle).queue();
                            }
                        } else {
                            defaultChannel.sendMessage(MUST_BE_IN_ON_DEMAND_CHANNEL).queue();
                        }
                    } else {
                        defaultChannel.sendMessage(MUST_BE_IN_VC_MESSAGE).queue();
                    }
                    break;
            }
        }
    }

    private String extractRoleId(String rawRoleString) {
        Matcher matcher = Pattern.compile("^<@&([0-9]+)>$").matcher(rawRoleString);
        matcher.find();
        String emoteId = matcher.group(1);

        return emoteId;
    }

    private String extractEmoteId(String rawEmojiString) {
        String emojiCodePoints;
        if (EmojiManager.isEmoji(rawEmojiString)) {
            emojiCodePoints = EmojiParser.parseToHtmlHexadecimal(rawEmojiString);
            emojiCodePoints = emojiCodePoints.replaceAll("&#x", "U+");
            emojiCodePoints = emojiCodePoints.replaceAll(";", "");
        } else {
            Matcher matcher = Pattern.compile("^<:[a-zA-Z0-9_]+:([0-9]+)>$").matcher(rawEmojiString);
            matcher.find();
            emojiCodePoints = matcher.group(1);
        }
        return emojiCodePoints;
    }
}
