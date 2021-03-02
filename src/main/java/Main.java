import listeners.CommandListener;
import listeners.PresenceListener;
import listeners.ReactionListener;
import listeners.VoiceChannelListener;
import logger.ErrorLogger;
import managers.QuotesManager;
import managers.RoleManager;
import managers.VoiceChannelManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class Main {

    /*
    permissions needed: (1358974032)
    move members
    view channels
    manage roles
    manage channels
    send messages
    embed links
    add reactions
    manage emojis
     */

    public static void main(String[] args) throws LoginException {
        JDABuilder jda = JDABuilder.createDefault(Secrets.TOKEN);
        jda.setActivity(Activity.playing("Type !help for commands"));

        VoiceChannelManager voiceChannelManager = new VoiceChannelManager();
        RoleManager roleManager = new RoleManager();
        QuotesManager quotesManager = new QuotesManager();

        jda.enableIntents(GatewayIntent.GUILD_MESSAGES);
        jda.enableIntents(GatewayIntent.GUILD_PRESENCES);
        jda.enableCache(CacheFlag.ACTIVITY);
        jda.enableCache(CacheFlag.VOICE_STATE);

        jda.addEventListeners(new CommandListener(quotesManager, voiceChannelManager));
        jda.addEventListeners(new ReactionListener(roleManager));
        jda.addEventListeners(new VoiceChannelListener(voiceChannelManager));
        jda.addEventListeners(new PresenceListener(voiceChannelManager));

        jda.build();
    }

}
