#!/bin/sh

###############################################################################
# Create a link to this file to execute jpdfbookmarks gui on linux or other 
# Unix like systems in a folder in the PATH environment variable. For example  
# as root type:
#
# $ ln -s  link_this_in_linux_path.sh /usr/local/bin/jpdfbookmarks_gui
#
###############################################################################

JAR_NAME=jpdfbookmarks.jar

PATH_TO_TARGET=`readlink -f $0`
DIR_OF_TARGET=`dirname $PATH_TO_TARGET`

# ADD logging config file
JVM_OPTIONS="-Xms64m -Xmx512m -Dorg.jpedal.jai=true -Djava.util.logging.config.file=$DIR_OF_TARGET/conf/jpdfbookmarks.logging.properties"

if [ -n "$JAVA_HOME" ]; then
  "$JAVA_HOME/bin/java" $JVM_OPTIONS -jar "$DIR_OF_TARGET/$JAR_NAME" "$@"
else
  java $JVM_OPTIONS -jar "$DIR_OF_TARGET/$JAR_NAME" "$@"
fi

