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

import server.util.AbstractResponder;

/**
 *
 * Serves as a command line control program for the server itself.
 * Commands are available to start, stop, add Responders/Applications/Modules or whatever you want to call them.
 *
 **/
public class main {
   private static Server server;
   private static int exit_status;

   private static Integer portNum = null;
   private static File conf = null;

   public static void main(String args[])
   {
      boolean interactive = false;
      exit_status = -1;
      
      // Parse args
      for(int i=0; i<args.length; i++){
         if(args[i].equals("-h")) {
            printHelp("cmd-line-help");
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
               printHelp("cmd-line-help");
               System.exit(1);
            }

         } else {
            try{
               portNum = Integer.valueOf(args[i]);
            } 
            catch(NumberFormatException e){
               System.out.println("Option not recognized");
               System.out.println(e);
               printHelp("cmd-line-help");
               return;
            }
         }
      }

      processCmd("start");

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
               System.out.println("Usage: set [VARIABLE] [VALUE]");
               System.out.println("  Update system/control variables");
               System.out.println("  ");
               System.out.println("  Variables:");
               System.out.println("     port : The portnumber");
               break;
            case "add":
               System.out.println("Usage: add [CLASSNAME] [ROOT] [ENDPOINT]");
               System.out.println("   Adds a responder.  ");
               System.out.println("  ");
               System.out.println("   CLASSNAME must be a class accessible through one of the libraries loaded with: load-lib");
               System.out.println("   ROOT is the filesystem root for this responder");
               System.out.println("   ENDPOINT is the relative address this responder will responder from.");
               System.out.println("   Ex: add responder.StaticResponder ./pages user/home");
               break;
            case "remove":
               System.out.println("Usage: remove [ENDPOINT]");
               System.out.println("   Removes the responder at [ENDPOINT]");
               break; 
            case "load-lib":
               System.out.println("Usage: load-lib [CLASSPATH]");
               System.out.println("   Specify the classpath to search. I.e.: /classes/responders");
               break;
            case "help":
               System.out.println("Usage: help [TOPIC]");
               System.out.println("   Outputs a help message for the topic.");
               System.out.println("  ");
               System.out.println("   Topics:");
               System.out.println("      add");
               System.out.println("      exit");
               System.out.println("      load-lib");
               System.out.println("      start");
               System.out.println("      stop");
               System.out.println("      getMessages");
               System.out.println("      restart");
               break;
            case "getMessages":
               System.out.println("Outputs the latest messages from the server.");
               break;
            case "start":
               System.out.println("Starts the server.");
               break;
            case "stop":
               System.out.println("Stops the server.");
               break;
            case "restart":
               System.out.println("Stops and then starts the server.");
               break;
            case "exit":
               System.out.println("Stops the server, then exits the program.");
               break;
            case "cmd-line-help":
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
            default:
               System.out.println("Welcome to the Djava Server. ");
               System.out.println("");
               System.out.println("To start a service, you will need to load a Responder,");
               System.out.println("and then add the Responder to the list.");
               System.out.println("You can get help with that by typing: help load-lib");
               System.out.println("and: help add");
               System.out.println("");
               System.out.println("Some commands may not take effect without a restart.");
               System.out.println("");
               System.out.println("For a list of all help topics, type: help help");
               break;
      }
   }

   /*
    * Loads the configuration from a file.
    * @param conf The configuration file to run.  All lines are sent to processCmd()
    * @param ignoreStart Will ignore any start commands in the conf file
    *
    **/
   private static void loadConf(File conf, boolean ignoreStart) throws FileNotFoundException{
      Scanner in = new Scanner(conf);
      while(in.hasNextLine()){
         String line = in.nextLine();
         System.out.println(line);
         if(line.equals("start") && ignoreStart) continue;
         else processCmd(line);
      }
   }

   /*
    * Command processor for interactive mode
    * @param cmdLine The command string the user typed in, or that came from the config file.
    *
    **/
   private static void processCmd(String cmdLine){
      String cmd = cmdLine.split(" ")[0];
      if(cmdLine.length() == 0) return; 
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
               //add classname filesystem-root endpoint csv,file,Extensions
               if(cmdLine.split(" ").length == 5){
                  if(cmdLine.split(" ")[3].startsWith("/")) {
                     AbstractResponder.createResponderTemplate(cmdLine.split(" ")[1], cmdLine.split(" ")[2], cmdLine.split(" ")[3], cmdLine.split(" ")[4].split(","));
                  } else {
                     AbstractResponder.createResponderTemplate(cmdLine.split(" ")[1], cmdLine.split(" ")[2], "/"+cmdLine.split(" ")[3], cmdLine.split(" ")[4].split(","));
                  }
               } else printHelp("add");
               break;
            case "remove":
               if(cmdLine.split(" ").length == 2) {
                  if(cmdLine.split(" ")[1].startsWith("/")) 
                     AbstractResponder.removeResponderTemplate(cmdLine.split(" ")[1]);
                  else AbstractResponder.removeResponderTemplate("/"+cmdLine.split(" ")[1]);
               } else printHelp("remove");
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
                     AbstractResponder.addClassLoader(new URLClassLoader(urls));
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
               if(server != null && server.isRunning()) {
                  break;
               } 
               if(server == null) server = new Server();
               else if(server.readState("state").equals("stopped")){
                  server = null;
                  System.gc();
                  server = new Server();
               }
               if(!server.isRunning()) {
                  if(portNum != null) server.changeState("port", portNum);
                  if(conf != null){
                     try{
                        loadConf(conf, true);
                     }
                     catch(FileNotFoundException e){
                        System.out.println(e);
                     }
                  }
                  server.start();
               }
               break;
            case "stop":
               server.stopServer();
               break;
            case "restart":
               processCmd("stop");
               processCmd("start");
               break;
            case "exit":
               if(server != null) server.stopServer();
               exit_status = 0;
               break;
            default:
               System.out.println("Command Not Recognized: "+cmd);
               break;
         }
      }
      catch(IOException e){
         System.out.println("Stopped");
         System.out.println(e);
      }
   }

}
