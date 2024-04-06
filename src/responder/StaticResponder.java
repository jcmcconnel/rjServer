package responder;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;

public class StaticResponder extends server.util.AbstractResponder
{

   public StaticResponder(File root, String ep)
   {
      super(root, ep);
   }

   /**
    *
    * TODO: Check for things like php and allow for system calls.
    **/
   protected String getBody(String target) throws server.util.ResponderException
   {
      String body = "";
      File resource;
      if(this.path == null || this.path.equals("") || this.path.equals("/")) resource = new File(this.root+"/index.html");
      else resource = new File(this.root+this.path);
      System.out.println("Static Responder EP:"+this.getEndPoint());
      System.out.println("Static Responder root:"+this.root);
      System.out.println("Static Responder path:"+this.path);
      System.out.println("Static Responder resource:"+resource.toString());

      try {
         if(resource.exists()) body = Files.readString(resource.toPath());
         else throw new server.util.ResponderException("Path: "+this.path+" does not exist");
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }
   
}

