#Welcome

  Minecraftly is a free and open source (FOSS) Minecraft server plugin to create "Minecraft servers within servers" automagically. It is designed & produced from scratch by [Viet Nguyen](https://vi.et), with assistance from friends, developers and helpers from the internet. Minecraftly is now community-developed and will be remained free under [GNU GPLv3](LICENSE) license.
  
  Minecraftly is compatible with most infrastructures, including but not limited to:
  - Single machine
  - Multiple machine cluster
  - Autoscaling cloud infrastructure
    - Google Cloud Platform
    - Amazon Web Services
    - Microsoft Azure
    - RackSpace
  - Containers
    - Docker
    - Kubernetes

---

##Features
- Player joins via {their_username}.{domain}.{tld}:
  - [x] if joins for the first time, server creates the world
  - [x] world has the folder format name {uuid}
  - [x] it contains world contents like usual (world, world_nether, world_the_end, session.lock, etc...)
  - [x] only owner of the world can create the world
  - [x] other players can only load the world if available.
  - [x] worlds are stored in the pre defined folder ~/mnt/worlds/{uuid}
  - [x] ~/mnt/worlds folder is shared across all spigot servers
  - [ ] player data will be in the common folder ~/mnt/playerdata, shared across all spigot servers
  - [ ] achievement stats will be in the common folder ~/mnt/stats, shared across all spigot servers
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
  - [ ] Whitelist your world
  - [ ] Turn on whitelist: /whitelist on
  - [ ] Add players to your whitelist: /whitelist add {username}
  - [ ] List the players who are in your whitelist: /whitelist list
  - [ ] Turn off whitelist: /whitelist off
- [ ] Ban
  - [ ] Ban players from ever playing in your world
  - [ ] Ban a player: /ban {username}
  - [ ] Show your ban list: /banlist
  - [ ] Unban a player: /unban {username}
  - [ ] Upon being banned, the player will be teleported back to their world
-  [ ] Teleport to a player
  - [ ] Teleport to any player
  - [ ] Send teleport request so you can teleport to the other player: /tpa {username}
  - [ ] Send teleport request so the other player teleport to you: /tpahere {username}
  - [ ] Accept a teleport request: /tpaccept
  - [ ] Deny a teleport request: /tpdeny
  - [ ] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [ ] If you are banned from that world, don't teleport
- [ ] Teleport to a world
  - [ ] Teleport yourself to any world of any player
  - [ ] Command: /server {username}
  - [ ] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [ ] If you are banned from that world, don't telepor-  [ ] Teleport to a player
  - [ ] Teleport to any player
  - [ ] Send teleport request so you can teleport to the other player: /tpa {username}
  - [ ] Send teleport request so the other player teleport to you: /tpahere {username}
  - [ ] Accept a teleport request: /tpaccept
  - [ ] Deny a teleport request: /tpdeny
  - [ ] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [ ] If you are banned from that world, don't teleport
- [ ] Separation of server message
  - [ ] Server messages that are shown per server will now be shown per world to reduce spammy, irrelevant messages
  - [ ] join message
  - [ ] leave message
  - [ ] quit message
  - [ ] kick message
  - [ ] death message
  - [ ] achievements message
- [ ] Mute
  - [ ] Mute annoying player from talking in your world
  - [ ] /mute {username}
  - [ ] /mutelist
  - [ ] /unmute {username}
- [ ] Kick
  - [ ] Kick a player from playing in your world
  - [ ] /kick {username}
  - [ ] Upon being kicked, the player will be teleported back to their world
- [ ] Reset
  - [x] Reset your world, world_nether, world_the_end, and dat file
  - [x] Doesn't reset your playerdata or achievement stats
  - [x] Command: /reset
  - [x] Type /reset again to confirm
- [ ] Back
  - [ ] Command: /back
  - [ ] You can go back to your death location on the world you are in.

---

#MySQL database structure
servers (table)
- Showing which Spigot servers are currently running correctly
- This acts as BungeeCord's dynamic config to list the available servers
- IP and port columns will use Spigot's server.properties' server-ip and server-port values
- TPS column is for tick-per-second, updated from time to time.
- Bungee will only bring players to the server with highest TPS
- If after a while, server doesn't update TPS, it means server crashes, then the row gets removed

| address | tps |
| --- | --- |
| 10.240.0.1:25566 | 19.97 |
| 10.240.0.2:25567 | 13.54 |
| 10.240.0.3:25568 | 18.32 |
| ... | ... |
 


world (table)
- Showing which world is currently loaded by which server.
- If a world is already loaded on one server, subsequent player who joins via subdomain will go to correct server.
- This will prevent world being loaded twice on many different spigot servers
- IP and port columns will use Spigot's server.properties' server-ip and server-port values

| world  | address |
| --- | --- |
| 00ceaed3-3715-49e9-b45f-0e01cf94f798 | 10.240.0.1:25566 |
| 00f0ec76-03a1-4d68-b7de-2f30a054e864 | 10.240.0.2:25567 |
| 00f6795c-8409-4efb-a5e8-ef94f51e68dc | 10.240.0.3:25568 |
| ... | ... |
 
 
 
uuid (table)
- For UUID caching 
- Because Mojang has an API limit of only 10 per minute.

| uuid  | username
| --- | --- | --- |
| 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | AsianGuy_Gamer |
| c088272e-a8ca-496c-91a2-b7394ffe879c | ImRainbowActive |
| cf1f1ea8-4bc9-4cba-886c-33997403eb80 | AruAkise_ |
| ... | ... | ... |
 
 
 
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
| ... | ... | ... |
 
 
 
logout (table)
- Previously logged out location for a player in a specific world
- It helps when players log back into the same world, they will be in their previous location
- Create entry only when player logs out in a world

| uuid  | world | x | y | z | yaw | pitch |
| --- | --- | --- | --- | --- | --- | --- |
| 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | bc68ca39-8f3a-4eb4-a764-8526de7fb90b | 128.0 | 67.0 | 4954.0 | 89.0 | -74.4 |
| bc68ca39-8f3a-4eb4-a764-8526de7fb90b | bc384491-4cf7-4185-be07-9bdb5a8310d4 | 6573.0 | 78.0 | 231.0 | 56.0 | 75.9 |
| bc384491-4cf7-4185-be07-9bdb5a8310d4 | 0cc87f4b-6b4a-404f-b11d-db2e76a24243 | 54654.0 | 54.0 | 6758.0 | 67.0 | 34.6 |
| ... | ... | ... |

---

##BungeeCord config
Here are a few things that need changes in the config of BungeeCord proxy server

- config.yml
  - ip_forward: true
  - servers:
    - lobby:
      - motd: 'A Minecraftly Server'
      - address: {your spigot server's IP address}:{your spigot server's port}
      - restricted: false

---

##Spigot config
Here are a few things that need changes in the config of Spigot Minecraft server

- server.properties
  - server-ip={the public or private server IP that is attached to your machine}
  - server-port={whatever port you want}
  - online-mode=false
- bukkit.yml
  - connection-throttle: -1
- spigot.yml
  - bungeecord: true
- eula.txt
  - eula=true

---

##How to describe Minecraftly
- Tumblr for Minecraft
- Wordpress Multisite for Minecraft
- A Minecraft server within a server

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

##Similar Technologies
Minecraftly is inspired an improved from many technologies, some of them were released after Minecraftly but wasn't that complete.
- [Multiverse](http://dev.bukkit.org/bukkit-plugins/multiverse-core/)
- [Docker + Minecraft = Dockercraft](https://github.com/docker/dockercraft)
- [Google's Containerized Minecraft Roulette](http://www.blog.juliaferraioli.com/2015/11/containerized-minecraft-roulette.html)

---

##Why Open Source?
  I'm [Viet](https://twitter.com/vietdoge), a simple guy with love for cloud computing, the web, technologies, and Minecraft just like you. Ever since I first run a Minecraft server in 2012, I've always been looking for a way to scale Minecraft with high availability and fault tolerant. It took me years to think and build the first prototype after seeing that not many people in the community have a high availability mindset. I can't do it alone and need your contribution to make it better.

  Started out as a simple Minecraft game server like every other, I understood that Minecraft and open world games have limitation in connections. I saw that it needs something that can handle connections and allows Minecraft to scale incredibly far with limited resources. So I created Minecraftly. It took over a year to develop the first prototype. It's now open source. I'm glad to share the technology with passionate people who want to contribute to the community, to change the world via cloud computing, and with everyone who wants to learn about high availability architecture.

  I dedicated myself to cloud computing and passed my AWS Solutions Architect Certification exam in 2015. It helped construct my knowledge to build a simplier open source project that anyone can test, build, host their own network.

---

##Parallel Space Partitioning:
 In most MMORPG games like World of Warcraft or Minecraft, or Second Life, to scale and serve massive amount of concurrent players, parallel space partitioning needs to be applied.
 
 Normally, space partitioning refers to the dividing of a game into multiple worlds, handled by each separated servers.

 Minecraftly goes above and beyond that. We divide the game into multiple worlds as well, but assigned to each separated players, handled by any servers in the cluster. Luckily, the nature of the Minecraft as an open world game allows that to happen.

---

##Architecture
 Usually, in a traditional Minecraft server, player flow is like this:
 
<img src="https://m.ly/images/8hGSRpe.svg" width="100%"/>
 
 Above is the old ways of handling players. It's too monolithic. Bottleneck usually happens when a single machine gets filled up with high amount of concurrent players.
 
Now, let's visualize another way to distribute players, where server is seperated from world files. In Minecraftly, it's like this:
 
<img src="https://m.ly/images/ykl5mnN.svg" width="100%"/>

Each player has their own world that they control, accessible via their own public address. The beautiful thing is that all players can interact with others normally.

---

##Requirements
 * BungeeCord: serve as a proxy server (equivalent to Nginx or HAProxy in web hosting)
 * Spigot: serve as Minecraft server. Spigot is important because it has "--world-dir" flag at startup, which specifies the directory for all world maps.
 * MySQL server: Holding each server data like ban list, whitelist, etc..
 * Redis server: Holding sessions, healthchecks, lists of available servers data.

---

##Dependencies
 Dependencies are Java plugins, libraries, and classes that serve as prerequisites for Minecraftly plugins to run. As development goes, we aim to remove all dependencies, as much as possible, so the software isn't dependent on any external factor. That's how we build loosely coupled software.
 * RedisBungee plugin: Currently acts as a bridge between Minecraftly plugins and Redis server
 * Vault plugin: Acts as a bridge between Minecraftly and permissions plugins.
 * ProtocolLib plugin: It's dependent somehow. Will work to remove this dependency.

---

##Contributing
 Thank you for your interest in Minecraftly. Contributing to Minecraftly is easy, just fork the project on GitHub, make your changes and submit a pull request to get your code merged. That's all there is to it.
 
 Minecraftly is licensed under the GNU General Public License version 3 (GNU GPLv3), and we welcome anybody to fork and submit a Pull Request back with their changes, and if you want to join as a permanent member we can add you to the team.

 Check out the [Wiki](https://github.com/minecraftly/minecraftly/wiki) for more information about setting up the development environment for Minecraftly, finding issues to work on, etc...

 If you are new to open source and/or GitHub, or just aren't sure about some details in the contribution process, here's a tutorial to get you started:
[How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

---

##Hosted Version
  Besides the free and open source version, we also offer a value added hosted service at [https://m.ly](https://m.ly). We reduce the steps of getting a Minecraft server into a single action. 
  
  You can play with friends and don't have to setup server. Just join, that's it. 

---

##License
 Minecraftly is distributed under [GNU GPLv3](LICENSE) license.
 
 This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build better software that is scalable, loosely coupled, work on both traditional and cloud infrastructure without vendor lock-in.
 
 The only term of using this software is that you expressively credit Minecraftly website as the creator of the software you are using on your server and website.

##Bounty Program
Want to make money fixing bugs for Minecraftly?

We help developers earn a salary from contributing to our open-source software.

[![GitHub Logo](https://d2bbtvgnhux6eq.cloudfront.net/assets/Bountysource-green-f2f437ed727ee2cacaee3f559c1907cb.png)](https://www.bountysource.com/teams/minecraftly)

---

##Build Status

[![Travis Widget]][Travis] [![Coverage Status Widget]][Coverage Status]

[Travis]: https://travis-ci.org/minecraftly/minecraftly
[Travis Widget]: https://travis-ci.org/minecraftly/minecraftly.svg?branch=master
[Coverage Status]: https://coveralls.io/r/minecraftly/minecraftly
[Coverage Status Widget]: https://coveralls.io/repos/minecraftly/minecraftly/badge.svg?branch=master
