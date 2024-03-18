package djava;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;

public class PageResponder extends Responder
{
   String root;

   public PageResponder(String ep, String root)
   {
      super(ep);
      this.root = root;
   }

   protected String getBody(String target)
   {
      String body = "";
      File ep = new File(root+this.getEndPoint()+"/index.html");
      
      try {
         body = Files.readString(ep.toPath());
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }
}

