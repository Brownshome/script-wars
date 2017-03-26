# Script Wars

## Intro
Script Wars is a AI design competition designed for university software students. Participants write small AI programs using the supplied library and connect to a competition server. The AIs then compete against each other in simple games for glory.

## How to take part
~~At the moment this project is just in the development stage and has no server running at the moment.~~ **There is a running server!! Join [here](http://13.55.154.170) (Still in development). I forgot to note the port needed to connect to the server; it is 35565. This makes the connection line** `Network.connect(ID, "13.55.154.170", 35565, "John Smith")` ~~Despite this you can still write AIs using the client library and run the server youself if you feel the urge to get ahead of the competition.~~

## How to contribute
While I would like to keep the development of the core system to myself there are several opportunities to speed this project along.
* Creating a new game by extending the [Game class](https://github.com/Brownshome/script-wars/blob/master/server/brownshome/scriptwars/server/game/Game.java). This requires no specialist knowledge or advanced java skills and the class is, or at least will be soon, well documented. All this needs is a cool idea.
* Making the site look nice. I am looking for someone to work with css and javascript to give the site a nice clean minimalistic white theme. As this might be a big job please to talk to me first before about it before re-formatting every html file in the project. I will get around to this myself eventually after the main functionality is polished but I'd be glad for help here.
* Write more client libraries. At the moment the only client API is written in Java. The server is built to be language agnostic but I do need language specific libraries. The protocol for the connection is documented and relatively simple. I would prefer more widly used languages but feel free to build one for your favorite esoteric printer garbage lookalike, looking at you [Malbolge](https://en.wikipedia.org/wiki/Malbolge).

For any of the items above submit a pull request or get in contact depending on how much work you are planning to do. As for contributing other features or fixes use your common sense. Ask me first before you start anything big in case it is A) already being implemented or B) not wanted. For small fixes or requests submit an issue as at this stage the code base is too fluid for pull requests to be worth anything for small fixes.

## Building & Downloading
Download the [git client](https://desktop.github.com/) (or the git command line tools) and fork the project into a directory, edit the files and make a pull request.

This project is using a [gradle](https://gradle.org/) based solution. Install the gradle tool and use the command `gradle war` to compile the server files and `gradle clientJar` to make the client library. Drop this file in the server webApps folder (or equivalent). The deployment server is [Tomcat 9.0](http://tomcat.apache.org/). Your millage may vary with other versions / servers may vary.

To use eclipse with the project, install the javaee version of eclipse or upgrade your installation with the needed plugins. Install the tomcat server to your disk. Click `new project` and go to `Dynamic Web Project` point the project location to the folder you placed the git repo. Set the server runtime to Tomcat 9.0 and the module version to 3.1.

Click next and remove the `src` folder. Add `server` and `client/java` to the source path. Then click finish. You should be able to now build and run the project via eclipse.

The ability to run the project directly from gradle may be added later.

For more information please see the [technical details document](https://github.com/Brownshome/script-wars/blob/master/Technical%20Details.md)
