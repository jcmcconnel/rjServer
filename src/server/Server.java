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
   private Socket socket;
   private ServerSocket serverSocket;

   private InputStream in;
   private PrintWriter out;

   private ServerSocketChannel serverChannel;

   private HashMap<String, Object> serverState;

   private StringWriter messages;
   private PrintWriter msgOut;

   
   public Server()
   {
      socket = null;
      serverSocket = null;
      in = null;
      out = null;

      serverChannel = null;

      messages = new StringWriter();
      msgOut = new PrintWriter(messages);

      serverState = new HashMap<String, Object>();

   }

   /**
    * Runnable start point
    **/
   public void run()
   {
      try {
         Selector selector;
         // starts server and waits for a connection
         if(serverState.containsKey("port") && serverState.get("port") != null) {
            //serverSocket = new ServerSocket(((Integer)serverState.get("port")).intValue());

            //https://stackoverflow.com/questions/58635444/proper-way-to-read-write-through-a-socketchannel
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(InetAddress.getByName("localhost"), ((Integer)serverState.get("port")).intValue()));

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            serverState.put("state", "running");
            msgOut.println("Server started");


            while(serverState.get("state").equals("running")){
               selector.select();

               Set<SelectionKey> selectedKeys = selector.selectedKeys();
               
               Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
               while(keyIterator.hasNext()){
                  SelectionKey key = keyIterator.next();
                  if(key.isAcceptable()) {
                     /* This is where we accept connections. */
                     ServerSocketChannel server = (ServerSocketChannel) key.channel();
                     SocketChannel clientChannel = server.accept();
                     if(clientChannel == null) {
                        continue;
                     }

                     System.out.println("client accepted");

                     clientChannel.configureBlocking(false);
                     clientChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
                  } else if(key.isReadable()){
                     /* This is where we read from previously accepted connections. */
                     ByteBuffer buf = ByteBuffer.allocate(256);
                     SocketChannel client = (SocketChannel) key.channel();
                     int bytesRead = client.read(buf);
                     if(key.attachment() == null) key.attach(new String(buf.array()));
                     else key.attach(key.attachment().toString()+new String(buf.array()));
                     //System.out.println("readable: "+key.attachment().toString()+"$$$"+bytesRead);

                     if(bytesRead == -1 || bytesRead < 256 || key.attachment().toString().endsWith("\r\n\r\n") || key.attachment().toString().endsWith("\n\n")){
                        System.out.println("Ending read");
                        System.out.println(key.attachment().toString());
                        //client.close();
                        key.cancel();
                        System.out.println(selectedKeys.toString());
                     }

                  } else if(key.isWritable() && !key.isReadable()){
                     System.out.println("Something is writable");
                     if(key.attachment() != null) System.out.println(key.attachment().toString());
                     SocketChannel client = (SocketChannel) key.channel();
                     ByteBuffer b = ByteBuffer.allocate(50);
                     b.putChar('H');
                     b.putChar('T');
                     b.putChar('T');
                     b.putChar('P');
                     b.putChar('/');
                     b.putChar('1');
                     b.putChar('.');
                     b.putChar('0');
                     b.putChar(' ');
                     b.putChar('4');
                     b.putChar('0');
                     b.putChar('4');
                     b.putChar('\n');
                     
                     client.write(b);
                     client.close();
                     key.cancel();
                  }
               }
               //clientSession();
            }
         } else return;
      }
      catch(SocketException e){
         msgOut.println("Socket closed");
      }
      catch(IOException i)
      {
         msgOut.println(i);
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
            try{
               response = server.util.AbstractResponder.getResponder(request).getResponse(request);
            }catch(ReflectiveOperationException | FileNotFoundException e){
               System.out.println(e);
               response = server.util.AbstractResponder.getErrorResponse(request);
            }
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
      //System.out.println(line);
      while(!line.isEmpty()) {
         line = this.readLine(in);
         //System.out.println(line);
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
         //System.out.println(s.toString());
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

