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
jhbuild run make distclean
jhbuild buildone --force java-libglom
jhbuild run make check
if [ $? -gt 0 ]; then
  exit 1
fi
popd

# stop tomcat before we install the jar for tomcat's lib directory
sudo /etc/init.d/tomcat6 stop

# remove old versions of java-libglom
sudo rm -rf /usr/share/tomcat6/lib/java-libglom-*.jar

# big hack -- I should probably create a pkg-config file for java-libglom
JLG_VERSION=$(cat ~/gnome/sources/java-libglom/configure.ac | grep AC_INIT | cut -d '[' -f 3 | cut -d ']' -f 1)
sudo mv ~/gnome/sources/java-libglom/java-libglom-$JLG_VERSION.jar /usr/share/tomcat6/lib/
sudo chown root:root /usr/share/tomcat6/lib/java-libglom-$JLG_VERSION.jar

# clean things up and install the war
sudo rm -rf /var/cache/tomcat6/* /var/lib/tomcat6/webapps/{OnlineGlom,OnlineGlom.war}
sudo mv ~/OnlineGlom.war /var/lib/tomcat6/webapps/
sudo /etc/init.d/tomcat6 start
