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
wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u5-b13/jdk-8u5-linux-x64.tar.gz
mkdir /opt/jdk
tar -zxf jdk-8u5-linux-x64.tar.gz -C /opt/jdk
ls /opt/jdk
update-alternatives --install /usr/bin/java java /opt/jdk/jdk1.8.0_05/bin/java 100
update-alternatives --install /usr/bin/javac javac /opt/jdk/jdk1.8.0_05/bin/javac 100

# Download some preconfigured files
wget -P /minecraft/server1 https://ci.destroystokyo.com/job/PaperSpigot/443/artifact/Paperclip.jar
wget -P /minecraft/server2 https://ci.destroystokyo.com/job/PaperSpigot/443/artifact/Paperclip.jar
wget -P /minecraft/server1/plugins https://ci.m.ly/job/Minecraftly/lastSuccessfulBuild/artifact/core-bukkit-1.8/build/libs/Minecraftly-1.0-SNAPSHOT.jar
wget -P /minecraft/server2/plugins https://ci.m.ly/job/Minecraftly/lastSuccessfulBuild/artifact/core-bukkit-1.8/build/libs/Minecraftly-1.0-SNAPSHOT.jar
wget -P /minecraft/server1/plugins http://dev.bukkit.org/media/files/894/359/Vault.jar
wget -P /minecraft/server2/plugins http://dev.bukkit.org/media/files/894/359/Vault.jar
wget -P /minecraft/server1/plugins http://ci.dmulloy2.net/job/ProtocolLib/224/artifact/target/ProtocolLib.jar
wget -P /minecraft/server2/plugins http://ci.dmulloy2.net/job/ProtocolLib/224/artifact/target/ProtocolLib.jar
wget -P /minecraft/proxy1 http://ci.md-5.net/job/BungeeCord/1119/artifact/bootstrap/target/BungeeCord.jar
wget -P /minecraft/proxy2 http://ci.md-5.net/job/BungeeCord/1119/artifact/bootstrap/target/BungeeCord.jar
wget -P /minecraft/proxy1/plugins https://ci.m.ly/job/Minecraftly/lastSuccessfulBuild/artifact/core-bungee-1.8/build/libs/MinecraftlyBungee-1.0-SNAPSHOT.jar
wget -P /minecraft/proxy2/plugins https://ci.m.ly/job/Minecraftly/lastSuccessfulBuild/artifact/core-bungee-1.8/build/libs/MinecraftlyBungee-1.0-SNAPSHOT.jar
wget -P /minecraft/proxy1/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/core-bungee-1.8/config.yml
wget -P /minecraft/proxy2/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/core-bungee-1.8/config.yml
wget -P /minecraft/proxy1/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/core-bungee-1.8/motd.yml
wget -P /minecraft/proxy2/plugins/MinecraftlyBungee https://raw.githubusercontent.com/minecraftly/minecraftly/master/core-bungee-1.8/motd.yml
wget -P /minecraft/proxy1/plugins http://ci.md-5.net/job/RedisBungee/534/artifact/target/RedisBungee-0.3.8-INTERIM.jar
wget -P /minecraft/proxy2/plugins http://ci.md-5.net/job/RedisBungee/534/artifact/target/RedisBungee-0.3.8-INTERIM.jar

# Start servers for the first time to generate files
cd /minecraft/proxy1 && screen -dmS proxy1 java -jar BungeeCord.jar
sleep 30
screen -r proxy1 -X stuff 'end\n'
cd /minecraft/proxy2 && screen -dmS proxy2 java -jar BungeeCord.jar
sleep 30
screen -r proxy2 -X stuff 'end\n'
cd /minecraft/server1 && screen -dmS server1 java -Dcom.mojang.eula.agree=true -jar Paperclip.jar --world-dir /minecraftly/worlds --port 25567
sleep 30
screen -r server1 -X stuff 'stop\n'
cd /minecraft/server2 && screen -dmS server2 java -Dcom.mojang.eula.agree=true -jar Paperclip.jar --world-dir /minecraftly/worlds --port 25568
sleep 30
screen -r server2 -X stuff 'stop\n'

# Configure some files
sed -i "s/ host: 0.0.0.0:.*/ host: 0.0.0.0:25565/" /minecraft/proxy1/config.yml
sed -i "s/ host: 0.0.0.0:.*/ host: 0.0.0.0:25566/" /minecraft/proxy2/config.yml
sed -i "s/ip_forward: .*/ip_forward: true/" /minecraft/proxy1/config.yml
sed -i "s/ip_forward: .*/ip_forward: true/" /minecraft/proxy2/config.yml
sed -i "s/motd: .*/motd: '1'/" /minecraft/proxy1/config.yml
sed -i "s/motd: .*/motd: '2'/" /minecraft/proxy2/config.yml
sed -i "s/md_5:/minecraftly:/" /minecraft/proxy1/config.yml
sed -i "s/md_5:/minecraftly:/" /minecraft/proxy2/config.yml
sed -i "s/address: localhost:.*/address: localhost:25567/" /minecraft/proxy1/config.yml
sed -i "s/address: localhost:.*/address: localhost:25568/" /minecraft/proxy2/config.yml
sed -i "s/heartbeatPort: .*/heartbeatPort: 25567/" /minecraft/server1/plugins/Minecraftly/config.yml
sed -i "s/heartbeatPort: .*/heartbeatPort: 25568/" /minecraft/server2/plugins/Minecraftly/config.yml
sed -i "s/server-id:.*/server-id: proxy1/" /minecraft/proxy1/plugins/RedisBungee/config.yml
sed -i "s/server-id:.*/server-id: proxy2/" /minecraft/proxy2/plugins/RedisBungee/config.yml
sed -i "s/level-name=.*/level-name=world1/" /minecraft/server1/server.properties
sed -i "s/level-name=.*/level-name=world2/" /minecraft/server2/server.properties
sed -i "s/online-mode=.*/online-mode=false/" /minecraft/server1/server.properties
sed -i "s/online-mode=.*/online-mode=false/" /minecraft/server2/server.properties
sed -i "s/bungeecord: .*/bungeecord: true/" /minecraft/server1/spigot.yml
sed -i "s/bungeecord: .*/bungeecord: true/" /minecraft/server2/spigot.yml
sed -i "s/connection-throttle: .*/connection-throttle: -1/" /minecraft/server1/bukkit.yml
sed -i "s/connection-throttle: .*/connection-throttle: -1/" /minecraft/server2/bukkit.yml

# Make some symbolic links so that both server share the same playerdata and achievement stats folders
mkdir -p /minecraft/playerdata
mkdir -p /minecraft/stats
mkdir -p /minecraft/worlds/world1
mkdir -p /minecraft/worlds/world2
ln -s /minecraft/playerdata /minecraft/worlds/world1/playerdata
ln -s /minecraft/playerdata /minecraft/worlds/world2/playerdata
ln -s /minecraft/stats /minecraft/worlds/world1/stats
ln -s /minecraft/stats /minecraft/worlds/world2/stats
rm -rf /minecraft/worlds/world
rm -rf /minecraft/worlds/world_nether
rm -rf /minecraft/worlds/world_the_end

# Start servers to play
cd /minecraft/proxy1 && screen -dmS proxy1 java -jar BungeeCord.jar
cd /minecraft/proxy2 && screen -dmS proxy2 java -jar BungeeCord.jar
cd /minecraft/server1 && screen -dmS server1 java -Dcom.mojang.eula.agree=true -jar Paperclip.jar --world-dir /minecraft/worlds --port 25567
cd /minecraft/server2 && screen -dmS server2 java -Dcom.mojang.eula.agree=true -jar Paperclip.jar --world-dir /minecraft/worlds --port 25568

# Congratulations! You can now access your servers via your IP:25565 and IP:25566
