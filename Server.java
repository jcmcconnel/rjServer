// A Java program for a Server
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server
{
	//initialize socket and input stream
	private Socket		 socket = null;
	private ServerSocket server = null;
	private Scanner in	 = null;
	private PrintWriter out = null;

	// constructor with port
	public Server(int port)
	{
		// starts server and waits for a connection
		try
		{
			server = new ServerSocket(port);
			System.out.println("Server started");

			System.out.println("Waiting for a client ...");

			socket = server.accept();
			System.out.println("Client accepted");

			// takes input from the client socket
			in = new Scanner(socket.getInputStream());

         out = new PrintWriter(socket.getOutputStream());

			String line = "";

			// reads message from client until "Over" is sent
			while (!line.equals("Over"))
			{
				//try
				//{
					if(in.hasNextLine()) {
                  line = in.nextLine();
                  System.out.println(line);
                  if(line.trim().equals("GET / HTTP/1.1")) {
                     out.println("HTTP/1.1 200 OK");
                     out.println("Cache-Control: no-cache");
                     out.println("Server: djava");
                     out.println("Date: Fri Mar 15 2024");
                     out.println("Connection: Keep-Alive:");
                     out.println("Content-Type: text/html");
                     out.println("");
                     
                     out.println("<html><head></head><body><p>Test</p></body></html>");
                     out.flush();
                     // close connection
                     //out.close();
                     //socket.close();
                     //in.close();
                     //return;
                  } else {
                     out.println("This is what I received"+line);
                     out.flush();
                  }
                  //System.out.println("Sent Response");
               }

				//}
				//catch(IOException i)
				//{
				//	System.out.println(i);
            //   in.close();
            //   return;
				//}
			}
			System.out.println("Closing connection");

			// close connection
			socket.close();
			in.close();
		}
		catch(IOException i)
		{
			System.out.println(i);
		}
	}

	public static void main(String args[])
	{
		Server server = new Server(5000);
	}
}

