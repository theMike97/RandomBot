# RandomBot
## Overview
This is a simple discord bot that manages voice channels, roles, and has a few minigames.  Ideally when this bot is finished, it will back up important data for each guild like role/emote combinations, reaction message ids, quotes from the quotes minigame, RL mafia stants/standings, etc.

### Voice Channel Management
- Features an "on demand" voice channel system where users join a single on-demand channel and the bot creates a new voice channel (VC) and moves the user to the new VC.
- The bot also names the VC according to the most played activity, prioritizing games (what are the users playing) over other activities like listening or watching.
- A user can also set a static custom VC name.
- When all users leave a created VC, the bot deletes it.

### Reaction/Role Assignments
- Assigns roles based on emotes reacting to a specific message.

### Minigames
#### Quotes
- Users can add a quote with the `!quote [qote]` command.
- Users can pull a random quote with the `!quote` command.

#### Rocket League Mafia
##### How it Works:
- Users can queue a mafia game with `!q`.
- Users start the game with `!start` at which point the bot will stop accepting players.
- The bot will ask how team should be made.  Players can choose auto-assigned teams, or manually-assigned teams.
- If the teams are auto-assigned, the bot will randomly assembly teams.  Otherwise, users will react with the appropriate emote/emoji to choose a team.
- The bot randomly chooses a queued player to be mafia and DMs players their role: Villager or Mafia.
- After the game is over, one of the players will record if they won or lost the game.
- The bot will then request votes for the mafia.  Players will vote by emote/emoji.
- Once all players have voted, the bot will reveal the mafia and award points.
##### Player Rules:
Only one player is chosen to be the mafia.  It is the mafia's job to throw the game as discretely as possible.  The mafia should not reveal himself.  Discussion is allowed and encouraged during the game.
##### Points rules:
I'll go back and look at this later.  i cant remember what I decided was a balanced points system.
