package listeners;

import managers.QuotesManager;
import managers.RoleManager;
import managers.VoiceChannelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter {

    // command static variables
    private static final String COMMAND_FLAG = "!";
    private static final String HELP_COMMAND = "help";
    private static final String QUOTE_COMMAND = "quote";
    private static final String MAX_USERS_COMMAND = "max-users";
    private static final String TITLE_COMMAND = "title";
    private static final String ROLE_REACTION_ID_COMMAND = "role-reaction-id";
    private static final String ADD_ROLE_EMOTE_LINK = "add-role-emote-link";
    private static final String REMOVE_ROLE_EMOTE_LINK = "remove-role-emote-link";

    private static final String MUST_BE_IN_VC_MESSAGE = "Must be in a voice channel to use this command.";
    private static final String MUST_BE_IN_ON_DEMAND_CHANNEL = "Must be in a created on-demand voice channel to change the name.";
    private static final String INVALID_ID_MESSAGE = "That is an invalid ID.  To get a valid message ID, right click on the" +
            "desired message and click \"Copy ID\" from the menu.";

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
            VoiceChannel vc = null;
            if (member != null) {
                vc = (member.getVoiceState() == null) ? null : member.getVoiceState().getChannel();
            }
            TextChannel defaultChannel = event.getTextChannel();

            String[] messageArray = message.getContentRaw().split("\\s+");
            String command = messageArray[0];
            String[] args = Arrays.copyOfRange(messageArray, 1, messageArray.length); //O(n)

            switch (command) {

                // admin commands first
                case "!test":
                    System.out.println(rm.getRoleAssignmentMessageId(event.getGuild()));
                    break;

                case COMMAND_FLAG + ROLE_REACTION_ID_COMMAND:
                    if (args.length != 1) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    // we didn't use an id
                    if (Pattern.compile("^[0-9]]").matcher(args[0]).find()) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    rm.setRoleAssignmentMessage(guild, args[0]);
                    System.out.println("Role assignment message id set to " + rm.getRoleAssignmentMessageId(guild));
                    break;

                case COMMAND_FLAG + ADD_ROLE_EMOTE_LINK:
                    if (args.length != 2) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    // we didn't use an emote
                    if (!Pattern.compile("^<:[a-zA-Z0-9_]+:[0-9]+>$").matcher(args[0]).find()) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    // we didn't use an id
                    if (Pattern.compile("^[0-9]]").matcher(args[1]).find()) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }

                    rm.addRoleEmoteLink(Objects.requireNonNull(guild.getRoleById(args[1])), extractEmoteId(args[0]));
                    break;

                case COMMAND_FLAG + REMOVE_ROLE_EMOTE_LINK:
                    if (args.length != 2) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    // we didn't use an emote
                    if (!Pattern.compile("^<:[a-zA-Z0-9_]+:[0-9]+>$").matcher(args[0]).find()) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }
                    // we didn't use an id
                    if (Pattern.compile("^[0-9]]").matcher(args[1]).find()) {
                        defaultChannel.sendMessage(INVALID_ID_MESSAGE).queue();
                        return;
                    }

                    rm.removeRoleEmoteLink(Objects.requireNonNull(guild.getRoleById(args[1])), extractEmoteId(args[0]));
                    break;

                // everyone commands
                case COMMAND_FLAG + HELP_COMMAND:
                    // if no args present, dm member complete commands list/description
                    // if args present, dm member description for specified command
                    if (args.length == 0) {
                        embedBuilder.clear();
                        embedBuilder.setTitle("RandomBot List of Commands");
                        embedBuilder.addField(QUOTE_COMMAND, "", false);
                        embedBuilder.addField(MAX_USERS_COMMAND, "", false);
                        embedBuilder.addField(TITLE_COMMAND, "", false);
                        defaultChannel.sendMessage(embedBuilder.build()).queue();
//                        });
                    } else if (args.length == 1) {
                        switch (args[0]) {
                            case ROLE_REACTION_ID_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + QUOTE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + ROLE_REACTION_ID_COMMAND + "[message id]", "Tell RandomBot which message it should listen to for role reactions.", false);
                                defaultChannel.sendMessage(embedBuilder.build()).queue();
                            case HELP_COMMAND:
                                defaultChannel.sendMessage("You've requested help for the " + HELP_COMMAND).queue();
                                break;
                            case QUOTE_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + QUOTE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + QUOTE_COMMAND + " \"[quote]\" ?[author]", "Add a quote by an author to the server.  If author is absent, RandomBot will use \"Anonymous\" instead.", false);
                                embedBuilder.addField(COMMAND_FLAG + QUOTE_COMMAND + "", "Get a random quote from the server and display it as a message.", false);
                                defaultChannel.sendMessage(embedBuilder.build()).queue();
                                break;
                            case MAX_USERS_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + MAX_USERS_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + MAX_USERS_COMMAND + " [number]", "Set user limit on a custom on-demand voice channel.", false);
                                embedBuilder.addField(COMMAND_FLAG + MAX_USERS_COMMAND + "", "Reset voice channel user limit on a custom on-demand voice channel to unlimited.", false);
                                defaultChannel.sendMessage(embedBuilder.build()).queue();
                                break;
                            case TITLE_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + TITLE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + TITLE_COMMAND + " [new custom title]", "Set a custom title for a custom on-demand voice channel.", false);
                                embedBuilder.addField(COMMAND_FLAG + TITLE_COMMAND + "", "Reset voice channel title to the title determined by RandomBot.", false);
                                defaultChannel.sendMessage(embedBuilder.build()).queue();
                                break;
                            default:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot Command Help");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + command + " is not a valid command", "perhaps you meant:", false);
                                defaultChannel.sendMessage(embedBuilder.build()).queue();
                                break;
                        }
                    } else {
                        embedBuilder.clear();
                        embedBuilder.setTitle("RandomBot Command Help");
                        embedBuilder.setDescription("Requested by " + user.getAsMention());
                        embedBuilder.addField("!command is not a valid command", "perhaps you meant:", false);
                    }
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

    private String extractEmoteId(String rawEmoteString) {
        // remove <> symbols
        rawEmoteString = rawEmoteString.replaceAll("[<>]", "");
        StringBuilder sb = new StringBuilder();
        // iterate backwards until we hit the first ':'
        for (int i = rawEmoteString.length() - 1; i >= 0; i--) {
            if (rawEmoteString.charAt(i) == ':') break;
            sb.append(rawEmoteString.charAt(i));
        }
        sb.reverse();

        return sb.toString();
    }
}
