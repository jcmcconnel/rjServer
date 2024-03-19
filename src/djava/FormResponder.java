package djava;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class FormResponder extends Responder
{

   public FormResponder(String ep)
   {
      super(ep);
   }

   protected String getBody(String target) throws ResponderError
   {
      StringBuilder body = new StringBuilder();
      body.append("<!DOCTYPE html>\n");
      body.append("<html>\n");
      body.append("  <head>\n");
      body.append("  </head>\n");
      body.append("  <body>\n");
      body.append("    <form action='' method='post' enctype='text/plain' class='form-example'>\n");
      body.append("      <div class='form-example'>\n");
      body.append("        <label for='name'>Enter your name: </label>\n");
      body.append("        <input type='text' name='name' id='name' required />\n");
      body.append("      </div>\n");
      body.append("      <div class='form-example'>\n");
      body.append("        <label for='email'>Enter your email: </label>\n");
      body.append("        <input type='email' name='email' id='email' required />\n");
      body.append("      </div>\n");
      body.append("      <div class='form-example'>\n");
      body.append("        <input type='submit' value='Subscribe!' />\n");
      body.append("      </div>\n");
      body.append("    </form>\n");
      if(request.containsKey("body")) body.append("<p>Body: "+request.get("body")+"</p>");
      body.append("  </body>\n");
      body.append("</html>\n");
      
      body.append("\n");
      return body.toString();
   }
   
}

