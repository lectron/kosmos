#Kosmos

Minecraft multi-world, orchestrated by multi-server. Interact as one universe.

The light weight, distributed layer for Minecraft. It allows multiple servers to orchestrate player interactions across multiple worlds as if it's one universe. Currently these supports BungeeCord and Spigot. Kosmos supports multi-server setup and is designed for high availability.

![Image of a Minecraft world](https://i.imgur.com/sjtYfym.png)

The ultimate goal with Kosmos is to be able to implement high availability and cross-server interactions without dividing player experience, which traditional Minecraft softwares do not inherently provide that functionality. The implementation is efficient, not complex, and highly performant.

##Links
Minecraftly server address: **m.ly**

[Website](http://m.ly)

[Blog](https://blog.minecraftly.com)

[Developer Wiki](https://github.com/minecraftly/kosmos/wiki)

[Work in Progress](https://github.com/minecraftly/kosmos/wiki/WIP)

[Current Main Branch - v2](https://github.com/minecraftly/kosmos/tree/v2)

[Rules of the Kosmos](https://github.com/minecraftly/kosmos/wiki/Plugin-Rules) (Important Read)

[Bounty Program](http://mc.ly/bounty) - Make money by helping us fix bugs and adding new features to the plugin.

---

##In a Nutshell
  - Each player gets a world
  - Each world has it's own subdomain name, mapped to player's own username: *{player-username}.m.ly*
  - Custom domain is possible via mapping CNAME to *{player-uuid}.m.ly*
  - Worlds are always on, no need to "startup on demand", or "stop when inactive".
  - Worlds are loaded and unloaded dynamically by one or multiple Spigot servers.
  - One or multiple BungeeCord server(s) bring players to the appropriate Spigot servers inside the appropriate world.
  - Player-to-player teleportation accross worlds
  - Player-to-world teleportation accross worlds
  - Player-to-warp teleporation accross worlds
  - A player is admin control of their world. They have access to:
    - 1. Kick, Ban, Mute others
    - 2. Whitelist their world
    - 3. Reset their world, while keeping their inventories.
    - 4. Appoint moderators, and give trust to people at different trust levels

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
  - kosmos

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

##Why Open Source?
  I'm [Viet](http://vi.et), a simple guy with love for gaming, technologies, and Minecraft just like you. I created one of the first Minecraft server in the world (way back in 2012), I've always been looking for a way to scale Minecraft with high availability and fault tolerant. There have been many people in the gaming community wanting a "single server" online gaming experience, that can have lots of players online in the same place at the same time. So I want to do it.

  Started out as a simple Minecraft game server like every other, I understood that Minecraft and open world games have scalability limitation just like any other MMO game. I saw that it needs something that can handle connections and allows Minecraft to scale incredibly far with limited resources. So I created Kosmos. It took over a year to develop the first prototype. It's now open source. I'd like to share the technology with passionate people who want to contribute to the community, to change the world via cloud computing, and with everyone who wants to learn about high availability architecture.

  I dedicated myself to cloud computing and passed my AWS Solutions Architect Certification exam in 2015. It helped construct my knowledge to build a simplier open source project that anyone can test, build, host their own network.

---

##Contributing
 Thank you for your interest in Kosmos. Contributing to Kosmos is easy, just fork the project on GitHub, make your changes and submit a pull request to get your code merged. That's all there is to it.
 
 Minecraftly is licensed under the GNU General Public License version 3 (GNU GPLv3), and we welcome anybody to fork and submit a Pull Request back with their changes, and if you want to join as a permanent member we can add you to the team.

 Check out the [Wiki](https://github.com/minecraftly/minecraftly/wiki) for more information about setting up the development environment for Kosmos, finding issues to work on, etc...

 If you are new to open source and/or GitHub, or just aren't sure about some details in the contribution process, here's a tutorial to get you started:
[How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

---

##License
 Kosmos is distributed under [GNU GPLv3](LICENSE) license.
 
 This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build better software that is scalable, loosely coupled, work on both traditional and cloud infrastructure without vendor lock-in.
 
 The only term of using this software is that you expressively credit Minecraftly website as the creator of the software you are using on your server and website.
 
 We welcome everyone to install and develop on top of Kosmos free of charge for personal, non-commercial use. We have a patent pending on the software and. We don't allow any commercial use of this architecture.
