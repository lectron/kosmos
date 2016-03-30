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
# Requirements: This script works with Debian 8 or higher.
#
###############################################################################

#Install screen to let multiple Minecraft sessions run in background
sudo apt-get install screen -y

#Install MySQL database server
#username: "root", 
#no password 
#database name: "minecraftly"
sudo export DEBIAN_FRONTEND=noninteractive
sudo echo "mysql-server-5.5 mysql-server/root_password password 123456" | debconf-set-selections
sudo echo "mysql-server-5.5 mysql-server/root_password_again password 123456" | debconf-set-selections
sudo apt-get -y install mysql-server-5.5
sudo mysql -u root -p123456 -e "create database minecraftly;"
sudo mysqladmin -u root -p123456 password ''

#Install Redis NoSQL server
sudo apt-get install redis-server -y

#Install latest Java version
sudo echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
sudo echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
sudo echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo apt-get update -y
sudo apt-get install oracle-java8-set-default -y

#Make some directories & download some preconfigured files
sudo mkdir /minecraftly
sudo wget -P /minecraftly/bungeecord1 https://storage.googleapis.com/minecraftly/test/BungeeCord.jar
sudo wget -P /minecraftly/bungeecord2 https://storage.googleapis.com/minecraftly/test/BungeeCord.jar
sudo wget -P /minecraftly/bungeecord1/plugins https://storage.googleapis.com/minecraftly/test/RedisBungee.jar
sudo wget -P /minecraftly/bungeecord2/plugins https://storage.googleapis.com/minecraftly/test/RedisBungee.jar
sudo wget -P /minecraftly/bungeecord1/plugins https://storage.googleapis.com/minecraftly/test/MinecraftlyBungee.jar
sudo wget -P /minecraftly/bungeecord2/plugins https://storage.googleapis.com/minecraftly/test/MinecraftlyBungee.jar
sudo wget -P /minecraftly/bungeecord1/plugins/MinecraftlyBungee https://storage.googleapis.com/minecraftly/test/config.yml
sudo wget -P /minecraftly/bungeecord2/plugins/MinecraftlyBungee https://storage.googleapis.com/minecraftly/test/config.yml
sudo wget -P /minecraftly/bungeecord1/plugins/MinecraftlyBungee https://storage.googleapis.com/minecraftly/test/motd.yml
sudo wget -P /minecraftly/bungeecord2/plugins/MinecraftlyBungee https://storage.googleapis.com/minecraftly/test/motd.yml
sudo wget -P /minecraftly/spigot1 https://storage.googleapis.com/minecraftly/test/spigot.jar
sudo wget -P /minecraftly/spigot2 https://storage.googleapis.com/minecraftly/test/spigot.jar
sudo wget -P /minecraftly/pigots1/plugins https://storage.googleapis.com/minecraftly/test/Minecraftly.jar
sudo wget -P /minecraftly/spigot2/plugins https://storage.googleapis.com/minecraftly/test/Minecraftly.jar
sudo wget -P /minecraftly/spigot1/plugins https://storage.googleapis.com/minecraftly/test/ProtocolLib.jar
sudo wget -P /minecraftly/spigot2/plugins https://storage.googleapis.com/minecraftly/test/ProtocolLib.jar
sudo wget -P /minecraftly/spigot1/plugins https://storage.googleapis.com/minecraftly/test/Vault.jar
sudo wget -P /minecraftly/spigot2/plugins https://storage.googleapis.com/minecraftly/test/Vault.jar

#Start servers for the first time to generate files
sudo cd /m/b1 && screen -dmS b1 java -jar BungeeCord.jar
sudo sleep 60
sudo screen -r b1 -X stuff 'end\n'
sudo cd /m/b2 && screen -dmS b2 java -jar BungeeCord.jar
sudo sleep 60
sudo screen -r b2 -X stuff 'end\n'
sudo cd /m/s1 && screen -dmS s1 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /mnt/worlds --port 25567
sudo sleep 60
sudo screen -r s1 -X stuff 'stop\n'
sudo cd /m/s2 && screen -dmS s2 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /mnt/worlds --port 25568
sudo sleep 60
sudo screen -r s2 -X stuff 'stop\n'

#Configure some files
sudo sed -i "s/ host: 0.0.0.0:.*/ host: 0.0.0.0:25565/" /minecraftly/bungeecord1/config.yml
sudo sed -i "s/ host: 0.0.0.0:.*/ host: 0.0.0.0:25566/" /minecraftly/bungeecord2/config.yml
sudo sed -i "s/ip_forward: .*/ip_forward: true/" /minecraftly/bungeecord1/config.yml
sudo sed -i "s/ip_forward: .*/ip_forward: true/" /minecraftly/bungeecord2/config.yml
sudo sed -i "s/motd: .*/motd: 'BungeeCord 1'/" /minecraftly/bungeecord1/config.yml
sudo sed -i "s/motd: .*/motd: 'BungeeCord 2'/" /minecraftly/bungeecord2/config.yml
sudo sed -i "s/md_5:/minecraftly:/" /minecraftly/bungeecord1/config.yml
sudo sed -i "s/md_5:/minecraftly:/" /minecraftly/bungeecord2/config.yml
sudo sed -i "s/heartbeatPort: .*/heartbeatPort: 25567/" /minecraftly/spigot1/plugins/Minecraftly/config.yml
sudo sed -i "s/heartbeatPort: .*/heartbeatPort: 25568/" /minecraftly/spigot2/plugins/Minecraftly/config.yml
sudo sed -i "s/address: localhost:.*/address: localhost:25567/" /m/bungeecord1/config.yml
sudo sed -i "s/address: localhost:.*/address: localhost:25568/" /m/bungeecord2/config.yml
sudo HOSTNAME=$(ifconfig eth0 | grep "inet addr" | cut -d ':' -f 2 | cut -d ' ' -f 1)
sudo sed -i "s/server-id:.*/server-id: $HOSTNAME/" /minecraftly/bungeecord1/plugins/RedisBungee/config.yml
sudo sed -i "s/server-id:.*/server-id: $HOSTNAME/" /minecraftly/bungeecord2/plugins/RedisBungee/config.yml
sudo sed -i "s/level-name=.*/level-name=world1/" /minecraftly/spigot1/server.properties
sudo sed -i "s/level-name=.*/level-name=world2/" /minecraftly/spigot2/server.properties
sudo sed -i "s/online-mode=.*/online-mode=false/" /minecraftly/spigot1/server.properties
sudo sed -i "s/online-mode=.*/online-mode=false/" /minecraftly/spigot2/server.properties
sudo sed -i "s/bungeecord: .*/bungeecord: true/" /minecraftly/spigot1/spigot.yml
sudo sed -i "s/bungeecord: .*/bungeecord: true/" /minecraftly/spigot2/spigot.yml
sudo sed -i "s/connection-throttle: .*/connection-throttle: -1/" /minecraftly/spigot1/bukkit.yml
sudo sed -i "s/connection-throttle: .*/connection-throttle: -1/" /minecraftly/spigot2/bukkit.yml

#Start servers to play
sudo cd /minecraftly/b1 && screen -dmS b1 java -jar BungeeCord.jar
sudo cd /minecraftly/b2 && screen -dmS b2 java -jar BungeeCord.jar
sudo cd /minecraftly/spigot1 && screen -dmS s1 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /mnt/worlds --port 25567
sudo cd /minecraftly/spigot2 && screen -dmS s2 java -Dcom.mojang.eula.agree=true -jar spigot.jar --world-dir /mnt/worlds --port 25568
