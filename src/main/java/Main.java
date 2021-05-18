import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import listeners.CommandListener;
import listeners.PresenceListener;
import listeners.ReactionListener;
import listeners.VoiceChannelListener;
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
//        JDABuilder jdaBuilder = JDABuilder.createDefault(Secrets.OFFICIAL_TOKEN);
        JDABuilder jdaBuilder = JDABuilder.createDefault(Secrets.BETA_TOKEN);
        jdaBuilder.setActivity(Activity.playing("Type !help for commands"));

        ProfileCredentialsProvider provider = new ProfileCredentialsProvider();
        try {
            provider.getCredentials();
        } catch (Exception e) {
            jdaBuilder.setActivity(Activity.playing("Not connected to Database - See exception:"));
            throw new AmazonClientException("Could not fetch credentials.");
        }

        VoiceChannelManager voiceChannelManager = new VoiceChannelManager();
        RoleManager roleManager = new RoleManager(provider);
        QuotesManager quotesManager = new QuotesManager(provider);

        jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES);
        jdaBuilder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        jdaBuilder.enableCache(CacheFlag.ACTIVITY);
        jdaBuilder.enableCache(CacheFlag.VOICE_STATE);

        jdaBuilder.addEventListeners(new CommandListener(quotesManager, voiceChannelManager, roleManager));
        jdaBuilder.addEventListeners(new ReactionListener(roleManager));
        jdaBuilder.addEventListeners(new VoiceChannelListener(voiceChannelManager));
        jdaBuilder.addEventListeners(new PresenceListener(voiceChannelManager));

        jdaBuilder.build();
    }
}
