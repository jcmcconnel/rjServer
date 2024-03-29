package server;

import java.io.*;
import java.util.Scanner;

public class main {
   private static Server server;
   private static int exit_status;

   public static void main(String args[])
   {
      boolean interactive = false;
      Integer portNum = null;
      File conf = null;
      exit_status = -1;
      
      // Parse args
      for(int i=0; i<args.length; i++){
         if(args[i].equals("-h")) {
            printHelp("");
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
               printHelp("");
               System.exit(1);
            }

         } else {
            try{
               portNum = Integer.valueOf(args[i]);
            } 
            catch(NumberFormatException e){
               System.out.println("Option not recognized");
               System.out.println(e);
               printHelp("");
               return;
            }
         }
      }

      // Create new server
      server = new Server();
      if(portNum != null) server.changeState("port", portNum);

      // Read in conf file if provided
      if(conf != null){
         loadConf(conf);
      }

      // Enter interactive mode if requested.
      if(interactive){
         Scanner input = new Scanner(System.in);
         String line;
         while(exit_status < 0){
            System.out.print(">>> ");
            line = input.nextLine();
            processCmd(line);
            if(server.hasMessages()) System.out.println(server.getMessages());
         }
      }
   }

   private static void printHelp(String cmd){
      switch(cmd){
            case "set":
               System.out.println("Update system/control variables");
               break;
            case "add-page":
               break;
            case "add-app":
               break;
            case "help":
               System.out.println("Using help");
               break;
            case "getMessages":
               break;
            case "start":
               break;
            case "stop":
               break;
            case "detach":
               break;
            case "exit":
               break;
         default:
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
            break;
      }
   }

   /*
    * Loads the configuration from a file.
    *
    **/
   private static void loadConf(File conf){

   }

   /*
    * Command processor for interactive mode
    *
    **/
   private static void processCmd(String cmdLine){
      String pageRoot = "./responder/pages";
      File pageRootFile = new File(pageRoot);

      String cmd = cmdLine.split(" ")[0];
           
      try{
         switch(cmd){
            case "set":
               if(cmdLine.split(" ").length == 3){
                  switch(cmdLine.split(" ")[1]){
                     case "port":
                        server.changeState("port", Integer.valueOf(cmdLine.split(" ")[2]));
                        break;
                  }
               }
               break;
            case "add-page":
               if(cmdLine.split(" ").length > 1){
                  File f = new File(pageRoot+"/"+cmdLine.split(" ")[1]);
                  if(f.isDirectory()) server.addResponder("/"+f.getName(), new responder.StaticResponder("/"+f.getName(), pageRoot));
                  else System.out.println("Page Does not Exist");
               }
               break;
            case "add-app":
               if(cmdLine.split(" ").length > 1){
                  File f = new File(pageRoot+cmdLine.split(" ")[1]+"/index.djava");
                  if(f.exists()) server.addResponder("/", new responder.ApplicationResponder("/", pageRoot));
                  else System.out.println("App Does not Exist");
               }
               break;
            case "load-lib":
               File libFile = new File(".");
               URL url = libFile.toURI().toURL();
               URL[] urls = new URL[]{url};
               Classloader cl = new URLClassLoader(urls);
               server.util.AbstractResponder sr = (server.util.AbstractResponder)cl.loadClass("responder.StaticResponder").getConstructor(String).instanceOf("/"+f.getName(), pageRoot);
               server.addResponder(sr);
               break;
            case "help":
               if(cmdLine.split(" ").length > 1) printHelp(cmdLine.split(" ")[1]);
               else printHelp("");
               break;
            case "getMessages":
               System.out.println(server.getMessages());
               break;
            case "start":
               if(!server.isRunning()) server.start();
               break;
            case "stop":
               server.stopServer();
               break;
            case "detach":
               exit_status = 0;
               break;
            case "exit":
               server.stopServer();
               exit_status = 0;
               break;
         }
      }
      catch(IOException e){
         System.out.println("Stopped");
         System.out.println(e);
      }
   }

}
