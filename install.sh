#!/bin/bash
###############################################################################
# minecraftly dev environment installer
# --------------------------------
# This script installs a minecraftly stack suitable for development. DO NOT run this
# on a system that you use for other purposes as it might delete important
# files, truncate your databases, and otherwise do mean things to you.
#
# By default, this script will install the minecraftly code in the /minecraftly directory
# and all of its dependencies (including java, screen, libraries and database
# servers) at the system level. The installed minecraftly will run on screen
# and accessible via "screen -r" command. Configuring, changing settings, and
# optimizing the server for performance boost is expected to be done outside 
# the installed environment and is not something this script handles.
#
# This dev environment requires no NAS/NFS server, because it only runs on one machine.
#
# Requirements: This script works with Debian 8 or higher.
#
###############################################################################

# Sudo as root
sudo -i

# Install screen to let multiple Minecraft sessions run in background
apt-get install screen -y

# Install MySQL database server
# username: "root", 
# no password 
# database name: "minecraftly"
export DEBIAN_FRONTEND=noninteractive
echo "mysql-server-5.5 mysql-server/root_password password 123456" | debconf-set-selections
echo "mysql-server-5.5 mysql-server/root_password_again password 123456" | debconf-set-selections
apt-get -y install mysql-server-5.5
mysql -u root -p123456 -e "create database minecraftly;"
mysqladmin -u root -p123456 password ''

# Install Redis server
apt-get install redis-server -y

# Install latest Java version
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" >> /etc/apt/sources.list.d/java-8-debian.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" >> /etc/apt/sources.list.d/java-8-debian.list
apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get update -y
apt-get install oracle-java8-installer -y

# Download some preconfigured files
wget -P /minecraftly/bungeecord1 http://ci.md-5.net/job/BungeeCord/1119/artifact/bootstrap/target/BungeeCord.jar
wget -P /minecraftly/bungeecord2 http://ci.md-5.net/job/BungeeCord/1119/artifact/bootstrap/target/BungeeCord.jar
wget -P /minecraftly/bungeecord1/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/RedisBungee.jar
wget -P /minecraftly/bungeecord2/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/RedisBungee.jar
wget -P /minecraftly/bungeecord1/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/MinecraftlyBungee.jar
wget -P /minecraftly/bungeecord2/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/MinecraftlyBungee.jar
wget -P /minecraftly/bungeecord1/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/config.yml
wget -P /minecraftly/bungeecord2/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/config.yml
wget -P /minecraftly/bungeecord1/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/motd.yml
wget -P /minecraftly/bungeecord2/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/motd.yml
wget -P /minecraftly/spigot1/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/Minecraftly.jar
wget -P /minecraftly/spigot2/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/Minecraftly.jar
wget -P /minecraftly/spigot1/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/ProtocolLib.jar
wget -P /minecraftly/spigot2/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/ProtocolLib.jar
wget -P /minecraftly/spigot1/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/Vault.jar
wget -P /minecraftly/spigot2/plugins https://raw.githubusercontent.com/minecraftly/minecraftly/master/Vault.jar

#Build paperspigot server file, version 1.8.8
apt-get install git -y
wget -P /minecraftly/papertools https://ci.destroystokyo.com/job/PaperSpigot-BuildTools/lastSuccessfulBuild/artifact/target/PaperTools.jar 
cd /minecraftly/papertools && java -jar PaperTools.jar
cp /minecraftly/papertools/spigot-1.8.8.jar /minecraftly/spigot1/spigot.jar
cp /minecraftly/papertools/spigot-1.8.8.jar /minecraftly/spigot2/spigot.jar

# Start servers for the first time to generate files
cd /minecraftly/bungeecord1 && screen -dmS b1 java -jar BungeeCord.jar
sleep 30
screen -r b1 -X stuff 'end\n'
cd /minecraftly/bungeecord2 && screen -dmS b2 java -jar BungeeCord.jar
sleep 30
screen -r b2 -X stuff 'end\n'
cd /minecraftly/spigot1 && screen -dmS s1 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /minecraftly/worlds --port 25567
sleep 30
screen -r s1 -X stuff 'stop\n'
cd /minecraftly/spigot2 && screen -dmS s2 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /minecraftly/worlds --port 25568
sleep 30
screen -r s2 -X stuff 'stop\n'

# Configure some files
sed -i "s/ host: 0.0.0.0:.*/ host: 0.0.0.0:25565/" /minecraftly/bungeecord1/config.yml
sed -i "s/ host: 0.0.0.0:.*/ host: 0.0.0.0:25566/" /minecraftly/bungeecord2/config.yml
sed -i "s/ip_forward: .*/ip_forward: true/" /minecraftly/bungeecord1/config.yml
sed -i "s/ip_forward: .*/ip_forward: true/" /minecraftly/bungeecord2/config.yml
sed -i "s/motd: .*/motd: 'BungeeCord 1'/" /minecraftly/bungeecord1/config.yml
sed -i "s/motd: .*/motd: 'BungeeCord 2'/" /minecraftly/bungeecord2/config.yml
sed -i "s/md_5:/minecraftly:/" /minecraftly/bungeecord1/config.yml
sed -i "s/md_5:/minecraftly:/" /minecraftly/bungeecord2/config.yml
sed -i "s/heartbeatPort: .*/heartbeatPort: 25567/" /minecraftly/spigot1/plugins/Minecraftly/config.yml
sed -i "s/heartbeatPort: .*/heartbeatPort: 25568/" /minecraftly/spigot2/plugins/Minecraftly/config.yml
sed -i "s/address: localhost:.*/address: localhost:25567/" /minecraftly/bungeecord1/config.yml
sed -i "s/address: localhost:.*/address: localhost:25568/" /minecraftly/bungeecord2/config.yml
sed -i "s/server-id:.*/server-id: bungeecord1/" /minecraftly/bungeecord1/plugins/RedisBungee/config.yml
sed -i "s/server-id:.*/server-id: bungeecord2/" /minecraftly/bungeecord2/plugins/RedisBungee/config.yml
sed -i "s/level-name=.*/level-name=world1/" /minecraftly/spigot1/server.properties
sed -i "s/level-name=.*/level-name=world2/" /minecraftly/spigot2/server.properties
sed -i "s/online-mode=.*/online-mode=false/" /minecraftly/spigot1/server.properties
sed -i "s/online-mode=.*/online-mode=false/" /minecraftly/spigot2/server.properties
sed -i "s/bungeecord: .*/bungeecord: true/" /minecraftly/spigot1/spigot.yml
sed -i "s/bungeecord: .*/bungeecord: true/" /minecraftly/spigot2/spigot.yml
sed -i "s/connection-throttle: .*/connection-throttle: -1/" /minecraftly/spigot1/bukkit.yml
sed -i "s/connection-throttle: .*/connection-throttle: -1/" /minecraftly/spigot2/bukkit.yml

# Make some symbolic links so that both server share the same playerdata and achievement stats folders
mkdir -p /minecraftly/playerdata
mkdir -p /minecraftly/stats
mkdir -p /minecraftly/worlds/world1
mkdir -p /minecraftly/worlds/world2
ln -s /minecraftly/playerdata /minecraftly/worlds/world1/playerdata
ln -s /minecraftly/playerdata /minecraftly/worlds/world2/playerdata
ln -s /minecraftly/stats /minecraftly/worlds/world1/stats
ln -s /minecraftly/stats /minecraftly/worlds/world2/stats

# Start servers to play
cd /minecraftly/bungeecord1 && screen -dmS b1 java -jar BungeeCord.jar
cd /minecraftly/bungeecord2 && screen -dmS b2 java -jar BungeeCord.jar
cd /minecraftly/spigot1 && screen -dmS s1 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /minecraftly/worlds --port 25567
cd /minecraftly/spigot2 && screen -dmS s2 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /minecraftly/worlds --port 25568

# Congratulations! You can now access your servers via your IP:25565 and IP:25566
