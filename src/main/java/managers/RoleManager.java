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
     * Sets message id variable and inserts/updates the reaction message id for the guild
     * in the database.
     *
     * @param guild     The guild for which the message id is set
     * @param messageId The message id
     */
    public void setRoleAssignmentMessage(Guild guild, String messageId) {
        roleAssignmentMessageId = messageId;
        Table reactionMessageTable = dynamoDB.getTable("ReactionMessage");
        reactionMessageTable.putItem(new Item()
                .withPrimaryKey("GuildID", guild.getId())
                .withString("ReactionMessageID", messageId));
    }

    /**
     * Gets message Id for the reaction message for the specified guild.
     *
     * @return  Reaction message Id
     */
    public String getRoleAssignmentMessageId() {
        return roleAssignmentMessageId;
    }

    /**
     * Create a link between a specified role and emote/emoji.  Adds this data to a <a href="#{@link}">{@link HashMap}</a>
     * with a key-value pair (emoteString, role) and inserts/updates a row in the database
     * identified by the guild id and the emoteString.  The emoteString should be either
     * the emote ID, or the emoji codepoints - whichever is applicable.
     *
     * @param role          The role that should be associated with the emoteString
     * @param emoteString   A string that identifies and emote/emoji
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
     * Remove a link between a specified role and emote/emoji.  Removes this data from a <a href="#{@link}">{@link HashMap}</a>
     * with a key-value pair (emoteString, role) and deletes a row in the database
     * identified by the guild id and the emoteString.  The emoteString should be either
     * the emote ID, or the emoji codepoints - whichever is applicable.
     *
     * @param role          The role that should be associated with the emoteString
     * @param emoteString   A string that identifies and emote/emoji
     */
    public void removeRoleEmoteLink(Role role, String emoteString) {
        Guild guild = role.getGuild();
        roleEmotes.remove(emoteString, role);

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
     * @param emoteString
     * @return
     */
    public boolean isInEmoteList(String emoteString) {
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

    // if messageId is null, pull data from table
    public void syncReactionMessageTable(Guild guild) {
        if (roleAssignmentMessageId == null) {
            Table reactionMessageTable = dynamoDB.getTable("ReactionMessage");
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put(":guildId", guild.getId());

            try {
                ItemCollection<QueryOutcome> items = reactionMessageTable.query(new QuerySpec().withKeyConditionExpression("GuildID = :guildId")
                        .withValueMap(valueMap));
                for (Item item : items) {
                    roleAssignmentMessageId = item.getString("ReactionMessageID");
                    System.out.println("Retrieved message ID: " + roleAssignmentMessageId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // if roleEmotes is empty, pull data from table.
    public void syncRoleReactionEmotesTable(Guild guild) {
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
    }

}
