DJava
=====
So, basically building an application server.  The DjavaServer class opens a socket.  Nginx can be configured to proxy that localhost port.  
Responder abstract class can be extended to serve content from any source desired. 

PageResponder is a simple extension of the Responder class to server html files from from a given root directory;


Current Status
==============
  - Great little echo bot, when used locally.  
  - Proxied interaction behind Nginx has been demonstrated.  
  - HTTP request implementation is in progress

Updates
======
Chicken Wings patch is live! Thanks to @AggieNateHarris

### References
Initial code example from: https://www.geeksforgeeks.org/socket-programming-in-java/

