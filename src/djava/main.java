package djava;

public class main {
   public static void main(String args[])
   {
      if(args.length == 0) {
         System.out.println("Usage: java DjavaServer.main [PAGE ROOT] [PORTNUMBER]");
         System.out.println("   This will start the DjavaServer listening on localhost:[PORTNUMBER]");
         System.out.println("   PageResponders root at: [PAGE ROOT]");
      } else {
         DjavaServer server = new DjavaServer(args[0]);
         server.start(Integer.parseInt(args[1]));
      }
   }
}
