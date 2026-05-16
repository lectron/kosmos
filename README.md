# Kosmos

Updated May 2026 by Grok AI for Viet York - plugin name fixed to Kosmos, license renamed, idea folder removed, dead links cleaned, repo references updated to lectron/kosmos, documentation refreshed

Minecraft multi-world, orchestrated by multi-server. Interact as one universe.

The light weight, distributed layer for Minecraft. It allows multiple servers to orchestrate player interactions across multiple worlds as if it's one universe. Currently these supports BungeeCord and Spigot.

Kosmos supports multi-server setup and is designed for high availability.

The ultimate goal with Kosmos is to be able to implement high availability and cross-server interactions without dividing player experience, which traditional Minecraft softwares do not inherently provide that functionality. The implementation is efficient, not complex, and highly performant.

## Links

- [GitHub Repo](https://github.com/lectron/kosmos)
- [License](LICENSE)
- [Rules of the Kosmos](https://github.com/lectron/kosmos/wiki/Plugin-Rules) (Important Read)
- [Current Main Branch v2](https://github.com/lectron/kosmos/tree/v2)

## In a Nutshell

- Each player gets a world
- Each world has its own subdomain name, mapped to player's own username: *{player-username}.m.ly*
- Custom domain is possible via mapping CNAME to *{player-uuid}.m.ly*
- Worlds are always on, no need to "startup on demand", or "stop when inactive"
- Worlds are loaded and unloaded dynamically by one or multiple Spigot servers
- One or multiple BungeeCord servers bring players to the appropriate Spigot servers inside the appropriate world
- Player-to-player teleportation across worlds
- Player-to-world teleportation across worlds
- Player-to-warp teleportation across worlds
- A player is admin control of their world. They have access to:
  1. Kick, Ban, Mute others
  2. Whitelist their world
  3. Reset their world, while keeping their inventories
  4. Appoint moderators, and give trust to people at different trust levels

## REQUIREMENTS

- [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/): serve as a proxy server (equivalent to Nginx or HAProxy in web hosting)
- [Spigot](https://www.spigotmc.org/wiki/spigot/): serve as Minecraft server
  - Alternative: PaperSpigot is a derivative of Spigot, giving a little higher performance than traditional Spigot
- [Redis server](https://redis.io/): Real time volatile database that connects BungeeCord and Spigot together
- (optional) shared file system for multiple Spigot servers to access the same /worlds folder
- (optional) Dank memes

## SETUP

See setup example [here](https://github.com/lectron/kosmos/wiki/Setup)

## Compiling

Kosmos is distributed as a [Maven](http://maven.apache.org/) project. To compile it and install it in your local Maven repository:

```bash
apt-get install git -y
apt-get install maven -y
git clone https://github.com/lectron/kosmos.git
cd kosmos
mvn clean install
```

## Build Kosmos plugin on Jenkins

This guide presumes that you got Jenkins server installed.

Simply create a new Freestyle project with the following configurations:

- **Freestyle project name**: kosmos
- **Branches to build**
  - Branch Specifier (blank for 'any'): master
- **Source Code Management**
  - Choose "Git"
  - Repository URL: https://github.com/lectron/kosmos.git
- **Build**
  - Execute shell command: mvn clean install
- **Post-build Actions**
  - Click "Add Post-build action" -> Archive the artifacts
  - Files to archive: **/*.jar

## Why Open Source?

I'm [Viet](http://vi.et), a simple guy with love for gaming, technologies, and Minecraft just like you. I created one of the first Minecraft servers in the world (way back in 2012), and I've always believed in the power of open source software. See the rest of the original story on the wiki for historical context.

## Contributing

Thank you for your interest in Kosmos. Contributing to Kosmos is easy, just fork the project on GitHub, make your changes and submit a pull request to get your code merged.

Kosmos is licensed under the GNU General Public License version 3 (GNU GPLv3), and we welcome anybody to fork and submit a Pull Request back with their changes. If you want to join as a core contributor, please get in touch.

Check out the [Wiki](https://github.com/lectron/kosmos/wiki) for more information about setting up the development environment for Kosmos, finding issues to work on, etc.

If you are new to open source and/or GitHub, or just aren't sure about some details in the contribution process, here's a tutorial to get you started: [How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

## License

Kosmos is distributed under [GNU GPLv3](LICENSE) license.

This is a "copyleft" license, which means if you publish the modified work as your own, you must open source it as well. It benefits the educational purpose of the software and helps everyone build upon it.

We welcome everyone to install and develop on top of Kosmos free of charge for personal, non-commercial use.
