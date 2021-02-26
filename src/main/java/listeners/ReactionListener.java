package listeners;

import managers.RoleManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

    private RoleManager rm;
    private final String ROLE_ASSIGN_MESSAGE_ID = "813509499578613780";

    public ReactionListener(RoleManager roleManager) {
        rm = roleManager;
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Message reactedMessage = event.retrieveMessage().complete();
        Member member = event.getMember();
        String reactionEmoteString;

        if (event.getReactionEmote().isEmote()) {
            reactionEmoteString = event.getReactionEmote().getEmote().getName();
        } else {
            reactionEmoteString = event.getReactionEmote().getAsCodepoints();
        }

        if (reactedMessage.getId().equals(ROLE_ASSIGN_MESSAGE_ID)) {
            if (rm.isInEmoteList(reactionEmoteString)) {
                rm.addToRoleFromEmote(reactionEmoteString, event.getGuild(), member);
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        Message reactedMessage = event.retrieveMessage().complete();
        Member member = event.getMember();
        String reactionEmoteString;

        if (event.getReactionEmote().isEmote()) {
            reactionEmoteString = event.getReactionEmote().getEmote().getName();
        } else {
            reactionEmoteString = event.getReactionEmote().getAsCodepoints();
        }

        if (reactedMessage.getId().equals(ROLE_ASSIGN_MESSAGE_ID)) {
            if (rm.isInEmoteList(reactionEmoteString)) {
                rm.removeFromRoleFromEmote(reactionEmoteString, event.getGuild(), member);
            }
        }

    }

}
