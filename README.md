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
  
##How it works
 Usually, in a traditional Minecraft server, player flow is like this:
```go
   Player A                            Player B                            Player C
      |                                   |                                   |
      |                                   |                                   |
      |                                   |                                   |
      +-----------------------------------+-----------------------------------+
                                          |
                                          |
                                          |
                                          ▼ 
                                       Server 1
                                          |
                                          |
                                          |
                                          ▼ 
                                    SERVER_world_1
                                    SERVER_world_1_nether
                                    SERVER_world_1_the_end
```
 Above is the old ways of handling players. Bottleneck usually happens when a single machine gets filled up with high amount of concurrent players.

#The Right Way to Scale
 Let's visualize a smarter way to distribute players, where server is seperated from world files. In Minecraftly, it's like this:
```ruby
        Player A                       Player B                       Player C
           |                              |                              |
           |                              |                              |
           |                              |                              |
           ▼                              ▼                              ▼
       Server 1 or 2 or 3 or 4 or 5 or 6 or 7 or 8 or 9 or 10 or 11 or 12... or ∞
           |                              |                              |
           |                              |                              |
           |                              |                              |
           ▼                              ▼                              ▼
+------------------------------------------------------------------------------------+
| PLAYER_world_A               PLAYER_world_B                 PLAYER_world_C         |
| PLAYER_world_A_nether        PLAYER_world_B_nether          PLAYER_world_C_nether  |
| PLAYER_world_A_the_end       PLAYER_world_B_the_end         PLAYER_world_C_the_end |
+------------------------------------------------------------------------------------+
```
  
##Requirements
 * BungeeCord: serve as a proxy server
 * Spigot: serve as Minecraft server
 * MySQL Server
 * Redis Server (Will plan to not depend on Redis and RedisBungee)
 * RedisBungee plugin (Will plan to not depend on Redis and RedisBungee)
 * Vault plugin (for permissions hook)
 * ProtocolLib plugin (Will plan to not depend on ProtocolLib)

##How is it better than [Minecraft Realms](https://minecraft.net/realms)?
 Minecraftly is better than Minecraft Realms in many ways. First, let's look at how complex and redundant Minecraft Realms is
![screen shot Minecraft Realms Architecture](https://media.amazonwebservices.com/blog/2014/minecraft_realms_arch_2.png)

 Cloud computing doesn't need to be that complicated. It's complicated mainly because of intellectual properties. Since we're open source, we can make it as simple and as extendable as possible.
 
 Here are some simple comparisons:
 * Minecraftly doesn't need Frontend, Manager, Controller, and Amazon S3 object storage
 * Minecraftly doesn't need move world from object storage back to local block storage, which means players can load world right away.
 * Minecraftly lets players jump from server to server in real time, embracing the feeling of having many people playing with you at the same time.
 * Minecraftly saves server cost more efficiently than Minecraft Realms. While Realms creates a separated server for each paid player which is costly, we use one server for many free and paid players, and still deliver better and more seamless performance.
 
##Contributing
  Minecraftly is licensed under the GNU General Public License version 3 (GNU GPLv3), and we welcome anybody to fork and submit a Pull Request back with their changes, and if you want to join as a permanent member we can add you to the team.
  
  This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build better software that work on both traditional and cloud infrastructure.
  
  For details of license, check the [LICENSE](LICENSE) file.

  For contributing information, check out the [CONTRIBUTING.md](CONTRIBUTING.md) file for more details.

##Managed Hosting
  Besides the free and open source version, we also offer a value added hosted service at [https://m.ly](https://m.ly). You can play with friends and don't have to setup server.
  
##Special Thanks To
  Andrew, Keir, Tux, Michael, Devin, Snivell, Ben (redraskal) and many others who have been helping me over the years to make this happen.
  
##To Do
- [x] Create world with UUID format for each player with the same UUID
- [x] Multiple servers pointing to one folder that serves worlds
- [ ] Let players mute, kick, ban and trust others in their own world
- [ ] Let players teleport to others via /tpa {username}, /tpahere {username}, /server {username} commands.

#License
 Minecraftly is distributed under [GNU GPLv3](LICENSE) license.
