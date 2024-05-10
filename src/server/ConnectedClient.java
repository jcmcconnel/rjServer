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

/* Package private */
class ConnectedClient {
   public InetSocketAddress remote;
   public long startTime;
   public ByteBuffer buffer;
   public StringBuilder rawRequest;
   public HashMap<String, String> request;
   public HashMap<String, Object> response;
   public boolean readyToWrite = false;

   public ServerSocketChannel server;
   public SocketChannel client;

   public boolean isDead = false;

   public ConnectedClient(InetSocketAddress r, long t, ServerSocketChannel s, SocketChannel c){
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
      isDead = true;
   }

   /**
      * Returns true, if the end of input is reached.
      **/
   public boolean processBuffer(){
      buffer.rewind();
      for(int i=0; i<buffer.array().length; i++){
         byte b = buffer.get();
         rawRequest.append((char)b);
         if(b == '\0') {
            rawRequest.trimToSize();
            readInRequest();
            readyToWrite = true;
            return true;
         }
      }
      return false;
   }

   protected void readInRequest() {
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

   public void write() throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(output);
      out.println(response.get("status-line"));
      Iterator i = response.keySet().iterator();
      while(i.hasNext()){
         String key = (String)i.next();
         if(!key.equals("status-line") && !key.equals("body")&&!key.equals("Content-Length")){
            out.println(key+": "+response.get(key));
         }
      }
      out.println("Content-Length: "+response.get("Content-Length"));
      out.print("\n");
      System.out.println("Content-Type: "+response.get("Content-Type"));
      System.out.println(response.get("body").getClass().toString());
      if(response.get("Content-Type").equals("image/jpg;")){
         System.out.println("Writing image data");
         out.flush();
         client.write(ByteBuffer.allocate(output.size()).wrap(output.toByteArray()));
         client.write(ByteBuffer.allocate(((byte[])response.get("body")).length).wrap((byte[])response.get("body")));
      } else {
         out.print(new String((byte[])response.get("body")));
         out.flush();
         client.write(ByteBuffer.allocate(output.size()).wrap(output.toByteArray()));
      }
   }

}

