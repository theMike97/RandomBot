package listeners;

import managers.QuotesManager;
import managers.VoiceChannelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.HashMap;

public class CommandListener extends ListenerAdapter {

    // command static variables
    private static final String COMMAND_FLAG = "!";
    private static final String HELP_COMMAND = "help";
    private static final String QUOTE_COMMAND = "quote";
    private static final String MAXUSERS_COMMAND = "maxusers";
    private static final String TITLE_COMMAND = "title";

    private static final String MUST_BE_IN_VC_MESSAGE = "Must be in a voice channel to use this command.";
    private static final String MUST_BE_IN_ON_DEMAND_CHANNEL = "Must be in a created on-demand voice channel to change the name.";

    private QuotesManager qm;
    private VoiceChannelManager vcm;
    private EmbedBuilder embedBuilder;

    public CommandListener(QuotesManager qm, VoiceChannelManager vcm) {
        this.qm = qm;
        this.vcm = vcm;
        embedBuilder = new EmbedBuilder();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User user = event.getAuthor();

        if (message.getChannelType().isGuild() && !user.isBot()) {
            Member member = event.getMember();
            VoiceChannel vc = (member.getVoiceState() == null) ? null : member.getVoiceState().getChannel();
            TextChannel defaultChannel = event.getTextChannel();

            String[] messageArray = message.getContentRaw().split("\\s+");
            String command = messageArray[0];
            String[] args = Arrays.copyOfRange(messageArray, 1, messageArray.length); //O(n)

            switch (command) {
                case COMMAND_FLAG + HELP_COMMAND:
                    // if no args present, dm member complete commands list/description
                    // if args present, dm member description for specified command
                    if (args.length == 0) {
                        embedBuilder.clear();
                        embedBuilder.setTitle("RandomBot List of Commands");
                        embedBuilder.addField(QUOTE_COMMAND, "", false);
                        embedBuilder.addField(MAXUSERS_COMMAND, "", false);
                        embedBuilder.addField(TITLE_COMMAND, "", false);
                        defaultChannel.sendMessage(embedBuilder.build()).queue();
//                        });
                    } else if (args.length == 1) {
                        switch (args[0]) {
                            case HELP_COMMAND:
                                defaultChannel.sendMessage("You've requested help for the " + HELP_COMMAND).queue();
                                break;
                            case QUOTE_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + QUOTE_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + QUOTE_COMMAND + " [quote] ?[author]", "Add a quote by an author to the server.  If author is absent, RandomBot will use \"Anonymous\" instead.", false);
                                embedBuilder.addField(COMMAND_FLAG + QUOTE_COMMAND + "", "Get a random quote from the server and display it as a message.", false);
                                defaultChannel.sendMessage(embedBuilder.build()).queue();
                                break;
                            case MAXUSERS_COMMAND:
                                embedBuilder.clear();
                                embedBuilder.setTitle("RandomBot \"" + COMMAND_FLAG + MAXUSERS_COMMAND + "\" Command");
                                embedBuilder.setDescription("Requested by " + user.getAsMention());
                                embedBuilder.addField(COMMAND_FLAG + MAXUSERS_COMMAND + " [number]", "Set user limit on a custom on-demand voice channel.", false);
                                embedBuilder.addField(COMMAND_FLAG + MAXUSERS_COMMAND + "", "Reset voice channel user limit on a custom on-demand voice channel to unlimited.", false);
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
                                embedBuilder.addField( COMMAND_FLAG + command + " is not a valid command", "perhaps you meant:", false);
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
                    if (args.length == 0) { // no args
                        String quote = qm.getRandomQuote(event.getGuild());
                        defaultChannel.sendMessage(quote).queue();
                    } else {
                        String quote = "";
                        for (String word : args) {
                            quote += word + " ";
                        }
                        quote = quote.substring(0, quote.length() - 1);
                        qm.addQuote(event.getGuild(), quote);
                        // build embedmessage and send
                        embedBuilder.clear();
                        embedBuilder.setTitle("Quote Added");
                        embedBuilder.addField(quote, "- " + "Anonymous", false);
                        defaultChannel.sendMessage(embedBuilder.build()).queue();
                    }
                    break;

                case COMMAND_FLAG + MAXUSERS_COMMAND:
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
                            defaultChannel.sendMessage("Incorrect usage of `" + command + "`.");
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
}
