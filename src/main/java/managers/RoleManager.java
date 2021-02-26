package managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.HashMap;

public class RoleManager {

    // key, value = <"emote name", "emote id">
    private static final HashMap<String, String> ROLE_EMOTES = new HashMap<String, String>() {{
            put("rocketleague", "813556774555877387");
            put("rlssl", "");
            put("rlgc", "");
            put("rlchamp", "813556834110537789");
            put("rldiamond", "");
            put("rlplat", "");
            put("rlgold", "");
            put("rlsilver", "");
            put("rlbronze", "");
            put("r6seige", "");
            put("starcitizen", "");
            put("gtav", "");
            put("valorant", "");
            put("dev", "");
    }};

    public RoleManager() {
    }

    public boolean isInEmoteList(String emoteString) {
        return ROLE_EMOTES.containsKey(emoteString);
    }

    public void addToRoleFromEmote(String emoteString, Guild guild, Member member) {
        try {
            Role role = guild.getRoleById(ROLE_EMOTES.get(emoteString));
            guild.addRoleToMember(member, role).queue();
//            System.out.println(member.getUser().getName() + " added to " + role.getName() + " role.");
            member.getUser().openPrivateChannel().queue((channel) -> {
                channel.sendMessage("You were added to the " + role.getName() + " role!").queue();
            });
        } catch (NullPointerException ex) {
            System.err.println("Emote reaction does not point to a valid role.");
        }
    }

    public void removeFromRoleFromEmote(String emoteString, Guild guild, Member member) {
        try {
            Role role = guild.getRoleById(ROLE_EMOTES.get(emoteString));
            guild.removeRoleFromMember(member, role).queue();
//            System.out.println(member.getUser().getName() + " removed from " + role.getName() + " role.");
            member.getUser().openPrivateChannel().queue((channel) -> {
                channel.sendMessage("You were removed from the " + role.getName() + " role!").queue();
            });
        } catch (NullPointerException ex) {
            System.err.println("Emote reaction does not point to a valid role.");
        }
    }

}
