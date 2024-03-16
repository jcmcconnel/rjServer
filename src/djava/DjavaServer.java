// A Java program for a Server
package djava;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;

public class DjavaServer
{
	//initialize socket and input stream
	private Socket		 socket = null;
	private ServerSocket server = null;
	private Scanner in	 = null;
	private PrintWriter out = null;

	// constructor with port
	public DjavaServer()
	{
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
      String content;
      Boolean endOfInput = false;
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
         } else if(line.startsWith("GET")) {
            System.out.println("Accepted client in http mode");
            request.put("request", line);
            System.out.println(line+request);
            while(!line.isEmpty()) {
               line = in.nextLine();
               System.out.println(line);
               if(line.contains(":")){
                  request.put(line.split(":")[0].trim(), line.split(":")[1].trim());
                  System.out.println("Stored: "+request.get(line.split(":")[0].trim()));
               }
            }
            Iterator i = request.keySet().iterator();
            while(i.hasNext()){
               String key = (String)i.next();
               if(key.equals("request")){
                  out.println("HTTP/1.1 200 OK");
               } else out.println(key+": "+request.get(key));
            }
            
            out.println("<html><head></head><body><p>Test</p></body></html>");
            out.flush();
         }
      }
      System.out.println("Closing connection");

      // close connection
      socket.close();
      in.close();
   }
}

