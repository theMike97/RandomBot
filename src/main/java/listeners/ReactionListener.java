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
        String reactionEmoteString;

        if (event.getReactionEmote().isEmote()) {
            reactionEmoteString = event.getReactionEmote().getEmote().getId();
        } else {
            reactionEmoteString = event.getReactionEmote().getAsCodepoints();
        }

        if (rm.getRoleAssignmentMessageId(event.getGuild()) != null) {
            if (reactedMessage.getId().equals(rm.getRoleAssignmentMessageId(event.getGuild()))) {
                if (rm.isInEmoteList(event.getGuild(), reactionEmoteString)) {
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

        if (reactedMessage.getId().equals(rm.getRoleAssignmentMessageId(event.getGuild()))) {
            if (rm.isInEmoteList(event.getGuild(), reactionEmoteString)) {
                rm.removeFromRoleFromEmote(reactionEmoteString, member);
            }
        }

    }

}
