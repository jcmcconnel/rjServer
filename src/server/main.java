/**
 * 
 *
 * @Author James McConnel
 **/
package server;

import java.io.*;
import java.lang.ClassLoader;
import java.lang.ReflectiveOperationException;
import java.lang.reflect.Constructor;
import java.util.Scanner;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * Serves as a command line control program for the server itself.
 * Commands are available to start, stop, detach, add Responders/Applications/Modules or whatever you want to call them.
 *
 **/
public class main {
   private static Server server;
   private static int exit_status;

   private static URLClassLoader cl;

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
               System.out.println(conf.toURI());
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
         try{
            loadConf(conf);
         }
         catch(FileNotFoundException e){
            System.out.println(e);
         }
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
            case "add":
               System.out.println("Usage: add [CLASSNAME] [ROOT] [ENDPOINT]");
               System.out.println("   Adds a responder.  ");
               System.out.println("   CLASSNAME must be a class accessible through one of the libraries loaded with: load-lib");
               System.out.println("   ENDPOINT must contain the preceding slash.  I.e.: '/test'");
               break;
            case "load-lib":
               System.out.println("Usage: load-lib [CLASSPATH]");
               System.out.println("   Specify the classpath to search.");
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
   private static void loadConf(File conf) throws FileNotFoundException{
      Scanner in = new Scanner(conf);
      while(in.hasNextLine()){
         String line = in.nextLine();
         System.out.println(line);
         processCmd(line);
      }
   }

   /*
    * Command processor for interactive mode
    *
    **/
   private static void processCmd(String cmdLine){
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
            case "add":
               //add classname filesystem-root endpoint
               if(cmdLine.split(" ").length == 4){
                  try{
                     File root = new File(cmdLine.split(" ")[2]);
                     System.out.println();
                     if(root.isDirectory()) {
                        System.out.println(cmdLine.split(" ")[1]);
                        Class rclass = cl.loadClass(cmdLine.split(" ")[1]);
                        System.out.println(rclass.toString());
                        Constructor rConstructor = rclass.getConstructor(String.class, String.class);
                        server.util.AbstractResponder r = (server.util.AbstractResponder)rConstructor.newInstance(cmdLine.split(" ")[2], "/"+cmdLine.split(" ")[3]);
                        System.out.println(r.toString());
                        server.addResponder("/"+cmdLine.split(" ")[3], r);
                     } else System.out.println("Application root is not a directory.");
                  }catch(ReflectiveOperationException e){
                     System.out.println(e);
                     printHelp("add");
                  }
               } else printHelp("add");
               break;
            case "load-lib":
               if(cmdLine.split(" ").length == 2){
                  String wd = cmdLine.split(" ")[1];
                  if(wd.trim().equals(".")) wd = System.getProperty("user.dir");
                  File libFile = new File(wd);
                  System.out.println(libFile.getAbsoluteFile().toURI().toURL());
                  if(libFile.getAbsoluteFile().isDirectory()){
                     URL url = libFile.getAbsoluteFile().toURI().toURL();
                     URL[] urls = new URL[]{url};
                     cl = new URLClassLoader(urls);
                  } else printHelp("load-lib");
               } else printHelp("load-lib");
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
