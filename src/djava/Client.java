// A Java program for a Client
package djava;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	// initialize socket and input output streams
	private Socket socket = null;
	private Scanner input	 = null;
	private PrintWriter out = null;
	private Scanner response = null;

   private static String mode;

	// constructor to put ip address and port
	public Client(String address, int port)
	{
		// establish a connection
		try {
			socket = new Socket(address, port);
			System.out.println("Connected");

			// takes input from terminal
			input = new Scanner(System.in);

			// sends output to the socket
			out = new PrintWriter(socket.getOutputStream());

         // Receives input from the socket
         response = new Scanner(socket.getInputStream());
		}
		catch (UnknownHostException u) {
			System.out.println(u);
			return;
		}
		catch (IOException i) {
			System.out.println(i);
			return;
		}

      if(mode == "echoMode") {
         out.println("echoMode");

         // string to read message from input
         String line = "";

         // keep reading until "Over" is input
         while (!line.equals("Over")) {
            line = input.nextLine();
            out.println(line);
            out.flush();
            System.out.println(response.nextLine());
         }
      } else {
         // string to read message from input
         String line = "GET / HTTP/1.1";
         out.println(line);
         out.flush();
         while (!line.equals("")) {
            line = input.nextLine();
            out.println(line);
            out.flush();
         }

         while (response.hasNextLine()) {
            System.out.println(response.nextLine());
         }
      }

		// close the connection
      input.close();
      out.close();
      response.close();
      try{
         socket.close();
      }
      catch (IOException i) {
         System.out.println(i);
      }
	}

	public static void main(String args[])
	{
      Client client;
      mode="echoMode";
		if(args.length == 0) {
         System.out.println("Usage: [OPTIONS] [PORTNUMBER]");
         System.out.println("   --http-test  Tries to mimic an HTTP request client (Optional)");
         System.out.println();
      } else if(args[0].equals("--http-test")){
         mode="httpMode";
         client = new Client("127.0.0.1", Integer.parseInt(args[1]));
      } else client = new Client("127.0.0.1", Integer.parseInt(args[0]));
	}
}

