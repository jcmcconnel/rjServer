DJava
=====
So, basically I am building an application server.

Just to be clear, this is a manual implementation of the HTTP protocol.  That is to say, if you are looking for something shiny, I understand Java has an HttpServer package you can use.  In the same spirit, you may find other things manually implemented as the primary intent of this project is as an educational exercise.

The server has been tested behind an Nginx proxy, and seems to be responding stably.  

Primary Features
----------------
  - The `DjavaServer` class opens a socket.
  - `Responder` abstract class can be extended to serve content from any source desired. 
    - `PageResponder`s serve simple html from a configured root directory.
    - The goal with `FormResponder` is to eventually demonstrate input parsing and return dynamically generated content.  I.e. `{num: "5+3", para: "$num"}` might return `<p>8</p>`, for example.  



Current Status
==============
  - Great little echo bot, when used locally.  
  - Proxied interaction behind Nginx has been demonstrated.  
  - HTTP request implementation is in progress
    - GET requests are working just fine.
    - POST requests are being read in correctly, and responses are being returned.

Development Goals
================
  - Move Responders to their own projects, or at least packages.
  - Create system to load them on the fly.
  - Make an application for Recipejar

Updates
======
Chicken Wings patch is live! Thanks to @AggieNateHarris

### References
Initial code example from: https://www.geeksforgeeks.org/socket-programming-in-java/

