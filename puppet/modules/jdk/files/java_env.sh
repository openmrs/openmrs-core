#!/bin/sh
export JAVA_HOME="/usr/"
export PATH=$PATH:$HOME/bin:$JAVA_HOME/bin
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:NewSize=128m"