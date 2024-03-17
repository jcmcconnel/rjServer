package djava;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;

public class DjavaServer
{
   private Socket socket;
   private ServerSocket server;
   private Scanner in;
   private PrintWriter out;

   private ArrayList<djava.Responder> responders;
   private djava.Responder errorResponder;
   
   public DjavaServer()
   {
      socket = null;
      server = null;
      in = null;
      out = null;

      errorResponder = new djava.Responder("/") {
         protected String getBody(String target){
            return djava.Responder.getDefaultErrorBody();
         }
      };
      responders = new ArrayList<djava.Responder>();
      responders.add(new djava.Responder("/") {
         protected String getBody(String target){
            return "<!DOCTYPE html>\n"+
                    "<html>\n"+
                    "  <head>\n"+
                    "  </head>\n"+
                    "  <body>\n"+
                    "    <p>"+"Index"+"</p>\n"+
                    "  </body>\n"+
                    "</html>\n";
         }
      });
      responders.add(new djava.Responder("/test") {
         protected String getBody(String target){
            return "<!DOCTYPE html>\n"+
                    "<html>\n"+
                    "  <head>\n"+
                    "  </head>\n"+
                    "  <body>\n"+
                    "    <p>"+"Test"+"</p>\n"+
                    "  </body>\n"+
                    "</html>\n";
         }
      });
      responders.add(new djava.Responder("/test1") {
         protected String getBody(String target){
            return "<!DOCTYPE html>\n"+
                    "<html>\n"+
                    "  <head>\n"+
                    "  </head>\n"+
                    "  <body>\n"+
                    "    <p>"+"Test1"+"</p>\n"+
                    "  </body>\n"+
                    "</html>\n";
         }
      });
      responders.add(new djava.Responder("/chickenWings") {
         protected String getBody(String target){
         return "<!DOCTYPE html>\n"+
                 "<html>\n"+
                 "  <head>\n"+
                 "  </head>\n"+
                 "  <body>\n"+
                 "    <p id=\"wings\" style=\"font-size: 50px; color: red;\">"+"chicken wings"+"</p>\n"+
                 " <script>function flashtext(ele,col) { \n"+
                 " var tmpColCheck = document.getElementById( ele ).style.color; \n"+
                 " if (tmpColCheck === 'red') { \n"+
                 " document.getElementById( ele ).style.color = col; \n"+
                 " } else {"+
                 " document.getElementById( ele ).style.color = 'red'; \n"+
                 " }\n"+
                 " }\n"+

                 "setInterval(function() {\n"+
                 "flashtext('wings','green');\n"+
                 "}, 500 );\n"+
                 "</script>\n"+
                 "  </body>\n"+
                 "</html>\n";
         }
      });
   }
   
   public void start(int port)
   {
      try {
         // starts server and waits for a connection
         server = new ServerSocket(port);
         System.out.println("Server started");
         while(true) clientSession();
      }
      catch(IOException i)
      {
         System.out.println(i);
      }
   }

   private void clientSession() throws IOException
   {
      HashMap<String, String> request = new HashMap<String, String>();
      HashMap<String, String> response = new HashMap<String, String>();
   
      System.out.println("Waiting for a client ...");
   
      socket = server.accept();
      System.out.println("Client accepted");
   
      // takes input from the client socket
      in = new Scanner(socket.getInputStream());
   
      out = new PrintWriter(socket.getOutputStream());
   
      String line = "";
   
      if(in.hasNextLine()) {
         line = in.nextLine();
         if(line.equals("echoMode")){
            // reads message from client until "Over" is sent
            while (!line.equals("Over"))
            {
               if(in.hasNextLine()) {
                  line = in.nextLine();
                  System.out.println(line);
                  out.println("This is what I received: "+line);
                  out.flush();
               }
   
            }
         } else {
            System.out.println("Accepted client in http mode");
            request.put("request-line", line);
            readInRequest(in, request);
            response = getResponder(request.get("request-line").split(" ")[1]).getResponse(request);
            writeResponse(out, response);
         }
      }
      System.out.println("Closing connection");
   
      // close connection
      socket.close();
      in.close();
   }
   
   private void readInRequest(Scanner in, HashMap<String, String> request){
      Boolean endOfInput = false;
      String line = request.get("request-line");
      System.out.println(line);
      while(!line.isEmpty()) {
         line = in.nextLine();
         System.out.println(line);
         if(line.contains(":")){
            request.put(line.split(":")[0].trim().toLowerCase(), line.split(":")[1].trim());
         }
      }
      if(request.containsKey("content-length")) {
         request.put("body", "");
         while(request.get("body").length() < Integer.parseInt(request.get("content-length"))){
            request.put("body", request.get("body")+in.nextLine());
         }
      }
   }
   
   private void writeResponse(PrintWriter out, HashMap<String, String> response) {
      out.println(response.get("status-line"));
      Iterator i = response.keySet().iterator();
      while(i.hasNext()){
         String key = (String)i.next();
         if(!key.equals("status-line") && !key.equals("body"))
            out.println(key+": "+response.get(key));
      }
      out.println("\n");
      out.println(response.get("body"));
      out.flush();
   }

   private djava.Responder getResponder(String target){
      String endPoint = target;
      if(target.split("/").length > 0) endPoint = "/"+target.split("/")[1];
      System.out.println("\ngetResponder - endPoint: "+endPoint);
      Iterator i = responders.iterator();
      while(i.hasNext()){
         djava.Responder r = (djava.Responder)i.next();
         if(r.equals(endPoint)) return r;
      }
      return errorResponder;
   }
}

