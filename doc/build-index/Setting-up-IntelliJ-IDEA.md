Setting up IntelliJ IDEA
========================

For developing the TellMeFirst Indexes Builder based on a customized version of DBpedia Spotlight.

## Download IDEA
from http://www.jetbrains.com/idea/download/index.html

Java (JDK) must be installed for installing IDEA.
Download (http://www.oracle.com/technetwork/java/javase/downloads/index.html) and install the recent  version.

If you are using Linux, you may have some difficulty to install the jdk. So follow this tutorial (for Ubuntu):
http://www.webupd8.org/2011/09/how-to-install-oracle-java-7-jdk-in.html


(The guide below was created using version 11.1.1 of IDEA.)

## Install the Scala Plugin
On the start screen (when all projects are closed), on the top right corner:

Open Plugin Manager > Browse repositories > search for Scala > right-click on Scala > Download and install > Restart
(For new version of IDEA,configure > plugins >search for Scala > right-click on Scala > Download and install > Restart).

## Create project
File > New project... > Import project from external model > Maven > Root directory: $PATH_TO_DBPEDIA_SPOTLIGHT_CODE
(For new version of IDEA,import project > choose path > Import project from external model > Maven)
Tick "Import maven projects automatically"

Next > Next > Finish
