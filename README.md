DJava
=====
So, basically I'm building an application server.  The DjavaServer class opens a socket.  Nginx can be configured to proxy that localhost port.  


Current Status
==============
  - Great little echo bot, when used locally.  
  - Proxied interaction behind Nginx on localhost has been demonstrated.  
  - HTTP GET request implementation is in progress
    - Currently responds on hard coded values: "/", "/test", "/test1" and returns error for all others.

### References
Initial code example from: https://www.geeksforgeeks.org/socket-programming-in-java/

