#!/bin/bash -x
#
# Script to install OnlineGlom.war on Ubuntu 10.10 using tomcat6.
#
# See this site for information on how to configure your server for OnlineGlom:
#
#   http://www.glom.org/wiki/index.php?title=Development/OnlineGlom#Deployment
#

echo "WARNING: Please consider this script documentation of the deployment proceedure"
echo "         rather than a general script the will work with all servers. Edit this"
echo "         script and remove the 'exit 1' line after this message if you want"
echo "         you want to use it."

# Do not remove or comment out this exit command if do not understand what this script does
exit 1

# compile and install the latest version java-libglom
pushd ~/gnome/sources/java-libglom
git pull
jhbuild run make clean
jhbuild run make
jhbuild run make install

# stop tomcat before we install the jar for tomcat's lib directory
sudo /etc/init.d/tomcat6 stop
sudo mv java-libglom-0.1.jar /usr/share/tomcat6/lib/
sudo chown root:root /usr/share/tomcat6/lib/java-libglom-0.1.jar
popd

# clean things up and install the war
sudo rm -rf /var/cache/tomcat6/* /var/lib/tomcat6/webapps/{OnlineGlom,OnlineGlom.war}
sudo mv ~/OnlineGlom.war /var/lib/tomcat6/webapps/
sudo /etc/init.d/tomcat6 start
