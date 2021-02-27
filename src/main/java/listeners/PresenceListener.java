package listeners;

import managers.VoiceChannelManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PresenceListener extends ListenerAdapter {


    private VoiceChannelManager vcm;

    public PresenceListener(VoiceChannelManager vcm) {
        this.vcm = vcm;
    }

    @Override
    public void onGenericUserPresence(GenericUserPresenceEvent event) {
//        System.out.println("user changed presence");
        Member member = event.getMember();

        // get vc member is in
        if (member.getVoiceState() == null) return;
        VoiceChannel channel = member.getVoiceState().getChannel();

        // update vc name information
        if (vcm.isCreatedVoiceChannel(channel)) {
            vcm.setVoiceChannelName(channel);
        }

    }

}
