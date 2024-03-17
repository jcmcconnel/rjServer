package djava;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;

public class PageResponder extends Responder
{
   public PageResponder(String ep)
   {
      super(ep);
   }

   protected String getBody(String target)
   {
      String body = "";
      File ep = new File("pages"+this.getEndPoint()+"/index.html");
      
      try {
         body = Files.readString(ep.toPath());
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }
}

