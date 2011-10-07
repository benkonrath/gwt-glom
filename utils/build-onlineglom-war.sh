#!/bin/bash
#
# Script to build the OnlineGlom.war file.
#

# confirm that we're in the right dir
lastdir=$(pwd | sed -e "s|/| |g" | awk '{print $NF}')
if [ $lastdir != "gwt-glom" ]; then
  scriptname=$(echo $0 | sed -e "s|^\./||")
  echo $scriptname":"
  echo "   Please run this script from the gwt-glom directory with this command:"
  echo "   ./utils/"$scriptname
  exit 1
fi


echo "Removing generated files ... "

rm --interactive=never -rfv src/main/webapp/WEB-INF/classes/* \
                            src/main/webapp/WEB-INF/lib/* \
                            src/main/webapp/WEB-INF/deploy/* \
                            src/main/webapp/OnlineGlom/* \
                            src/main/webapp/logs/* \
                            src/main/gwt-unitCache \
                            target/* target/.generated

echo "done"

echo "Building and creating OnlineGlom.war. This can take a couple of minutes ... "

mvn clean package

if [ $? -ne 0 -o ! -f target/gwt-glom-1.0-SNAPSHOT.war ]; then
  echo "Error building OnlineGlom."
  exit 1
fi

mv target/{gwt-glom-1.0-SNAPSHOT,OnlineGlom}.war

echo "done"

echo "The deployable war can be found here:"
echo ""
echo "   ./target/OnlineGlom.war"
echo ""
echo "You can scp or sftp it to your server for deployment."
