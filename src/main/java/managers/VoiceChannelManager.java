package managers;

import logger.ErrorLogger;
import net.dv8tion.jda.api.entities.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VoiceChannelManager {

    private static final String[] ADJECTIVES = new String[]{
            "Able",
            "Bad",
            "Best",
            "Better",
            "Big",
            "Black",
            "Certain",
            "Clear",
            "Different",
            "Depraved",
            "Early",
            "Easy",
            "Economic",
            "Federal",
            "Free",
            "Full",
            "Good",
            "Great",
            "Hard",
            "High",
            "Important",
            "International",
            "Large",
            "Late",
            "Little",
            "Local",
            "Long",
            "Low",
            "Major",
            "Military",
            "National",
            "New",
            "Old",
            "Only",
            "Other",
            "Political",
            "Possible",
            "Public",
            "Real",
            "Small",
            "Social",
            "Special",
            "Strong",
            "Sure",
            "True",
            "White",
            "Whole",
            "Young"
    };
    private static final String[] ANIMALS = new String[]{
            "Dog",
            "Puppy",
            "Turtle",
            "Rabbit",
            "Parrot",
            "Cat",
            "Kitten",
            "Goldfish",
            "Mouse",
            "Hamster",
            "Cow",
            "Duck",
            "Shrimp",
            "Pig",
            "Goat",
            "Crab",
            "Deer",
            "Bee",
            "Sheep",
            "Fish",
            "Turkey",
            "Dove",
            "Chicken",
            "Horse",
            "Crow",
            "Peacock",
            "Sparrow",
            "Goose",
            "Stork",
            "Pigeon",
            "Hawk",
            "Eagle",
            "Raven",
            "Flamingo",
            "Seagull",
            "Ostrich",
            "Swallow",
            "Black bird",
            "Penguin",
            "Robin",
            "Swan",
            "Owl",
            "Woodpecker",
            "Squirrel",
            "Chimpanzee",
            "Ox",
            "Lion",
            "Panda",
            "Walrus",
            "Otter",
            "Kangaroo",
            "Monkey",
            "Koala",
            "Mole",
            "Elephant",
            "Leopard",
            "Hippopotamus",
            "Giraffe",
            "Fox",
            "Coyote",
            "Hedgehog",
            "Camel",
            "Starfish",
            "Alligator",
            "Tiger",
            "Bear",
            "Raccoon",
            "Wolf",
            "Crocodile",
            "Dolphin",
            "Snake",
            "Elk",
            "Gorilla",
            "Bat",
            "Hare",
            "Toad",
            "Frog",
            "Rat",
            "Badger",
            "Lizard",
            "Reindeer",
            "Seal",
            "Octopus",
            "Shark",
            "Seahorse",
            "Whale",
            "Jellyfish",
            "Squid",
            "Lobster",
            "Pelican",
            "Clams",
            "Shells",
            "Sea urchin",
            "Cormorant",
            "Sea anemone",
            "Sea lion",
            "Moth",
            "Butterfly",
            "Spider",
            "Ant",
            "Dragonfly",
            "Fly",
            "Mosquito",
            "Grasshopper",
            "Beetle",
            "Cockroach",
            "Centipede",
            "Worm",
            "Louse",
            "Human"
    };
    public static final boolean CUSTOM_TITLE = true;
    public static final boolean STANDARD_TITLE = false;

    private ArrayList<VoiceChannel> voiceChannels;
    private HashMap<String, String> vcNameCache; // key, value = <"vc id", "generic name">
    private HashMap<String, Boolean> customTitleMap; // key, value = <"vc id", custom title>

    /**
     * Constructor
     */
    public VoiceChannelManager() {
        voiceChannels = new ArrayList<>(); // contains vc created by on demand
        vcNameCache = new HashMap<>();
        customTitleMap = new HashMap<>();
    }

    /**
     * Get random adjective
     *
     * @return String from ADJECTIVES array.
     */
    private String getAdjective() {
        int index = (int) (Math.random() * (ADJECTIVES.length - 1));
        return ADJECTIVES[index];
    }

    private String getAnimal() {
        int index = (int) (Math.random() * (ANIMALS.length - 1));
        return ANIMALS[index];
    }

    public VoiceChannel getVoiceChannel(int index) {
        return voiceChannels.get(index);
    }

    public void addVoiceChannel(VoiceChannel vc, String genericName) {
        voiceChannels.add(vc);
        vcNameCache.put(vc.getId(), genericName); // remember randomly generated voice name in case ppl stop playing a game
        customTitleMap.put(vc.getId(), STANDARD_TITLE);
        System.out.println("\"" + vc.getName() + "\" created.");
        System.out.println("vcNameCache current state: " + vcNameCache);
    }

    public void removeVoiceChannel(VoiceChannel vc) {
        voiceChannels.remove(vc);
        vcNameCache.remove(vc.getId());
        customTitleMap.remove(vc.getId());
        System.out.println("\"" + vc.getName() + "\" removed.");
        System.out.println("vcNameCache current state: " + vcNameCache);
    }

    public int getNumberOfVoiceChannels() {
        return voiceChannels.size();
    }

    public boolean isCreatedVoiceChannel(VoiceChannel vc) {
        return voiceChannels.contains(vc);
    }

    public void createVoiceChannel(Member member, Category category) {
        Guild guild = member.getGuild();
        String genericName = getAdjective() + " " + getAnimal();

        /*
        get user activity.  prioritize channel name to default type
         */
        Activity primaryActivity = getPrimaryMemberActivity(member);
        String vcName = (primaryActivity == null) ? genericName : primaryActivity.getName();

        guild.createVoiceChannel(vcName, category).queue(voiceChannel -> {
            addVoiceChannel(voiceChannel, genericName);
            guild.moveVoiceMember(member, guild.getVoiceChannelById(voiceChannel.getId())).queue();
        });
    }

    private Activity getPrimaryMemberActivity(Member member) {
        List<Activity> activities = member.getActivities();
        Activity primaryActivity = null;

        for (Activity activity : activities) {
            if (activity.getType().equals(Activity.ActivityType.LISTENING)) {
                primaryActivity = activity;
            }
            if (activity.getType().equals(Activity.ActivityType.DEFAULT)) {
                primaryActivity = activity;
                return primaryActivity;
            }
        }

        return primaryActivity; // returns null if no activities
    }

    public Activity getPrimaryVoiceChannelActivity(VoiceChannel voiceChannel) {
        int maxPlayers = 0;
        Activity primaryActivity = null;
        boolean isPlaying = false;

        // get list of members from vc
        List<Member> members = voiceChannel.getMembers();
        // get list of activities for each member and the number of members playing the activity
        // create hashmap of activities and popularity, prioritizing default activities
        HashMap<Activity, Integer> activityMap = new HashMap<>();
        for (Member member : members) {
            List<Activity> memberActivities = member.getActivities();
            for (Activity activity : memberActivities) {
                // if activity is default, set isPlaying to true and update hashmap accordingly.
                if (activity.getType().equals(Activity.ActivityType.DEFAULT)) {
                    isPlaying = true;
                    if (activityMap.containsKey(activity)) { // if we've seen this activity before, update value
                        int value = activityMap.get(activity);
                        activityMap.replace(activity, ++value);
                    } else { // otherwise add it
                        activityMap.put(activity, 1);
                    }

                    int value = activityMap.get(activity);
                    if (value > maxPlayers) {
                        maxPlayers = value;
                        primaryActivity = activity;
                    }
                }
                // if activity is listening and there is no default activity in map, update hashmap accordingly
                if (activity.getType().equals(Activity.ActivityType.LISTENING) && !isPlaying) {
                    if (activityMap.containsKey(activity)) { // if we've seen this activity before, update value
                        int value = activityMap.get(activity);
                        activityMap.replace(activity, ++value);
                    } else { // otherwise add it
                        activityMap.put(activity, 1);
                    }

                    int value = activityMap.get(activity);
                    if (value > maxPlayers) {
                        maxPlayers = value;
                        primaryActivity = activity;
                    }
                }
            }
        }
        System.out.println("Activities Map for VC " + voiceChannel.getId() + ": " + activityMap);

        return primaryActivity;
    }

    public void setCustomChannelName(VoiceChannel vc, String name) {
        customTitleMap.replace(vc.getId(), CUSTOM_TITLE);
        // if vc is not named correctly, name it correctly
        if (!vc.getName().equals(name)) {
            vc.getManager().setName(name).queue();
            System.out.println("voice channel name changed");
        }
    }

    public void setStandardChannelName(VoiceChannel vc) {
        customTitleMap.replace(vc.getId(), STANDARD_TITLE);
        setVoiceChannelName(vc);
    }

    public void setVoiceChannelName(VoiceChannel vc) {
        Activity primaryActivity = getPrimaryVoiceChannelActivity(vc);
        String gameActivity = null;
        if (customTitleMap.get(vc.getId()) == STANDARD_TITLE) {
            gameActivity = (primaryActivity == null) ? null : primaryActivity.getName();
        } else {
            gameActivity = vc.getName();
        }

        // if vc is not named correctly, name it correctly
        if (!vc.getName().equals(gameActivity)) {
            if (gameActivity != null) {
                vc.getManager().setName(gameActivity).queue();
            } else {
                // if there is no game being played, set to original random name
                vc.getManager().setName(vcNameCache.get(vc.getId())).queue();
            }
            System.out.println("voice channel name changed");
        }
    }
}