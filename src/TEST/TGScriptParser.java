package TEST;

import java.io.*;
import java.util.*;
public class TGScriptParser {

   private static HashMap<String, String> tokens = new HashMap<String, String>();
   private static HashMap<String, HashMap<String, Object>> context = new HashMap<String, HashMap<String, Object>>();

   public static void main(String args[]) {
      if(args.length == 0) {
         System.out.println("Usage: [OPTIONS] [FILE]");
      } else if(args.length == 1) {
         File inFile = new File(args[0]);
         if(inFile.exists()) {
            try{
              tokens.put("string", "variable");
              tokens.put("if", "branch");
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
      String buffer = new String();
      while(in.available() > 0){
         c = in.read();
         if(c != '\n' && c != ' ') buffer += (char)c;
         if(tokens.containsKey(buffer.trim().toLowerCase())) {
            System.out.println("found token: "+buffer);
            if(tokens.get(buffer.trim().toLowerCase()).equals("branch")){
               System.out.println("parsing for branch");
               while(in.available() > 0){
                  c = in.read();
                  if(c == '\n' || c == ' ') continue; 
                  if(c == '(') {
                     break;
                  } else throw new IOException("'(' Expected");
               }

               if(parseCondition(in)){
                  parseBody(in);
               }
               //Parse body
               //Parse for else
               buffer = "";
            } else if(tokens.get(buffer.trim().toLowerCase()).equals("variable")){
               System.out.println("parsing for variable");
               parseVarDeclaration(buffer.trim().toLowerCase(), in);
               buffer = "";
            }
         }
      }
   }

   private static boolean parseCondition(InputStream in) throws IOException {
      int c;
      String content = "";
      while(in.available() > 0){
         c = in.read();
         if(c == '(') {
            if(parseCondition(in)) content += "true";
            else content += "false";
         } else if(c == ')'){
            return parseLogicStatement(new Scanner(content));
         } else content += (char)c;

      }
      return false;
   }

   private static boolean parseLogicStatement(Scanner in) throws IOException {
      Object contextVal = null;
      String op = null;
      Object compVal = null;
      while(in.hasNext()){
         String next = in.next().trim().toLowerCase();

         if(contextVal != null) { 
            if(next.equals("||")) {
               if(contextVal.equals("true")) return true;
               else return parseLogicStatement(in);
            }
            if(next.equals("&&")) {
               if(contextVal.equals("true")) return parseLogicStatement(in);
               else return false;
            }
         }

         if(contextVal == null && context.containsKey(next)) {
            contextVal = context.get(next).get("value"); 
            continue;
         } 
         if(contextVal == null) {
            contextVal = next;
            continue;
         }
         if(contextVal != null) {
            op = next;
            continue;
         }

         if(op != null && context.containsKey(next)){
            compVal = context.get(next).get("value");
            break;
         }
         if(op != null) {
            compVal = next;
            contextVal = evaluateLogicStatement(contextVal, op, compVal);
            op = null;
            compVal = null;
         }
      }
      if(contextVal != null && contextVal.equals("true")) return true;
      else return false;
   }

   private static String evaluateLogicStatement(Object field, String operator, Object value) {
      switch(operator){
         case "==":
            return (new Boolean(field.equals(value))).toString();
         case "!=":
            return (new Boolean(!field.equals(value))).toString();
      }
      return "false";

      //   case ">=":
      //      return field.equals(value).toString();
      //   case "<=":
      //      return field.equals(value).toString();
      //   case ">":
      //      return field.equals(value).toString();
      //   case "<":
      //      return field.equals(value).toString();
      //}
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
            if(dataType.equals("string")) {
               int start = content.indexOf("\"")+1;
               int end = content.lastIndexOf("\"");
               varDef.put("value", content.substring(start, end));
            } else varDef.put("value", content);
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
