package managers;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import logger.ErrorLogger;
import net.dv8tion.jda.api.entities.*;

import java.net.URI;
import java.util.*;

public class QuotesManager {

    private String quotesChannelId;
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private Table quotesTable;
    private Integer quoteCount;
    private Guild guild;

    public QuotesManager() {
        ProfileCredentialsProvider provider = new ProfileCredentialsProvider();
        try {
            provider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Could not fetch credentials.");
        }

        client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(provider)
                .withRegion("us-east-2")
                .build();

        dynamoDB = new DynamoDB(client);
        quotesTable = dynamoDB.getTable("Quotes");
        quoteCount = null;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
        if (quoteCount == null) {
            quoteCount = getQuoteCount();
        }
    }

    public void addQuote(String[] quoteData) throws Exception {
        // send quote message to quotes channel
        System.out.println("Adding quote...");

        quotesTable.putItem(new Item()
                .withPrimaryKey("GuildID", guild.getId(), "QuoteID", quoteCount++)
                .withString("QuoteText", quoteData[0])
                .withString("Author", quoteData[1]));
    }

    private Integer getQuoteCount() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":id", new AttributeValue().withS(guild.getId()));

        ScanRequest request = new ScanRequest().withTableName("Quotes")
                .withFilterExpression("GuildID = :id")
                .withExpressionAttributeValues(expressionAttributeValues);

        ScanResult result = client.scan(request);
        return result.getCount();
    }

    public String[] getRandomQuote() {
        String[] quoteData = new String[2];
        // just gonna get a specific quote from the guild.
        String guildId = guild.getId();

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":id", new AttributeValue().withS(guildId));

        ScanRequest request = new ScanRequest().withTableName("Quotes")
                .withFilterExpression("GuildID = :id")
                .withExpressionAttributeValues(expressionAttributeValues);

        ScanResult result = client.scan(request);
        int randomIndex = (int) (Math.random() * quoteCount);
        Map<String, AttributeValue> randomQuote = result.getItems().get(randomIndex);

        quoteData[0] = randomQuote.get("QuoteText").getS();
        quoteData[1] = randomQuote.get("Author").getS();

        return quoteData;
    }

}
