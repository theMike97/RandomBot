package listeners;

import managers.QuotesManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class CommandListener extends ListenerAdapter {

    private static final String COMMAND_FLAG = "!";

    private QuotesManager qm;
    private EmbedBuilder embedBuilder;

    public CommandListener(QuotesManager qm) {
        this.qm = qm;
        embedBuilder = new EmbedBuilder();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User user = event.getAuthor();
        TextChannel defaultChannel = event.getTextChannel();

        String[] messageArray = message.getContentRaw().split("\\s+");
        String command = messageArray[0];
        String[] args = Arrays.copyOfRange(messageArray, 1, messageArray.length); //O(n)

        if (message.getChannelType().isGuild() && !user.isBot()) {
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
                        qm.addQuote(event.getGuild(), quote);
                        defaultChannel.sendMessage("Quote added!").queue();
                    }
                    break;
            }
        }
    }
}
