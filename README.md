# Script Wars

## Intro
Script Wars is a AI design competition designed for university software students. Participants write small AI programs using the supplied library and connect to a competition server. The AIs then compete against each other in simple games for glory.

## How to take part
At the moment this project is just in the development stage and has no server running at the moment. Despite this you can still write AIs using the client library and run the server youself if you feel the urge to get ahead of the competition.

## How to contribute
While I would like to keep the development of the core system to myself there are several opportunities to speed this project along.
* Creating a new game by extending the [Game class](https://github.com/Brownshome/script-wars/blob/master/server/brownshome/scriptwars/server/game/Game.java). This requires no specialist knowledge or advanced java skills and the class is, or at least will be soon, well documented. All this needs is a cool idea.
* Making the site look nice. I am looking for someone to work with css and javascript to give the site a nice clean minimalistic white theme. As this might be a big job please to talk to me first before about it before re-formatting every html file in the project. I will get around to this myself eventually after the main functionality is polished but I'd be glad for help here.
* Write more client libraries. At the moment the only client API is written in Java. The server is built to be language agnostic but I do need language specific libraries. The protocol for the connection is documented and relatively simple. I would prefer more widly used languages but feel free to build one for your favorite esoteric printer garbage lookalike, looking at you [Malbolge](https://en.wikipedia.org/wiki/Malbolge).

For any of the items above submit a pull request or get in contact depending on how much work you are planning to do. As for contributing other features or fixes use your common sense. Ask me first before you start anything big in case it is A) already being implemented or B) not wanted. For small fixes or requests submit an issue as at this stage the code base is too fluid for pull requests to be worth anything for small fixes.

## Building
I will be uploading a [gradle](https://gradle.org/) based solution soon but if you simply can't wait the main source file is `/server` and `WebContent` is the standard javaee web content directory. The deployment server is [Tomcat 9.0](http://tomcat.apache.org/). Your millage may vary with other versions / servers.

For those of you who have no idea what this means. Hang tight. I will be adding a much more user friendly guide once the new build system is running.
