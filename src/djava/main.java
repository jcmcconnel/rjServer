package djava;

public class main {
	public static void main(String args[])
	{
      if(args.length == 0) {
         System.out.println("Usage: java DjavaServer.main [PORTNUMBER]");
         System.out.println("   This will start the DjavaServer listening on localhost:[PORTNUMBER]");
      } else {
         DjavaServer server = new DjavaServer();
         server.start(Integer.parseInt(args[0]));
      }
	}
}
