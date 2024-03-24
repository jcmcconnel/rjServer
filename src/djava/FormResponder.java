package djava;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class FormResponder extends Responder
{

   private HashMap<String, String> localVariables;

   public FormResponder(String ep)
   {
      super(ep);
      localVariables = new HashMap<String, String>();
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
      body.append("        <label for='email'>Enter some text</label>\n");
      body.append("        <textarea id='body' name='body' rows='10' cols'33' required >{num:\"5+3\" para:\"$num\"}</textarea>\n");
      body.append("      </div>\n");
      body.append("      <div class='form-example'>\n");
      body.append("        <input type='submit' value='Subscribe!' />\n");
      body.append("      </div>\n");
      body.append("    </form>\n");
      if(request.containsKey("body")) body.append("<p>Body: "+request.get("body")+"</p>");
      if(request.containsKey("body")) body.append(parseRequestBody());
      body.append("  </body>\n");
      body.append("</html>\n");
      
      body.append("\n");
      return body.toString();
   }

    protected String parseRequestBody(){
      StringBuilder output = new StringBuilder(); 
      String temp = request.get("body");
      int start = temp.indexOf("body={");
      int end = start+6;

      System.out.println("Attempting to parse...");

      if(start != -1) {
         System.out.println("There is a body");
         int braceStack = 1;
         StringBuilder currentKey = new StringBuilder();
         StringBuilder currentValue = new StringBuilder();
         boolean inQuote = false;
         while(braceStack > 0) {
            if(temp.charAt(end) == '}') {
               braceStack--;
               end++;
               continue;
            }
            if(temp.charAt(end) == '{') {
               braceStack++;
               end++;
               continue;
            }
            if(currentKey.toString().endsWith(":")){
                if(inQuote && temp.charAt(end) == '"') {
                  inQuote = false;
                  output.append(getOutput(currentKey.toString().trim(), currentValue.toString()));
                  currentKey = new StringBuilder();
                  currentValue = new StringBuilder();
                } else if(temp.charAt(end) == '"') inQuote = true;
                else currentValue.append(temp.charAt(end));
            } else currentKey.append(temp.charAt(end));
            end++;
         }
         return output.toString();

      } else return "";
    }

    protected String getOutput(String key, String value){
      Scanner in = new Scanner(value);
      StringBuilder output = new StringBuilder();
      int num = 0;
      System.out.println("found key:"+key);
      System.out.println("found value:"+value);
      if(key.equals("num:")){
         while(in.hasNextInt()) {
            int i = in.nextInt();
            System.out.println("found int: "+Integer.toString(i));
            num += i;
         }
         localVariables.put("num", Integer.toString(num));
         return "";
      } else if(key.equals("para:")){
         output.append("<p>");
         while(in.hasNext()) {
            String s = in.next();
            if(s.equals("$num")) output.append(localVariables.get("num"));
            else output.append(s);
         }
         output.append("</p");
         System.out.println("output is: "+output.toString());
         return output.toString();
      }
      return "";
    }
   
}

