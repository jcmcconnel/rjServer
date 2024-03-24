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
   private AbstractXHTMLBasedFile template;

   public FormResponder(String ep, String pageRoot)
   {
      super(ep);
      template = new AbstractXHTMLBasedFile(pageRoot+this.getEndPoint()+"index.djava"){
         protected String buildBody(){
            StringBuilder s = new StringBuilder();
            s.append("<body>");
            System.out.println(this.getDataElement("form").toString());
            s.append(this.getDataElement("form").toString());
            if(request.containsKey("body")) s.append("<p>Body: "+request.get("body")+"</p>");
            if(request.containsKey("body")) s.append(parseRequestBody());
            s.append("</body>");
            return s.toString();
         }
         protected String buildHead(){
            StringBuilder s = new StringBuilder();
            s.append("<head>");
            s.append(this.getDataElement("title").toString());
            s.append("</head>");
            return s.toString();
         }
         protected void prepforSave(){}
         protected void cleanUpAfterSave(){}
         protected String getMacroText(String macro){return "";}
      };
      template.addToken("form");
      try{
         template.load();
      }catch(IOException e){
          System.out.println(e.toString());
      }
      localVariables = new HashMap<String, String>();
   }

   protected String getBody(String target) throws ResponderError
   {
      return template.toString();
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

