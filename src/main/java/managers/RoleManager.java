package managers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.HashMap;
import java.util.Map;

public class RoleManager {

    // <key, value> = <emoteId, role>
    private HashMap<String, Role> roleEmotes;

    private String roleAssignmentMessageId;
    private final AmazonDynamoDB client;
    private final DynamoDB dynamoDB;

    public RoleManager(ProfileCredentialsProvider provider) {
        roleAssignmentMessageId = null;
        roleEmotes = new HashMap<>();

        client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(provider)
                .withRegion("us-east-2")
                .build();

        dynamoDB = new DynamoDB(client);
    }

    /**
     *
     * @param guild
     * @param messageId
     */
    public void setRoleAssignmentMessage(Guild guild, String messageId) {
        roleAssignmentMessageId = messageId;
        Table reactionMessageTable = dynamoDB.getTable("ReactionMessage");
        reactionMessageTable.putItem(new Item()
                .withPrimaryKey("GuildID", guild.getId())
                .withString("ReactionMessageID", messageId));
    }

    /**
     * 
     * @param guild
     * @return
     */
    public String getRoleAssignmentMessageId(Guild guild) {
        // this code takes care of the event that the global message id variable is reset to null,
        // but the database has the message id for the guild stored.
        if (roleAssignmentMessageId == null) {
            roleAssignmentMessageId = getRoleAssignmentMessageIdFromDB(guild);
        }
        return roleAssignmentMessageId;
    }

    private String getRoleAssignmentMessageIdFromDB(Guild guild) {
        Table reactionMessageTable = dynamoDB.getTable("ReactionMessage");
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(":guildId", guild.getId());

        String messageId = null;
        try {
            ItemCollection<QueryOutcome> items = reactionMessageTable.query(new QuerySpec().withKeyConditionExpression("GuildID = :guildId")
                    .withValueMap(valueMap));
            for (Item item : items) {
                messageId = item.getString("ReactionMessageID");
                System.out.println("Retrieved message ID: " + messageId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageId;
    }

    /**
     *
     * @param role
     * @param emoteString
     */
    public void addRoleEmoteLink(Role role, String emoteString) {
        Guild guild = role.getGuild();
        roleEmotes.put(emoteString, role);

        Table reactionEmotesTable = dynamoDB.getTable("RoleReactionEmotes");
        reactionEmotesTable.putItem(new Item()
                .withPrimaryKey("GuildID", guild.getId(), "EmoteID", emoteString)
                .withString("RoleID", role.getId()));
        System.out.println("Added (GuildId, EmoteId, RoleName): ("
                + guild.getId() + ", "
                + emoteString + ", "
                + role.getName() + ")");
    }

    /**
     *
     * @param role
     * @param emoteString
     */
    public void removeRoleEmoteLink(Role role, String emoteString) {
        Guild guild = role.getGuild();
        roleEmotes.remove(emoteString);

        Table reactionEmotesTable = dynamoDB.getTable("RoleReactionEmotes");
        try {
            reactionEmotesTable.deleteItem(new DeleteItemSpec()
                    .withPrimaryKey(new PrimaryKey("GuildID", guild.getId(), "EmoteID", emoteString)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param guild
     * @param emoteString
     * @return
     */
    public boolean isInEmoteList(Guild guild, String emoteString) {
        // populate roleEmotes from db if it is empty
        if (roleEmotes.isEmpty()) {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":id", new AttributeValue().withS(guild.getId()));
            try {
                ScanResult result = client.scan(new ScanRequest().withTableName("RoleReactionEmotes")
                        .withFilterExpression("GuildID = :id")
                        .withExpressionAttributeValues(expressionAttributeValues));

                for (Map<String, AttributeValue> row : result.getItems()) {
                    roleEmotes.put(row.get("EmoteID").getS(), guild.getRoleById(row.get("RoleID").getS()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return roleEmotes.containsKey(emoteString);
    }

    /**
     *
     * @param emoteString
     * @param member
     */
    public void addToRoleFromEmote(String emoteString, Member member) {
        Guild guild = member.getGuild();
        try {
            Role role = roleEmotes.get(emoteString);
            guild.addRoleToMember(member, role).queue();
//            System.out.println(member.getUser().getName() + " added to " + role.getName() + " role.");
            member.getUser().openPrivateChannel().queue((channel) ->
                    channel.sendMessage("You were added to the " + role.getName() + " role!"
                    ).queue());
        } catch (NullPointerException ex) {
            System.err.println("Emote reaction does not point to a valid role.");
        }
    }

    /**
     *
     * @param emoteString
     * @param member
     */
    public void removeFromRoleFromEmote(String emoteString, Member member) {
        Guild guild = member.getGuild();
        try {
            Role role = roleEmotes.get(emoteString);
            guild.removeRoleFromMember(member, role).queue();
//            System.out.println(member.getUser().getName() + " removed from " + role.getName() + " role.");
            member.getUser().openPrivateChannel().queue((channel) ->
                    channel.sendMessage("You were removed from the " + role.getName() + " role!"
                    ).queue());
        } catch (NullPointerException ex) {
            System.err.println("Emote reaction does not point to a valid role.");
        }
    }

}
