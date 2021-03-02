package listeners;

import managers.RoleManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

    private final RoleManager rm;

    public ReactionListener(RoleManager roleManager) {
        rm = roleManager;
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Message reactedMessage = event.retrieveMessage().complete();
        Member member = event.getMember();
        Guild guild = event.getGuild();
        String reactionEmoteString;

        if (event.getReactionEmote().isEmote()) {
            reactionEmoteString = event.getReactionEmote().getEmote().getId();
        } else {
            reactionEmoteString = event.getReactionEmote().getAsCodepoints();
        }

        rm.syncReactionMessageTable(guild); // will sync if messageId variable is null
        if (rm.getRoleAssignmentMessageId(guild) != null) {

            if (reactedMessage.getId().equals(rm.getRoleAssignmentMessageId(guild))) {

                rm.syncRoleReactionEmotesTable(guild); // will sync if roleEmotes is null
                if (rm.isInEmoteList(guild, reactionEmoteString)) {

                    rm.addToRoleFromEmote(reactionEmoteString, member);
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        Message reactedMessage = event.retrieveMessage().complete();
        Member member = event.getMember();
        String reactionEmoteString;

        if (event.getReactionEmote().isEmote()) {
            reactionEmoteString = event.getReactionEmote().getEmote().getId();
        } else {
            reactionEmoteString = event.getReactionEmote().getAsCodepoints();
        }

        rm.syncReactionMessageTable(event.getGuild()); // will sync if messageId variable is null
        if (rm.getRoleAssignmentMessageId(event.getGuild()) != null) {

            if (reactedMessage.getId().equals(rm.getRoleAssignmentMessageId(event.getGuild()))) {

                rm.syncRoleReactionEmotesTable(event.getGuild()); // will sync if roleEmotes is null
                if (rm.isInEmoteList(event.getGuild(), reactionEmoteString)) {

                    rm.removeFromRoleFromEmote(reactionEmoteString, member);
                }
            }
        }
    }

}
