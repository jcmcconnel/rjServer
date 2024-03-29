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
      File ep = new File(this.root+this.getEndPoint()+"/index.html");
      resource = new File(this.root+this.getEndPoint()+this.path+"/index.html");
      System.out.println("Static Responder EP:"+this.getEndPoint());
      System.out.println("Static Responder root:"+this.root);
      
      try {
         if(resource.exists()) body = Files.readString(resource.toPath());
         else throw new server.util.ResponderException("Path does not exist");
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }
   
}

