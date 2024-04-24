package server;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;

import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.*;
import java.util.Set;


/**
 * Notes: Move toward ServerSocketChannel/SocketChannel non-blocking implementation.
 *   - Non-blocking input would allow for multiple connections, Asynchronous only means that the socket operation is disconnected from the parent process.
 *     This will require a list of open client-connections along with their partial request contents
 **/

public class Server implements Runnable
{
   private class Client {
      public InetSocketAddress remote;
      public long startTime;
      public ByteBuffer buffer;
      public StringBuilder rawRequest;
      public HashMap<String, String> request;
      public HashMap<String, String> response;
      public boolean readyToWrite = false;

      public ServerSocketChannel server;
      public SocketChannel client;

      public boolean isDead = false;

      public Client(InetSocketAddress r, long t, ServerSocketChannel s, SocketChannel c){
         remote = r;
         startTime = t;
         rawRequest = new StringBuilder();
         request = new HashMap<String, String>();
         server = s;
         client = c;
      }

      /**
       * Closes things associated with this client.
       **/
      public void close() throws IOException {
         client.close();
         //server.close();
         isDead = true;
      }

      /**
       * Returns true, if the end of input is reached.
       **/
      public boolean processBuffer(){
         msgOut.println("Process buffer");
         buffer.rewind();
         for(int i=0; i<buffer.array().length; i++){
            byte b = buffer.get();
            //msgOut.print((char)b);
            rawRequest.append((char)b);
            if(b == '\0') {
               msgOut.println("EOF found: "+(char)b+i);
               msgOut.println(rawRequest.toString());
               rawRequest.trimToSize();
               readInRequest();
               readyToWrite = true;
               return true;
            }
         }
         return false;
      }

      private void readInRequest() {
         Scanner scanner = new Scanner(rawRequest.toString());
         while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.equals("")) break;
            if(line.contains(":")){
               String key = line.split(":")[0].trim().toLowerCase();
               this.request.put(key, line.substring(line.indexOf(':')+1).trim());
            } else request.put("request-line", line);
         }
         if(this.request.containsKey("content-length")) {
            StringBuilder s = new StringBuilder();
            while(s.toString().length() < Integer.parseInt(this.request.get("content-length"))) {
               String line = scanner.nextLine();
               s.append(line);
            }
            this.request.put("body", s.toString());
         }
      }

      private String getRawResponse() {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         PrintWriter out = new PrintWriter(output);
         out.println(response.get("status-line"));
         //msgOut.println(response.get("status-line"));
         Iterator i = response.keySet().iterator();
         while(i.hasNext()){
            String key = (String)i.next();
            if(!key.equals("status-line") && !key.equals("body")&&!key.equals("Content-Length")){
               out.println(key+": "+response.get(key));
               //msgOut.println(key+": "+response.get(key));
            }
         }
         out.println("Content-Length: "+response.get("Content-Length"));
         out.print("\n");
         out.print(response.get("body"));
         out.flush();
         return output.toString();
      }
   }

   private Socket socket;

   private InputStream in;
   private PrintWriter out;

   private ServerSocketChannel serverChannel;

   private HashMap<String, Object> serverState;

   private StringWriter messages;
   private PrintWriter msgOut;

   private ArrayList<Client> activeConnections;

   
   public Server()
   {
      socket = null;
      in = null;
      out = null;

      serverChannel = null;

      messages = new StringWriter();
      msgOut = new PrintWriter(messages);

      serverState = new HashMap<String, Object>();

      activeConnections = new ArrayList<Client>();

   }

   /**
    * Runnable start point
    **/
   public void run()
   {
      Selector selector;
      try {
         // starts server and waits for a connection
         if(serverState.containsKey("port") && serverState.get("port") != null) {
            //--- Selector ---//
            //https://stackoverflow.com/questions/58635444/proper-way-to-read-write-through-a-socketchannel
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(InetAddress.getByName("localhost"), ((Integer)serverState.get("port")).intValue()));

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            serverState.put("state", "running");
            msgOut.println("Server started");


            while(serverState.get("state").equals("running")){
               //--- Selector ---//
               selector.select();

               Set<SelectionKey> selectedKeys = selector.selectedKeys();
               
               Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
               while(keyIterator.hasNext()){
                  SelectionKey key = keyIterator.next();
                  if(key.isAcceptable()) {
                     /* This is where we accept connections. */
                     //ServerSocketChannel server = (ServerSocketChannel) key.channel();
                     SocketChannel clientChannel = serverChannel.accept();
                     if(clientChannel == null) {
                        continue;
                     }

                     msgOut.println("client accepted");

                     clientChannel.configureBlocking(false);
                     clientChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);

                     activeConnections.add(new Client((InetSocketAddress)clientChannel.getRemoteAddress(), System.currentTimeMillis(), serverChannel, clientChannel));
                     keyIterator.remove();
                     continue;
                  }
                  SocketChannel client = (SocketChannel) key.channel();
                  Client connectedClient = null;
                  Iterator i = activeConnections.iterator();
                  while(i.hasNext()){
                     Client c = (Client)i.next();
                     if(c.isDead) i.remove();
                     if(c.remote.equals(client.getRemoteAddress())){
                        connectedClient = c;
                        break;
                     }
                  }
                  if(key.isReadable() && connectedClient != null && !connectedClient.readyToWrite){
                     /* This is where we read from previously accepted connections. */
                     ByteBuffer buf = ByteBuffer.allocate(256);
                     msgOut.println("connectedClient"+connectedClient.remote.toString());
                     connectedClient.buffer = ByteBuffer.allocate(256);
                     connectedClient.client.read(connectedClient.buffer);

                     if(connectedClient.processBuffer()){
                        if(connectedClient.rawRequest.toString().trim().isEmpty()){
                           //Return ERROR
                           keyIterator.remove();
                           connectedClient.close();
                           continue;
                        }

                        try{
                           connectedClient.response = server.util.AbstractResponder.getErrorResponse(connectedClient.request);
                           connectedClient.response = server.util.AbstractResponder.getResponder(connectedClient.request).getResponse(connectedClient.request);
                        }catch(ReflectiveOperationException | FileNotFoundException e){
                           msgOut.println(e);
                           connectedClient.response = server.util.AbstractResponder.getErrorResponse(connectedClient.request);
                        }

                        key.attach(connectedClient);

                     } else key.attach(connectedClient);
                  }
                  if(key.isWritable() && connectedClient != null && connectedClient.readyToWrite){
                     if(connectedClient == null || !connectedClient.readyToWrite){
                        if(connectedClient == null) msgOut.println("null client");
                        if(!connectedClient.readyToWrite) msgOut.println("Not ready to write");
                        keyIterator.remove();
                        continue;
                     }

                     String output = connectedClient.getRawResponse();
                     msgOut.println("Ready to write: "+connectedClient.remote.toString());
                     client.write(ByteBuffer.allocate(output.length()).wrap(output.getBytes()));
                     connectedClient.close();
                     keyIterator.remove();
                     continue;
                  }
               }
            }
            msgOut.println("Closing channel");
            selector.close();
            Iterator<Client> iter = activeConnections.iterator();
            while(iter.hasNext()) {
               iter.next().close();
               iter.remove();
            }
            serverChannel.close();
         }
      }
      catch(SocketException e){
         msgOut.println("Socket Error");
         msgOut.println(e);
      }
      catch(IOException i)
      {
         msgOut.println("Bottom of run"+i.toString());
      }
   }

   /**
    * Starts the server in a new thread.
    **/
   public void start(){
       serverState.put("current-thread", new Thread(this));
       ((Thread)serverState.get("current-thread")).start();
   }

   public void stopServer() throws IOException {
      serverState.put("state", "stopped");
      msgOut.println("Stopping Server");
      if(((Thread)serverState.get("current-thread")).isAlive()) ((Thread)serverState.get("current-thread")).interrupt();
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
      //msgOut.println(line);
      while(!line.isEmpty()) {
         line = this.readLine(in);
         //msgOut.println(line);
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
         //msgOut.println(s.toString());
         request.put("body", s.toString());
      }
   }
   
   private void writeResponse(PrintWriter out, HashMap<String, String> response) {
      out.println(response.get("status-line"));
      msgOut.println(response.get("status-line"));
      Iterator i = response.keySet().iterator();
      while(i.hasNext()){
         String key = (String)i.next();
         if(!key.equals("status-line") && !key.equals("body")){
            out.println(key+": "+response.get(key));
            msgOut.println(key+": "+response.get(key));
         }
      }
      out.println("\n");
      out.println(response.get("body"));
      out.flush();
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

