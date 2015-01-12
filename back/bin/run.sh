#!/bin/sh

CONF_DIRECTORY=../conf

#cd ../rest

#mvn clean install exec:java -Dexec.mainClass="it.polito.tellmefirst.web.rest.TMFServer" -Dexec.args="$CONF_DIRECTORY/server.properties"

nohup java -jar jetty-runner-9.1.4.v20140401.jar --port 8888 tmfpak.war &
