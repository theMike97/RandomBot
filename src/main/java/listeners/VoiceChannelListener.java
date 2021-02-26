package listeners;

import managers.VoiceChannelManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceChannelListener extends ListenerAdapter {

    private static final String ON_DEMAND = "on demand";
    VoiceChannelManager vcm;

    public VoiceChannelListener(VoiceChannelManager voiceChannelManager) {
        vcm = voiceChannelManager;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Member member = event.getMember();
        VoiceChannel vc = event.getChannelJoined();

        // this is an on demand vc and we should do something
        if (vc.getName().toLowerCase().contains(ON_DEMAND)) {
            vcm.createVoiceChannel(member, vc.getParent());
        }

        // if the joined channel is created by the on demand channel
        if (vcm.isCreatedVoiceChannel(vc)) {
            vcm.setVoiceChannelName(vc);
        }

    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Member member = event.getMember();
        VoiceChannel leftVoiceChannel = event.getChannelLeft();

//        System.out.println(member.getUser().getName() + " left " + leftVoiceChannel.getName());

        // if the left vc is a created on demand channel
        if (vcm.isCreatedVoiceChannel(leftVoiceChannel)) {

            // if channel has 0 members, delete it
            if (leftVoiceChannel.getMembers().size() == 0) {
                vcm.removeVoiceChannel(leftVoiceChannel);
                leftVoiceChannel.delete().queue();
            } else {
                // otherwise rename it
                vcm.setVoiceChannelName(leftVoiceChannel);
            }
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Member member = event.getMember();

        VoiceChannel joinedVoiceChannel = event.getChannelJoined();
        VoiceChannel leftVoiceChannel = event.getChannelLeft();

        // this is an on demand vc and we should do something
        if (joinedVoiceChannel.getName().toLowerCase().contains(ON_DEMAND)) {
            vcm.createVoiceChannel(member, joinedVoiceChannel.getParent());
        }

        // if the joined channel is created by the on demand channel
        if (vcm.isCreatedVoiceChannel(joinedVoiceChannel)) {
//            System.out.println("user joined created vc");
            vcm.setVoiceChannelName(joinedVoiceChannel);
        }

        // if the left vc is a created on demand channel
        if (vcm.isCreatedVoiceChannel(leftVoiceChannel)) {

            // if channel has 0 members, delete it
            if (leftVoiceChannel.getMembers().size() == 0) {
                vcm.removeVoiceChannel(leftVoiceChannel);
                leftVoiceChannel.delete().queue();
            } else {
                // otherwise rename it
                vcm.setVoiceChannelName(leftVoiceChannel);
            }
        }
    }

}