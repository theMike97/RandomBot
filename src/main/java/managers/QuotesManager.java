package managers;


import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;

import com.amazonaws.services.dynamodbv2.model.*;

import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class QuotesManager {

    private final AmazonDynamoDB client;
    private final DynamoDB dynamoDB;

    public QuotesManager(ProfileCredentialsProvider provider) {

        client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(provider)
                .withRegion("us-east-2")
                .build();

        dynamoDB = new DynamoDB(client);
    }

    public void addQuote(Guild guild, String[] quoteData) {
        // send quote message to quotes channel
        System.out.println("Adding quote...");

        dynamoDB.getTable("Quotes").putItem(new Item()
                .withPrimaryKey("GuildID", guild.getId(), "QuoteID", UUID.randomUUID().toString())
                .withString("QuoteText", quoteData[0])
                .withString("Author", quoteData[1]));
    }

    public String[] getRandomQuote(Guild guild) {
        String[] quoteData = new String[2];
        // just gonna get a specific quote from the guild.
        String guildId = guild.getId();

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":id", new AttributeValue().withS(guildId));

        ScanRequest request = new ScanRequest().withTableName("Quotes")
                .withFilterExpression("GuildID = :id")
                .withExpressionAttributeValues(expressionAttributeValues);

        ScanResult result = client.scan(request);
        int randomIndex = (int) (Math.random() * result.getCount());
        Map<String, AttributeValue> randomQuote = result.getItems().get(randomIndex);

        quoteData[0] = randomQuote.get("QuoteText").getS();
        quoteData[1] = randomQuote.get("Author").getS();

        return quoteData;
    }

}