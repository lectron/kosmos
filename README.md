#Welcome

  Minecraftly is a free and open source (FOSS) Minecraft server plugin to create Minecraft servers automatically, with autoscaling compatibility, designed & produced from scratch by Viet Nguyen, with assistance from some developers and helpers from the internet. It aims towards creating an easy to run a distributed network, on any server, using traditional server system. Minecraftly Cloud Platform is now community-developed and will be remained free under GNU GPL license.
  
  Started out as a simple Minecraft game server like every other, I understood that Minecraft and open world games have limitation in connections. I saw that it needs something that can handle connections and allows Minecraft to scale incredibly far with limited resources. So I created Minecraftly Cloud Platform. It took over a year to develop the first prototype. It's now open source. I'm glad to share the technology with passionate people who want to contribute to the community, to change the world via cloud computing, and with everyone who wants to learn about high availability architecture.
  
  Minecraftly can run on traditional Minecraft server setup, or multiple server cluster. It works properly with Docker and Kubernetes. The advantage is, it's able to run on one and multiple servers at the same time, sharing the same NFS, Redis, and MySQL servers as a way of communicating between servers.

---

##How world creation, loading and management work
- Player joins via {their_username}.{domain}.{tld}:
  - [x] if joins for the first time, server creates the world
  - [x] world has the folder format name {uuid}
  - [x] it contains world contents like usual (world, world_nether, world_the_end, session.lock, etc...)
  - [x] only owner of the world can create the world
  - [x] other players can only load the world if available.
  - [x] worlds are stored in the pre defined folder /worlds/{uuid}
  - [x] ~/worlds folder is shared across all spigot servers
  - [x] works with {domain}.{tld} or simply the proxy IP address
  - [x] default game mode for world owner is SURVIVAL
  - [x] Default game rule changes for all worlds:
    - [x] keepInventory: true
    - [x] mobGriefing: false
    - [ ] commandBlockOutput: false
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
- Reset
  - [x] Reset your world, world_nether, world_the_end, and dat file
  - [x] Doesn't reset your playerdata or achievement stats
  - [x] Command: /reset
  - [x] Type /reset again to confirm
- [ ] Mute
  - [ ] Mute annoying player from talking in your world
  - [ ] /mute {username}
  - [ ] /mutelist
  - [ ] /unmute {username}
- [ ] Kick
  - [ ] Kick a player from playing in your world
  - [ ] /kick {username}
  - [ ] Upon being kicked, the player will be teleported back to their world
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
- [ ] Teleport to a world
  - [ ] Teleport yourself to any world of any player
  - [ ] Command: /server {username}
  - [ ] If world is whitelisted and you are not in that world's whitelist, don't teleport
  - [ ] If you are banned from that world, don't teleport
-  [ ] Teleport to a player
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

##How to describe Minecraftly
- Tumblr for Minecraft
- Wordpress Multisite for Minecraft
- A Minecraft server within a server
  
##Features
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
  
##Why Open Source?
  I'm [Viet](https://twitter.com/vietdoge), a simple guy with love for cloud computing, the web, technologies, and Minecraft just like you. Ever since I first run a Minecraft server in 2012, I've always been looking for a way to scale Minecraft with high availability and fault tolerant. It took me years to think and build the first prototype after seeing that not many people in the community have a high availability mindset. I can't do it alone and need your contribution to make it better.
  
  I dedicated myself to cloud computing and passed my AWS Solutions Architect Certification exam in 2015. It helped construct my knowledge to build a simplier open source project that anyone can test, build, host their own network.
  
##Parallel Space Partitioning:
 In most MMORPG games like World of Warcraft or Minecraft, or Second Life, to scale and serve massive amount of concurrent players, parallel space partitioning needs to be applied.
 
 Normally, space partitioning refers to the dividing of a game into multiple worlds, handled by each separated servers.

 Minecraftly goes above and beyond that. We divide the game into multiple worlds as well, but assigned to each separated players, handled by any servers in the cluster. Luckily, the nature of the Minecraft as an open world game allows that to happen.

##Architecture
 Usually, in a traditional Minecraft server, player flow is like this:
 
<img src="https://m.ly/images/8hGSRpe.svg" width="100%"/>
 
 Above is the old ways of handling players. It's too monolithic. Bottleneck usually happens when a single machine gets filled up with high amount of concurrent players.
 
 
How about the official Minecraft Realms by Mojang?
 
 <img src="https://media.amazonwebservices.com/blog/2014/minecraft_realms_arch_2.png" width="100%"/>
 
Minecraft Realms is scalable but too complicated, and players are still separated from the others.
 
Now, let's visualize another way to distribute players, where server is seperated from world files. In Minecraftly, it's like this:
 
<img src="https://m.ly/images/ykl5mnN.svg" width="100%"/>

Players can interact with others normally, it's just that each player has their own world, accessible via their own public address.

##How is it better than [Minecraft Realms](https://minecraft.net/realms)?
 Cloud computing doesn't need to be that complicated. It's complicated mainly because of intellectual properties. Since we're open source, we can make it as simple and as extendable as possible.
 
 Here are some simple comparisons:
 * Minecraftly doesn't need Frontend, Manager, Controller, and Amazon S3 object storage
 * Minecraftly doesn't need move world from object storage back to local block storage, which means players can load world right away.
 * Minecraftly lets players jump from server to server in real time, embracing the feeling of having many people playing with you at the same time.
 * Minecraftly saves server cost more efficiently than Minecraft Realms. While Realms creates a separated server for each paid player which is costly, we use one server for many free and paid players, and still deliver better and more seamless performance.
  
##Requirements
 * BungeeCord: serve as a proxy server (equivalent to Nginx or HAProxy in web hosting)
 * Spigot: serve as Minecraft server. Spigot is important because it has "--world-dir" flag at startup, which specifies the directory for all world maps.
 * MySQL server: Holding each server data like ban list, whitelist, etc..
 * Redis server: Holding sessions, healthchecks, lists of available servers data.
 
##Dependencies
 Dependencies are Java plugins, libraries, and classes that serve as prerequisites for Minecraftly plugins to run. As development goes, we aim to remove all dependencies, as much as possible, so the software isn't dependent on any external factor. That's how we build loosely coupled software.
 * RedisBungee plugin: Currently acts as a bridge between Minecraftly plugins and Redis server
 * Vault plugin: Acts as a bridge between Minecraftly and permissions plugins.
 * ProtocolLib plugin: It's dependent somehow. Will work to remove this dependency.

##Contributing
 Thank you for your interest in Minecraftly. Contributing to Minecraftly is easy, just fork the project on GitHub, make your changes and submit a pull request to get your code merged. That's all there is to it.
 
 Minecraftly is licensed under the GNU General Public License version 3 (GNU GPLv3), and we welcome anybody to fork and submit a Pull Request back with their changes, and if you want to join as a permanent member we can add you to the team.

 Check out the [Wiki](https://github.com/minecraftly/minecraftly/wiki) for more information about setting up the development environment for Minecraftly, finding issues to work on, etc...

 If you are new to open source and/or GitHub, or just aren't sure about some details in the contribution process, here's a tutorial to get you started:
[How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

##Managed Hosting
  Besides the free and open source version, we also offer a value added hosted service at [https://m.ly](https://m.ly). You can play with friends and don't have to setup server.

##License
 Minecraftly is distributed under [GNU GPLv3](LICENSE) license.
 
 This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build better software that is scalable, loosely coupled, work on both traditional and cloud infrastructure without vendor lock-in.
 
 The only term of using this software is that you expressively credit Minecraftly website as the creator of the software you are using on your server and website.

##Want to make money fixing bugs for Minecraftly?
We help developers earn a salary from contributing to our open-source software.

[![GitHub Logo](https://d2bbtvgnhux6eq.cloudfront.net/assets/Bountysource-green-f2f437ed727ee2cacaee3f559c1907cb.png)](https://www.bountysource.com/teams/minecraftly)

##Build Status

[![Travis Widget]][Travis] [![Coverage Status Widget]][Coverage Status]

[Travis]: https://travis-ci.org/minecraftly/minecraftly
[Travis Widget]: https://travis-ci.org/minecraftly/minecraftly.svg?branch=master
[Coverage Status]: https://coveralls.io/r/minecraftly/minecraftly
[Coverage Status Widget]: https://coveralls.io/repos/minecraftly/minecraftly/badge.svg?branch=master
