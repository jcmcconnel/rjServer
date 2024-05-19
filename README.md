Tsaiger
=====
So, basically I am building an application server or more precisely, a simple Java based server to serve dynamic content while proxied behind a first-class web-server like Nginx or Apache.

Just to be clear, this is a manual implementation of the HTTP protocol.
That is to say, if you are looking for something shiny, I understand Java has an HttpServer package you can use.
In the same spirit, you may find other things manually implemented as the primary intent of this project is as an educational exercise, and for personal use, as my hope is to eventually migrate my personally website to it primarily.  Maybe a bit lofty of a goal, but that is where my compass needle is pointing.

The server has been tested behind both Nginx and Apache proxies, and seems to be responding stably.

It is currently using about 2 threads.

Primary Features
----------------
  - Dynamic class loading for easily creating custom responders.
  - Simple content responder included: 'responder.BasicResponder'.
  - Responders can be given a list of file extensions for filtering.
  - Online help
  - Interactive mode
    - start/stop/restart the server and add/remove responders on the fly, etc.
  - Config files are supported as well.


Current Status
==============
  - Interative mode: Working. Additional commands are under consideration.
  - Online help: Working
  - Dynamic loading of custom responders: Working
  - HTTP request implementation: *in progress*, Currently compatible with HTTP 1.0.
    - GET requests: Working.
    - POST requests: Working.

Development Goals
================
  - [-] Move Responders to their own projects, or at least packages.
  - [X] Create system to load them on the fly.
  - [ ] ~~Migrate to HttpServer, or at least integrate it as an option.~~ 
    - Will not do.  Using SocketChannels manually.
  - [-] Make it multi-threaded for additional capacity.
    - *In Progress:* Here's an interesting question: What makes multi-threading better?  According to my research, using additional threads only improves client load handling by an order of magnitude. Apparently the preferred method is to use SocketChannels and looping to accept client connection requests, read, and write depending on the state of the saved connection.  This allows a single thread to handle massively greater connection volumes than multi-threading, while at the same time keeping the free threads available for the heavy lifting of request processing.  i.e.: One thread handles connection management, additional threads can be used for data processing.

In Progress
===========
I am prototyping an interpreter for a scripting language for one of the responders.  Currently very alpha.
It's in the test package and a test case can be run with the following command:

```
java src/TEST/TGScriptParser.java src/TEST/test.tgs
```

Acknowledgments
======
  - Chicken Wings patch is live! Thanks to @AggieNateHarris
  - Initial inspiration from: https://www.geeksforgeeks.org/socket-programming-in-java/

