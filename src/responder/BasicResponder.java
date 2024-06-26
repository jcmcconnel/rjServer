package responder;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class BasicResponder extends server.util.AbstractResponder
{

   public BasicResponder(File root, String ep, String[] exts)
   {
      super(root, ep, exts);
   }

   /**
    *
    * TODO: Check for things like php and allow for system calls.
    **/
   protected String getBody(String target) throws server.util.ResponderException
   {
      String body = "";
      File resource = new File(this.root+"/index."+this.fileExtensions[1]);
      if(this.path == null || this.path.equals("") || this.path.equals("/")){
         for(int i=0; i<this.fileExtensions.length; i++){
            resource = new File(this.root+"/index."+this.fileExtensions[i]);
            if(resource.exists()) break;
         }
      } else {
         int pathExtIndex = this.path.lastIndexOf(".");
         String pathExt = "";
         if(pathExtIndex > 0) pathExt = this.path.substring(pathExtIndex);
         System.out.println("Ext: "+pathExt);
         resource = new File(this.root+this.path);
         for(int i=0; i<this.fileExtensions.length; i++){
            if(pathExtIndex > 0){
               if(pathExt.equals(this.fileExtensions[i])) break;
            } else {
               resource = new File(this.root+this.path+"/index."+this.fileExtensions[i]);
               if(resource.exists()) break;
            }
         }
      }
      System.out.println("Static Responder EP:"+this.getEndPoint());
      System.out.println("Static Responder root:"+this.root);
      System.out.println("Static Responder path:"+this.path);
      System.out.println("Static Responder resource:"+resource.toString());

      try {
         if(resource.exists()) body = Files.readString(resource.toPath());
         else throw new server.util.ResponderException("Path: "+this.path+" does not exist");
         if(resource.getName().endsWith(".php")){
            //Php is the problem, this code wasn't.  It's breaking because it doesn't like relative paths.
            String cmd = String.format("php -f "+resource.getPath(), "DOCUMENT_ROOT="+this.root, this.root);
            System.out.println(cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            StringBuilder sb = new StringBuilder();
            try{
               process.waitFor();
               BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
               BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
               String line = "";
               String errLine = "";
               while((line = input.readLine()) != null || (errLine = error.readLine()) != null ) {
                  System.out.println(line);
                  System.out.println("err: "+errLine);
                  //sb.append(line);
               }
               //body = sb.toString();        
            }
            catch(InterruptedException e){}
            System.out.println(sb.toString());
         }
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }
   
}

