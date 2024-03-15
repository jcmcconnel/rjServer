// A Java program for a Server
package djava;
import java.net.*;
import java.io.*;
import java.util.Scanner;

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
            while(in.hasNextLine()) {
               line = in.nextLine();
               if(line.trim().equals("GET / HTTP/1.1") || line.trim().equals("GET / HTTP/1.0")) {
                  out.println("HTTP/1.1 200 OK");
                  out.println("Cache-Control: no-cache");
                  out.println("Server: djava");
                  out.println("Date: Fri Mar 15 2024");
                  out.println("Connection: Keep-Alive:");
                  out.println("Content-Type: text/html");
                  out.println("");
                  
                  out.println("<html><head></head><body><p>Test</p></body></html>");
                  out.flush();
               }
            }
         }
      }
      System.out.println("Closing connection");

      // close connection
      socket.close();
      in.close();
   }
}

