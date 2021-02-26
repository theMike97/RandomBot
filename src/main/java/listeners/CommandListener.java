package listeners;

import managers.QuotesManager;
import managers.VoiceChannelManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class CommandListener extends ListenerAdapter {

    private static final String COMMAND_FLAG = "!";
    private static final String MUST_BE_IN_VC_MESSAGE = "Must be in a voice channel to use this command.";

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
                case COMMAND_FLAG + "help":
                    user.openPrivateChannel().queue((channel) -> {
                        channel.sendMessage("You've requested help!").queue();
                    });
                    break;

                case COMMAND_FLAG + "quote":
                    if (args.length == 0) { // no args
                        String quote = qm.getRandomQuote(event.getGuild());
                        defaultChannel.sendMessage(quote).queue();
                    } else {
                        String quote = "";
                        for (String word : args) {
                            quote += word + " ";
                        }
                        quote = quote.substring(0, quote.length()-1);
                        qm.addQuote(event.getGuild(), quote);
                        defaultChannel.sendMessage("Quote added!").queue();
                    }
                    break;

                case COMMAND_FLAG + "maxusers":
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

                case COMMAND_FLAG + "title":
                    if (vc != null) {
                        if (args.length == 0) {
                            // reset naming to standard naming convention
                            vcm.setStandardChannelName(vc);
                        } else {
                            // set custom title
                            String customTitle = "";
                            for (String word : args) customTitle += word + " ";
                            customTitle = customTitle.substring(0, customTitle.length()-1);
                            vcm.setCustomChannelName(vc, customTitle);
                        }
                    } else {
                        defaultChannel.sendMessage(MUST_BE_IN_VC_MESSAGE).queue();
                    }
            }
        }
    }
}
