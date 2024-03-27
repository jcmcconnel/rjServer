package server;

import java.io.*;

public class main {
   public static void main(String args[])
   {
      boolean interactive = false;
      Integer portNum = Integer.valueOf(5000);
      File conf = null;
      Server server;
      String pageRoot = "./responder/pages";
      File pageRootFile = new File(pageRoot);
      
      for(int i=0; i<args.length; i++){
         if(args[i].equals("-h")) {
            printHelp();
            return;
         } else if(args[i].equals("-i")) interactive = true;
         else if(args[i].equals("--conf")) {
            try{
               conf = new File(args[i+1]);
               if(!conf.exists()) throw new NullPointerException();
               i++;
            }
            catch(NullPointerException | ArrayIndexOutOfBoundsException e){
               System.out.println(e);
               printHelp();
               System.exit(1);
            }

         } else {
            try{
               portNum = Integer.valueOf(args[i]);
            } 
            catch(NumberFormatException e){
               System.out.println("Option not recognized");
               System.out.println(e);
               printHelp();
               return;
            }
         }
      }
      server = new Server();
      //////////////// In progress ////////////////////
      for(File f : pageRootFile.listFiles()){
         if(f.isDirectory()) {
         System.out.println("adding responder: "+f.getName());
            server.addResponder("/"+f.getName(), new responder.StaticResponder("/"+f.getName(), pageRoot));
         } else if(f.getName().endsWith(".djava")) server.addResponder("/", new responder.ApplicationResponder("/", pageRoot));
      }
      //////////////// In progress ////////////////////
      server.start(portNum.intValue());
      if(conf != null){
         loadConf(conf);
      }
      if(interactive){
         while(true){
         }
      }
   }

   private static void printHelp(){
      System.out.println("Usage: java Server.main [OPTION(S)] [PORTNUMBER]");
      System.out.println("   This will start the Server listening on localhost:[PORTNUMBER]");
      System.out.println("   If not provided the PORTNUMBER defaults to 5000");
      System.out.println("");
      System.out.println("   Options:");
      System.out.println("   -h Print this message and exits");
      System.out.println("   -i Start the server in interactive mode");
      System.out.println("   --conf [FILENAME] Optional configuration file");
      System.out.println("");
      System.out.println("   -i and --conf are both optional, but if not provided the ");
      System.out.println("   server will have nothing to serve.");
      System.out.println("   Thus, if you would like to do anything interesting, you will need to specify");
      System.out.println("   one or more responders to use either in the configuration file, ");
      System.out.println("   or on the fly in interactive mode.");
   }

   private static void loadConf(File conf){}
}
