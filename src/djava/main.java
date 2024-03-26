package djava;

public class main {
   public static void main(String args[])
   {
      if(args.length == 0) {
         System.out.println("Usage: java Server.main [PAGE ROOT] [PORTNUMBER]");
         System.out.println("   This will start the Server listening on localhost:[PORTNUMBER]");
      } else {
         Server server = new Server(args[0]);
         server.start(Integer.parseInt(args[1]));
      }
   }
}
