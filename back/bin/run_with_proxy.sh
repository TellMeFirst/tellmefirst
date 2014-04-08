#!/bin/sh

CONF_DIRECTORY=../conf

#cd ../rest

#mvn clean install exec:java -Dexec.mainClass="it.polito.tellmefirst.web.rest.TMFServer" -Dexec.args="$CONF_DIRECTORY/server.properties" -Dhttp.proxyHost=localhost -Dhttp.proxyPort=3128 -Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128 -Dhttp.nonProxyHosts="localhost|127.0.0.1"

nohup java -Dhttp.proxyHost=192.168.93.188 -Dhttp.proxyPort=81 -Dhttps.proxyHost=192.168.93.188 -Dhttps.proxyPort=81 -Dhttp.nonProxyHosts="localhost|127.0.0.1" -jar jetty-runner-9.1.4.v20140401.jar --port 8888 tmfpak.war &