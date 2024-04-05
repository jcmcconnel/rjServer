package server;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.reflect.Constructor;


public class Server implements Runnable
{
   private Socket socket;
   private ServerSocket serverSocket;
   private InputStream in;
   private PrintWriter out;

   private HashMap<String, server.util.AbstractResponder> responders;
   private server.util.AbstractResponder errorResponder;

   private HashMap<String, Object> serverState;

   private StringWriter messages;
   private PrintWriter msgOut;

   private static URLClassLoader responderLoader;
   
   public Server()
   {
      socket = null;
      serverSocket = null;
      in = null;
      out = null;

      messages = new StringWriter();
      msgOut = new PrintWriter(messages);

      serverState = new HashMap<String, Object>();

      responders = new HashMap<String, server.util.AbstractResponder>();
      responders.put("/error", new server.util.AbstractResponder("./", "error") {
         protected String getBody(String target){
            return getDefaultErrorBody("Unknown Error: "+target);
         }
      });
   }

   public void addResponder(String className, String rootDir, String endPoint) throws ReflectiveOperationException {
      File root = new File(rootDir);
      System.out.println("adding responder");
      if(root.isDirectory()) {
         Class rclass = responderLoader.loadClass(className);
         Constructor rConstructor = rclass.getConstructor(String.class, String.class);
         server.util.AbstractResponder r;
         r = (server.util.AbstractResponder)rConstructor.newInstance(rootDir, endPoint);
         responders.put(endPoint, r);
         System.out.println("responder added");
      } else System.out.println("Application root is not a directory.");
   }

   public void removeResponder(String endPoint){
      responders.remove(endPoint);
   }

   public void addClassLoader(ClassLoader cl){
      responderLoader = (URLClassLoader)cl;
   }
   
   public void run()
   {
      try {
         // starts server and waits for a connection
         if(serverState.containsKey("port") && serverState.get("port") != null) 
            serverSocket = new ServerSocket(((Integer)serverState.get("port")).intValue());
         else return;
         serverState.put("state", "running");
         msgOut.println("Server started");
         while(serverState.get("state").equals("running")) clientSession();
      }
      catch(SocketException e){
         msgOut.println("Socket closed");
      }
      catch(IOException i)
      {
         msgOut.println(i);
      }
   }

   public void start(){
       serverState.put("current-thread", new Thread(this));
       ((Thread)serverState.get("current-thread")).start();
   }

   public void stopServer() throws IOException {
      serverState.put("state", "stopped");
      if(serverSocket != null) serverSocket.close();
   }

   public boolean isRunning(){
      if(serverState.containsKey("state")) return serverState.get("state").equals("running");
      else return false;
   }

   public void changeState(String key, Object value){
      serverState.put(key, value);
   }

   public String readState(String key){
      return serverState.get(key).toString();
   }

   private void clientSession() throws IOException
   {
      HashMap<String, String> request = new HashMap<String, String>();
      HashMap<String, String> response = new HashMap<String, String>();
   
      msgOut.println("Waiting for a client ...");
   
      socket = serverSocket.accept();
      msgOut.println("Client accepted");
   
      // takes input from the client socket

      in = socket.getInputStream();
   
      out = new PrintWriter(socket.getOutputStream());
   
      String line = this.readLine(in);
   
      request.put("request-line", line);

      if(!line.trim().equals("")) {
         line = this.readLine(in);
         if(line.equals("echoMode")){
            // reads message from client until "Over" is sent
            while (!line.equals("Over"))
            {
               if(in.available() > 0) {
                  line = this.readLine(in);
                  System.out.println(line);
                  out.println("This is what I received: "+line);
                  out.flush();
               }
   
            }
         } else {
            msgOut.println("Accepted client in http mode");
            readInRequest(in, request);
            response = getResponder(request).getResponse(request);
            writeResponse(out, response);
         }
      }
      msgOut.println("Closing connection");
   
      // close connection
      in.close();
      socket.close();
   }

   private String readLine(InputStream in) throws IOException {
      int c = in.read();
      StringBuilder s = new StringBuilder();
      while(c != -1 && c != '\n' && in.available() > 0){
          if(c != '\r') s.append((char)c); 
         c = in.read();
      }
      return s.toString();
   }
   
   private void readInRequest(InputStream in, HashMap<String, String> request) throws IOException {
      String line = request.get("request-line");
      System.out.println(line);
      while(!line.isEmpty()) {
         line = this.readLine(in);
         System.out.println(line);
         if(line.contains(":")){
            String key = line.split(":")[0].trim().toLowerCase();
            request.put(key, line.substring(line.indexOf(':')).trim());
         } 
      }
      if(request.containsKey("content-length")) {
         StringBuilder s = new StringBuilder();
         while(s.toString().length() < Integer.parseInt(request.get("content-length"))) {
            s.append((char)in.read());
         }
         System.out.println(s.toString());
         request.put("body", s.toString());
      }
   }
   
   private void writeResponse(PrintWriter out, HashMap<String, String> response) {
      out.println(response.get("status-line"));
      System.out.println(response.get("status-line"));
      Iterator i = response.keySet().iterator();
      while(i.hasNext()){
         String key = (String)i.next();
         if(!key.equals("status-line") && !key.equals("body")){
            out.println(key+": "+response.get(key));
            System.out.println(key+": "+response.get(key));
         }
      }
      out.println("\n");
      out.println(response.get("body"));
      out.flush();
   }

   private server.util.AbstractResponder getResponder(HashMap<String, String> request){
      String referer = removeHostName(request.get("referer"));
      String target = request.get("request-line").split(" ")[1];
      String endPoint;
      if(referer == null || referer.equals("")) {
         if(target.split("/").length > 0) endPoint = "/"+target.split("/")[1];
         else endPoint = target;
      } else endPoint = referer;
      msgOut.println("endPoint: "+endPoint);
      if(responders.containsKey(endPoint)) {
         if(target.contains(endPoint)) {
            return responders.get(endPoint);
         } else return responders.get(endPoint).getRedirect(endPoint+target);
      } else return responders.get("/");
   }

   private String removeHostName(String fqdn) {
      if(fqdn == null) return "";
      String temp = fqdn.substring(fqdn.indexOf("//")+2);
      temp = temp.substring(temp.indexOf('/'));
      return temp;
   }

   public boolean hasMessages(){
      return !messages.toString().isEmpty();
   }

   public String getMessages(){
      String temp = messages.toString();
      messages.getBuffer().setLength(0);
      messages.getBuffer().trimToSize();
      return temp;
   }
}

