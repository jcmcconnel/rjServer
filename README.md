DJava
=====
So, basically I am building an application server or more precisely, a simple Java based server to serve dynamic content while proxied behind a first-class web-server like Nginx or Apache.

Just to be clear, this is a manual implementation of the HTTP protocol.
That is to say, if you are looking for something shiny, I understand Java has an HttpServer package you can use.
In the same spirit, you may find other things manually implemented as the primary intent of this project is as an educational exercise, and for personal use, as my hope is to eventually migrate my personally website to it primarily.  Maybe a bit lofty of a goal, but that is where my compass needle is pointing.

I really need to come up with a different name, as originally I meant it as a bit of a joke, because I started it after trying Django.  I'm sorry, but I don't like Django.  Maybe it would be great for something more high-powered than what I need, but there you go.

The server has been tested behind both Nginx and Apache proxies, and seems to be responding stably.

It is currently single threaded.

Primary Features
----------------
  - Dynamic class loading for easily creating custom responders.
  - Static html responder included: 'responder.StaticResponder'.
  - Online help
  - Interactive mode
    - start/stop/restart the server and add/remove responders on the fly, etc.
  - Config files are supported as well.


Current Status
==============
  - Interative mode: working, additional commands are under consideration.
  - Online help: working
  - Dynamic loading of custom responders: working
  - HTTP request implementation: *in progress*, Currently compatible with HTTP 1.0.
    - GET requests: working.
    - POST requests: working.

Development Goals
================
  - [-] Move Responders to their own projects, or at least packages.
  - [X] Create system to load them on the fly.
  - [ ] Migrate to HttpServer, or at least integrate it as an option.
  - [ ] Make it multi-threaded for additional capacity.

Acknowledgments
======
  - Chicken Wings patch is live! Thanks to @AggieNateHarris
  - Initial inspiration from: https://www.geeksforgeeks.org/socket-programming-in-java/

