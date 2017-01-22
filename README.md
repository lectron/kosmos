#Kosmos

Minecraft multi-world, orchestrated by multi-server. Interact as one universe.

The light weight, distributed layer for Minecraft. It allows multiple servers to orchestrate player interactions across multiple worlds as if it's one universe. Currently these supports BungeeCord and Spigot. Kosmos supports multi-server setup and is designed for high availability.

![Image of a Minecraft world](https://i.imgur.com/sjtYfym.png)

The ultimate goal with Kosmos is to be able to implement high availability and cross-server interactions without dividing player experience, which traditional Minecraft softwares do not inherently provide that functionality. The implementation is efficient, not complex, and highly performant.

##Links
Minecraftly server address: **m.ly**

[Website](http://m.ly)

[Developer Wiki](https://github.com/minecraftly/kosmos/wiki)

[Current Main Branch - v2](https://github.com/minecraftly/kosmos/tree/v2)

[General Plugin Rules](https://github.com/minecraftly/kosmos/wiki/Plugin-Rules) (Important Read)

[Make money via our bounty Program](https://www.bountysource.com/teams/minecraftly)

We empower and compensate the community for helping us fix bugs and add new features.

---

##REQUIREMENTS
 * [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/): serve as a proxy server (equivalent to Nginx or HAProxy in web hosting)
 * [Spigot](https://www.spigotmc.org/wiki/spigot/): serve as Minecraft server.
   - Alternative: PaperSpigot is a derivative of Spigot, giving a little higher performance than traditional Spigot.
 * [Redis server](https://redis.io/): Real time volatile database that connects BungeeCord and Spigot together.
 * (optional) shared file system for multiple Spigot servers to access the same /worlds folder
 * (optional) Dank memes
 
##SETUP
  See setup example [here](https://github.com/minecraftly/kosmos/wiki/Setup)

##WORK IN PROGRESS
- Player joins via {their_username}.{domain}.{tld}:
  - [x] if joins for the first time, server creates the world
  - [x] world has the folder format name {uuid}
  - [x] it contains world contents like usual (world, world_nether, world_the_end, session.lock, etc...)
  - [x] only owner of the world can create the world
  - [x] other players can only load the world if available.
  - [x] worlds are stored in the pre defined folder ~/minecraft/worlds/{uuid}
  - [x] ~/minecraft/worlds folder is shared across all spigot servers
  - [x] player data will be in the common folder ~/minecraft/playerdata, shared across all spigot servers
  - [x] achievement stats will be in the common folder ~/minecraft/stats, shared across all spigot servers
  - [x] works with {domain}.{tld} or simply the proxy IP address
  - [x] default game mode for world owner is SURVIVAL
  - [x] Default game rule changes for all worlds:
    - [x] keepInventory: true
    - [x] mobGriefing: false
- Player joins via {another_player_username}.m.ly
  - [x] proxy checks player's UUID
  - [x] check if the world's UUID is already loaded on any server
  - [x] proxy brings player to the server with world already loaded
  - [x] else, brings player to any server and load world via that server
  - [x] Default game mode for visitor of a world is ADVENTURE
    - [ ] Players with ADVENTURE mode can't open chests
- Trust a player to build in your world
  - [x] Owner of world can trust others to build
  - [x] The trusted player will have game mode changed from ADVENTURE to SURVIVAL for that world only.
  - [x] Trust command: /trust {username_you_want_to_trust}
  - [x] Trust list command: /trustlist
  - [x] Untrust command: untrust {username_you_want_to_untrust}
- [ ] Location handling
  - [ ] If player joins via IP address or normal {domain}.{tld} -> brings player to previous logout location on previous world
  - [ ] If player joins explicitly via {subdomain}.{domain}.{tld} -> bring player to previous logout location of that specific world
- Spawn point
  - [x] /setspawn: Set your world's spawn at your standing location in your world
    - [x] Everyone who joins your world for the first time, including you, will spawn here
    - [ ] If you die, you will spawn back here if you don't set a home location
    - If anyone else die in your world, they will spawn back here
  - [x] /spawn: Go back to your spawn point instantly
    - [x] Everyone, including you, in your world who types this command will teleport to the spawn point of your world.
- Home location
  - [x] /sethome: Set your home location at your standing location in your world
    - [x] Only you are able to set home in your world
    - [ ] When you die, you will spawn back here
  - [x] /home
    - [x] You go to your home location
    - [x] You can use this command when you are on any world
    - [x] If using this command while playing in another world, check if the world's UUID is already loaded on any server
- [ ] Whitelist
  - [x] Whitelist your world
  - [x] Turn on whitelist: /whitelist on
  - [x] Add players to your whitelist: /whitelist add {username}
  - [x] List the players who are in your whitelist: /whitelist list
  - [x] Turn off whitelist: /whitelist off
- [x] Ban
  - [x] Ban players from ever playing in your world
  - [x] Ban a player: /ban {username}
  - [x] Show your ban list: /banlist
  - [x] Unban a player: /unban {username}
  - [x] Upon being banned, the player will be teleported back to their world
-  [ ] Teleport to a player
  - [ ] Teleport to any player
  - [ ] Send teleport request so you can teleport to the other player: /tpa {username}
  - [ ] Send teleport request so the other player teleport to you: /tpahere {username}
  - [ ] Accept a teleport request: /tpaccept
  - [ ] Deny a teleport request: /tpdeny
  - [ ] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [ ] If you are banned from that world, don't teleport
- [x] Teleport to a world
  - [x] Teleport yourself to any world of any player
  - [x] Command: /world {username}
  - [x] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [x] If you are banned from that world, don't teleport
- [ ] Teleport to a player
  - [ ] Teleport to any player
  - [ ] Send teleport request so you can teleport to the other player: /tpa {username}
  - [ ] Send teleport request so the other player teleport to you: /tpahere {username}
  - [ ] Accept a teleport request: /tpaccept
  - [ ] Deny a teleport request: /tpdeny
  - [ ] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [ ] If you are banned from that world, don't teleport
- [x] Separation of server message
  - [x] Server messages that are shown per server will now be shown per world to reduce spammy, irrelevant messages
  - [x] join message
  - [x] leave message
  - [x] quit message
  - [x] kick message
- [ ] Mute
  - [ ] Mute annoying player from talking in your world
  - [ ] /mute {username}
  - [ ] /mutelist
  - [ ] /unmute {username}
- [x] Kick
  - [x] Kick a player from playing in your world
  - [x] /kick {username}
  - [ ] Upon being kicked, the player will be teleported back to their world
- [ ] Reset
  - [ ] Reset your world, world_nether, world_the_end, and dat file
  - [ ] Doesn't reset your playerdata or achievement stats
  - [ ] Command: /reset
  - [ ] Type /reset again to confirm
- [ ] Back
  - [ ] Command: /back
  - [ ] You can go back to your death location on the world you are in.
- [x] List players currently playing on the world you are in
  - [x] Command: /list
- [x] CNAME
  - [x] Peole can use their custom domain to map into official {their_username}.{domain}.{tld}
  - Example: We setup play.minecraftly.com can set a CNAME record to 696f8b26-c97b-4e0c-b8f2-a1938d469701.m.ly & everyone who joins play.minecraftly.com will be in that player "minecraftly" world right away.

---

#Redis key-value store database structure
Redis key-value store is for use for queueing, bringing players to the right place at the right time, whether it's join, teleport, or other related events.

uuid (table)
- For UUID caching
- Because Mojang has an API limit of only 10 per minute.

 | uuid  | username
 | --- | --- | --- |
 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | AsianGuy_Gamer |
 | c088272e-a8ca-496c-91a2-b7394ffe879c | ImRainbowActive |
 | cf1f1ea8-4bc9-4cba-886c-33997403eb80 | AruAkise_ |
 | ... | ... | ... |

world (table)
- Showing which world is currently loaded by which server.
- If a world is already/currently loaded on one server, subsequent player who joins via subdomain will go to correct server.
- This will prevent world being loaded twice on many different spigot servers
- IP and port columns will use Spigot's server.properties' server-ip and server-port values

 | world  | address |
 | --- | --- |
 | 00ceaed3-3715-49e9-b45f-0e01cf94f798 | 10.240.0.1:25566 |
 | 00f0ec76-03a1-4d68-b7de-2f30a054e864 | 10.240.0.2:25567 |
 | 00f6795c-8409-4efb-a5e8-ef94f51e68dc | 10.240.0.3:25568 |
 | ... | ... |

server (table)
- Showing which Spigot servers are currently running correctly
- This acts as BungeeCord's dynamic config to list the available servers
- It helps handling join event via BungeeCord after sessions login, not subsequent join from server to server.
- IP and port columns will use Spigot's server.properties' server-ip and server-port values
- "players" column shows number of players currently online inside that Spigot server.
- BungeeCord server will base on this to bring players to the Spigot server with least number of players currently online.
- This feature will be ignore if a player is joining a specific world with specific players currently online on that world, instead it will use "world" table to route players correctly instead.

 | address | players |
 | --- | --- |
 | 10.240.0.1:25566 | 43 |
 | 10.240.0.2:25567 | 7 |
 | 10.240.0.3:25568 | 56 |
 | ... | ... |

player (table)
- List all players online with their current server IP and port
- For teleporting player to player correctly
- IP and port columns will use Spigot's server.properties' server-ip and server-port values

 | uuid  | address |
 | --- | --- |
 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | 10.240.0.1:25566 |
 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | 10.240.0.2:25567 |
 | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 10.240.0.3:25568 |
 | ... | ... |

---

#MySQL database structure

mute (table)
- List all players who are muted on each world

 | uuid  | world |
 | --- | --- |
 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b |
 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | bc384491-4cf7-4185-be07-9bdb5a8310d4 |
 | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 |
 | ... | ... | ... |

ban (table)
- List all players who are banned on each world

 | uuid  | world |
 | --- | --- |
 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b |
 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | bc384491-4cf7-4185-be07-9bdb5a8310d4 |
 | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 |
 | ... | ... | ... |

back (table)
- Previous death location for a player in a specific world
- Logs only when player is dead in a world
  
 | uuid  | world | x | y | z | yaw | pitch |
 | --- | --- | --- | --- | --- | --- | --- |
 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | 128.0 | 67.0 | 4954.0 | 89.0 | -74.4 |
 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 6573.0 | 78.0 | 231.0 | 56.0 | 75.9 |
 | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | 54654.0 | 54.0 | 6758.0 | 67.0 | 34.6 |
 | ... | ... | ... | ... | ... | ... | ... |

logout (table)
- Previously logged out location for a player in a specific world
- It helps when players log back into the same world, they will be in their previous location
- Create entry only when player logs out in a world

 | uuid  | world | x | y | z | yaw | pitch |
 | --- | --- | --- | --- | --- | --- | --- |
 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | 128.0 | 67.0 | 4954.0 | 89.0 | -74.4 |
 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 6573.0 | 78.0 | 231.0 | 56.0 | 75.9 |
 | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | 54654.0 | 54.0 | 6758.0 | 67.0 | 34.6 |
 | ... | ... | ... | ... | ... | ... | ... |

---

##Compiling
Minecraftly is distributed as a [Maven](http://maven.apache.org/) project. To compile it and install it in your local Maven repository:

```git
apt-get install git -y
apt-get install maven -y
git clone https://github.com/minecraftly/kosmos.git
cd minecraftly
mvn clean install
```

---

##Build Kosmos plugin on Jenkins
This guide presumes that you got Jenkins server installed
Simply create a new Freestyle project with the following configurations:

- Freestyle project name
  - minecraftly

- Branches to build
  - Branch Specifier (blank for 'any'): master

- Source Code Management
  - Choose "Git"
  - Repository URL: https://github.com/minecraftly/kosmos.git

- Build
  - Execute shell command: mvn clean install
  
- Post-build Actions
  - Click "Add Post-build action" -> Archive the artifacts
  - Files to archive: **/*.jar

---

##Uniqueness
  - Individual server per player.
  - Accessible via player username: (username).m.ly
  - If a player changes username, the server will map to that subdomain accordingly.
  - Same plugins as everyone else, good for leaderboards, ranks, etc...
  - Servers are always on, no need to "startup on demand", or "stop when inactive".
  - Players can have control of their server. The player is partially OP'd when they login to their server. They have access to:
    - 1. Kick, Ban, Mute others
    - 2. Whitelist their server
    - 3. Reset their world, while keeping their inventories.
  - Teleportation cross servers
  - No slots limit.

---

##Why Open Source?
  I'm [Viet](https://twitter.com/vietdoge), a simple guy with love for gaming, technologies, and Minecraft just like you. I created one of the first Minecraft server in the world (way back in 2012), I've always been looking for a way to scale Minecraft with high availability and fault tolerant. There have been many people in the gaming community wanting a "single server" online gaming experience, that can have lots of players online in the same place at the same time. So I want to do it.

  Started out as a simple Minecraft game server like every other, I understood that Minecraft and open world games have limitation in connections. I saw that it needs something that can handle connections and allows Minecraft to scale incredibly far with limited resources. So I created Minecraftly. It took over a year to develop the first prototype. It's now open source. I'm glad to share the technology with passionate people who want to contribute to the community, to change the world via cloud computing, and with everyone who wants to learn about high availability architecture.

  I dedicated myself to cloud computing and passed my AWS Solutions Architect Certification exam in 2015. It helped construct my knowledge to build a simplier open source project that anyone can test, build, host their own network.

---

##Parallel Space Partitioning:
 In most MMORPG games like World of Warcraft or Minecraft, or Second Life, to scale and serve massive amount of concurrent players, parallel space partitioning needs to be applied.
 
 Normally, space partitioning refers to the dividing of a game into multiple worlds, handled by each separated servers.

 Minecraftly goes above and beyond that. We divide the game into multiple worlds as well, but assigned to each separated players, handled by any servers in the cluster. Luckily, the nature of the Minecraft as an open world game allows that to happen.

---

##Contributing
 Thank you for your interest in Minecraftly. Contributing to Minecraftly is easy, just fork the project on GitHub, make your changes and submit a pull request to get your code merged. That's all there is to it.
 
 Minecraftly is licensed under the GNU General Public License version 3 (GNU GPLv3), and we welcome anybody to fork and submit a Pull Request back with their changes, and if you want to join as a permanent member we can add you to the team.

 Check out the [Wiki](https://github.com/minecraftly/minecraftly/wiki) for more information about setting up the development environment for Minecraftly, finding issues to work on, etc...

 If you are new to open source and/or GitHub, or just aren't sure about some details in the contribution process, here's a tutorial to get you started:
[How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

---

##License
 Minecraftly is distributed under [GNU GPLv3](LICENSE) license.
 
 This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build better software that is scalable, loosely coupled, work on both traditional and cloud infrastructure without vendor lock-in.
 
 The only term of using this software is that you expressively credit Minecraftly website as the creator of the software you are using on your server and website.
 
 We welcome everyone to install and develop on top of Minecraftly free of charge for personal, non-commercial use. We have a patent pending on the software and. We don't allow any commercial use of this architecture.
