[![Travis Widget]][Travis] [![Coverage Status Widget]][Coverage Status]

[Travis]: https://travis-ci.org/minecraftly/minecraftly
[Travis Widget]: https://travis-ci.org/minecraftly/minecraftly.svg?branch=master
[Coverage Status]: https://coveralls.io/r/minecraftly/minecraftly
[Coverage Status Widget]: https://coveralls.io/repos/minecraftly/minecraftly/badge.svg

#Want to make money fixing bugs for Minecraftly?
We help developers earn a salary from contributing to our open-source software.

[![GitHub Logo](https://d2bbtvgnhux6eq.cloudfront.net/assets/Bountysource-green-f2f437ed727ee2cacaee3f559c1907cb.png)](https://salt.bountysource.com/teams/minecraftly)

#Welcome
---
  Please read everything carefully, as they tell you what this is all about, explain how to install the software, and what to do if something goes wrong. 

##What is Minecraftly?
  Minecraftly (Minecraftly Cloud Platform) is a free and open source (FOSS) alternative of the official Mojang's Minecraft Realms, designed and written from scratch by Viet Nguyen and Keir Nellyer, respectively, with assistance from some friendly developers and helpers from the internet. It aims towards creating an easy to run a distributed network, on any server, using traditional server system. Minecraftly Cloud Platform is now community-developed and will be remained free under GNU GPL license.
  
  Started out as a simple Minecraft game server like every other, I understood and saw the importance of education through gaming. The period of changing mindset from owning a proprietary software to deciding if I should open source it was not short of a challenge. In the end, I'm glad that I was able to open source it, and share it with passionate people who want to contribute to the community, to change the world via cloud computing, and with everyone who wants to learn.

  It has all the features you would expect in a Minecraft server, with additional performance gain
  including async, cloud computing compatibility, on demand
  loading.
  
  Minecraftly can run on traditional Minecraft server setup, with local file system. The advantage is, it's able to run on one and multiple servers at the same time, sharing the same NFS, Redis, and MySQL servers as a way of communicating between servers.
  
  We currently support the latest version of Minecraft. The version is always up to date.
  
##Why Open Source?
  I'm [Viet](https://twitter.com/vietdoge), a simple guy with love for cloud computing, the web, technologies, and Minecraft just like you. Ever since I first run a Minecraft server in 2012, I've always been looking for a way to scale Minecraft with high availability and fault tolerant. It took me years to think and build the first prototype after seeing that not many people in the community have a high availability mindset. I can't do it alone and need your contribution to make it better.
  
  I dedicated myself to cloud computing and passed my AWS Solutions Architect Certification exam in 2015. It helped construct my knowledge to build a simplier open source project that anyone can test, build, host their own network.
  
## Parallel Space Partitioning:
 In most MMORPG games like World of Warcraft or Minecraft, or Second Life, to scale and serve massive amount of concurrent players, parallel space partitioning needs to be applied.
 
 Normally, space partitioning refers to the dividing of a game into multiple worlds, handled by each separated servers.

 Minecraftly goes above and beyond that. We divide the game into multiple worlds as well, but assigned to each separated players, handled by any servers in the cluster. Luckily, the nature of the Minecraft as an open world game allows that to happen.
 
 #Minecraftly is:
 lean: lightweight, simple, accessible
 portable: public, private, hybrid, multi cloud
 extensible: modular, pluggable, hookable, composable
 self-healing: auto-placement, auto-replication ready
 
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
  
##Special Thanks To
  Keir (@ikeirnez), Tux (@minecrafter), Ben (@redraskal), (@portalBlock) and many others who have been helping me over the years to make this happen.
  
##Product Roadmap
- [x] Create world for each player who joins using a folder UUID
- [x] Map subdomain of {uuid}.m.ly to the player server
- [x] Multiple servers pointing to one folder that serves worlds
- [ ] Let players whitelist, mute, kick, ban and trust others in their own world
- [ ] Let players teleport to others via /tpa {username}, /tpahere {username}, /server {username} commands.

#License
 Minecraftly is distributed under [GNU GPLv3](LICENSE) license.
 
 This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build better software that is scalable, loosely coupled, work on both traditional and cloud infrastructure without vendor lock-in.
 
 The only term of using this software is that you expressively credit Minecraftly website as the creator of the software you are using on your server and website.
