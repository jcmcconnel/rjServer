package TEST;

import java.io.*;
import java.util.*;
public class TGScriptParser {

   private static HashMap<String, HashMap<String, Object>> context = new HashMap<String, HashMap<String, Object>>();

   public static void main(String args[]) {
      if(args.length == 0) {
         System.out.println("Usage: [OPTIONS] [FILE]");
      } else if(args.length == 1) {
         File inFile = new File(args[0]);
         if(inFile.exists()) {
            try{
              parse(new FileInputStream(inFile));
            } 
            catch(IOException e){
               System.out.println(e);
            } 
         } else return;
      }
   }

   private static void parse(InputStream in) throws IOException {
      int c;
      String[] tokens = {"if", "String"};
      String buffer = new String();
      while(in.available() > 0){
         c = in.read();
         if(c != '\n' && c != ' ') buffer += (char)c;
         if(buffer.trim().equals("if")){
            if(parseIfCondition(in)){
               parseBody(in);
            }
            //Parse body
            //Parse for else
            buffer = "";
         } else if(buffer.equals("String")){
            parseVarDeclaration("String", in);
            buffer = "";
         }
      }
   }

   private static boolean parseIfCondition(InputStream in) throws IOException {
      int c;
      int parenthesisStack = 0;
      String content = new String();
      while(in.available() > 0){
         c = in.read();
         if(c == '(') {
            parenthesisStack++;
         } else if(c == ')'){
            parenthesisStack--;
         } else content += (char)c;
         if(parenthesisStack == 0 && !content.isEmpty()) {
            if(content.equals("true")) return true;
         } 
      }
      return false;
   }

   private static void parseVarDeclaration(String dataType, InputStream in) throws IOException {
      int c;
      String varName = "";
      HashMap<String, Object> varDef = new HashMap<String, Object>();
      String content = new String();
      while(in.available() > 0){
         c = in.read();
         if(c == '=') {
            varName = content.trim();
            content = "";
         } else if(c == ';'){
            varDef.put("type", dataType);
            varDef.put("value", content);
            context.put(varName, varDef);
            return;
         } else content += (char)c;
      }
   }

   private static void parseBody(InputStream in) throws IOException {
      int c;
      String cmd = null;
      String[] tokens = {"print"};
      String buffer = new String();
      while(in.available() > 0){
         c = in.read();
         if(c != '\n' && c != ' ' && c != ';') buffer += (char)c;
         else if(c == ';') {
            if(cmd != null){
               String token = buffer.trim();
               System.out.println(context.get(token).get("value"));
            }
         }
         if(buffer.equals("print")){
           cmd = "print";
           buffer = "";
         }       
      }

   }
}
