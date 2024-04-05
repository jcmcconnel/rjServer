package responder;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;

public class StaticResponder extends server.util.AbstractResponder
{

   public StaticResponder(String root, String ep)
   {
      super(root, ep);
   }

   protected String getBody(String target) throws server.util.ResponderException
   {
      String body = "";
      File resource;
      resource = new File(this.root+this.path+"/index.html");
      System.out.println("Static Responder EP:"+this.getEndPoint());
      System.out.println("Static Responder root:"+this.root);
      System.out.println("Static Responder path:"+this.path);
      System.out.println("Static Responder resource:"+resource.toString());

      if(resource.exists()) System.out.println("Static Responder resource does exist");
      
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

