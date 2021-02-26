package managers;

import logger.ErrorLogger;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class QuotesManager {

    String quotesChannelId;

    public QuotesManager() {
        quotesChannelId = "814203718554877962";
    }

    public void addQuote(Guild guild, String quote) {
        // send quote message to quotes channel
        TextChannel quotesChannel = guild.getTextChannelById(quotesChannelId);
        quotesChannel.sendMessage(quote).queue();
    }

    public String getRandomQuote(Guild guild) {
        // iterate through quotes channel message history and pull a random one out
        TextChannel quotesChannel = guild.getTextChannelById(quotesChannelId);
//        System.out.println(quotesChannel.getName());
        MessageHistory history = quotesChannel.getHistory();
        List<Message> quotesList = history.getRetrievedHistory();
        System.out.println(quotesList);
        int quotesSize = history.size();
        System.out.println(quotesSize);

        int index = (int) (Math.random() * (quotesSize - 1));
        String quote = (history.isEmpty()) ? "No quotes stored!" : quotesList.get(index).getContentRaw();

        return quote;
    }

}
