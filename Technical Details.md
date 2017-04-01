# Technical details document

This document describes the internal details that were too technical to describe in the readme. This includes things such as communication protocols and multi-threading structures.

## IDs

The user ID is a single integer made up of 4 bytes as follows:
* Unused / Error code
* Protocol ID
* Game ID
* Player ID

The protocol ID dictates what method the client library uses to connect with the server. At the moment the only valid value is 1.
The game ID is an index into a global pool of games. This dictates what game the player joines when they call connect.
The player ID is an index into an array specific to each game. Meaning that two IDs can have the same player ID provided they are part of different games.

## Player management

When an ID is requested via the site a space is reserved in that games' player array. When a connection is made using that ID that slot is set to active and the connection details (port, ip) are saved. Any connections using the same ID will be denied. Until the slot is made active that ID may be re-used for any reason. Upon disconnect the ID is returned to the pool and the connection details are cleared.

Player are expected to send in commands once per game tick. Failure to do so will result in the player being diconnected from the server and returned to a non-active state.

## Comunication protocol

The UDP connection protocol between the server and the client is relatively simple. Each packet from the client to the server is their User ID followed by some data. All numerical types are byte alligned and send as is. Strings are sent with two bytes preceding them describing the length followed by the character data encoded in UTF-8. Booleans are bit packed with the LSB being the first bit to be read. Remaining bits in the written byte are padded to maintain the byte allignment.

The communication from the server to the client has no ID but does have a single byte header dictating the message type.

| Byte | Meaning                            |
|:-----|:-----------------------------------|
| -1   | Error, followed by a status string |
| 0    | Game Data                          |
| 1    | Disconnect, no further data        |
| 2    | Timed out, no further data         |

## Threading information

At the moment there is a UDP listener thread, one thread per game, and the tomcat server thread.

The Game loop / Network system is synchronized through the game object monitor.
The Game - ID mapping is synchronized through a ReentantReadWriteLock.
The Display Viewer list (web clients) is synchronized through the display object monitor.
The Game Type list is synchronized through the a ReentrantReadWriteLock.
The Game Type table listeners are synchronized through the gameType object monitor.