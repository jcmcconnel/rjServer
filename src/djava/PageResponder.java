package djava;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class PageResponder extends Responder
{
   private String root;

   private String path;
   private ArrayList<String> currentParameters;
   private String currentHashId;

   public PageResponder(String ep, String root)
   {
      super(ep);
      this.root = root;
   }

   protected String getBody(String target)
   {
      String body = "";
      String path = "";
      File resource;
      File ep = new File(root+this.getEndPoint()+"/index.html");
      System.out.println("attempting to parse path from: "+target);
      path = parsePath(target);
      System.out.println(path);
      resource = new File(root+this.getEndPoint()+path+"/index.html");

      
      try {
         if(resource.exists()) body = Files.readString(resource.toPath());
         else body = this.getErrorBody(target);
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }
   
   private String parsePath(String target){
      ArrayList<String> pathComponents = new ArrayList<String>();

      StringReader in = new StringReader(target);

      try{
         if(target.startsWith(this.getEndPoint())) in.skip(this.getEndPoint().length());

         String temp = "";
         while(in.ready()) {
            int c = in.read();
            if(c == -1) {
               pathComponents.add(temp);
               break;
            }
            if(c == '/') {
               pathComponents.add(temp);
               temp = "";
            } else if(c == '#') {
               currentHashId = parseIdentifier(in);
               break;
            } else if(c == '?') {
               currentParameters = parseParameters(in);
               break;
            } else temp = temp+(char)c;
         }
      }
      catch(IOException e) {
         System.out.println(e);
         return "";
      }
      return String.join("/", pathComponents);
   }

   private String parseIdentifier(StringReader in) throws IOException {
      String returnValue = "";
      while(in.ready()){
         int c = in.read();
         if(c == -1) break;
         if(c == '?'){
            currentParameters = parseParameters(in);
            break;
         } else returnValue = returnValue + (char)c;
      }
      return returnValue;
   }

   private ArrayList<String> parseParameters(StringReader in) throws IOException {
      ArrayList<String> parameters = new ArrayList<String>();
      String temp = "";
      while(in.ready()){
         int c = in.read();
         if(c == -1) {
            parameters.add(temp);
            break;
         }
         if(c == '&' || c == ';'){
            parameters.add(temp);
            temp = "";
         } else temp = temp + (char)c;
      }
      return parameters;
   }
}

