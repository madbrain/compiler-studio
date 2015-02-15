#!/bin/sh

CS_HOME=`dirname $0`/.. 

exec java -jar ${CS_HOME}/lib/compiler-studio-gui-${project.version}.jar $*
