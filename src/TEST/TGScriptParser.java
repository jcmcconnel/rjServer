package TEST;

import java.io.*;
import java.util.*;
public class TGScriptParser {

   private static HashMap<String, String> tokens = new HashMap<String, String>();
   private static HashMap<String, HashMap<String, Object>> context = new HashMap<String, HashMap<String, Object>>();

   private static HashMap<String, String> commands = new HashMap<String, String>();

   public static void main(String args[]) {
      if(args.length == 0) {
         System.out.println("Usage: [OPTIONS] [FILE]");
      } else if(args.length == 1) {
         File inFile = new File(args[0]);
         if(inFile.exists()) {
            try{
              tokens.put("string", "variable");
              tokens.put("if", "branch");

              commands.put("print", "");
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
            //System.out.println("found token: "+buffer);
            if(tokens.get(buffer.trim().toLowerCase()).equals("branch")){
               //System.out.println("parsing for branch");
               while(in.available() > 0){
                  c = in.read();
                  if(c == '\n' || c == ' ') continue; 
                  if(c == '(') {
                     break;
                  } else throw new IOException("'(' Expected");
               }

               if(parseCondition(in)){
                  parseBody(in);
               } else {
                  //System.out.println("Looking for else");
                  //Reject next command/block
                  //If followed by else parseBody it.
                  while(in.available() > 0){
                     c = in.read();
                     if(c == '{') {
                        int bracketStack = 1;
                        while(in.available() > 0){
                           c = in.read();
                           if(c == '{') bracketStack++;
                           else if(c == '}') bracketStack--;
                           if(bracketStack == 0) break;
                        }
                     } else if(c == ';') break;
                  }
                  buffer = "";
                  while(in.available() > 0 && buffer.length() < 5){
                     c = in.read();
                     if(c != '\n' && c != ' ') buffer += (char)c;
                     if(buffer.length() == 4 && buffer.equals("else")){
                        parseBody(in);
                        buffer = "";
                     }
                  }

               }
               //Parse body
               //Parse for else
               buffer = "";
            } else if(tokens.get(buffer.trim().toLowerCase()).equals("variable")){
               //System.out.println("parsing for variable");
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
         if(next.indexOf('"') == 0) while(next.lastIndexOf('"') < next.length()-1) next += " "+in.next();
         //System.out.println(next);

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
            //System.out.println("contextVal: "+contextVal);
            continue;
         } 
         if(contextVal == null) {
            contextVal = next;
            continue;
         }
         if(contextVal != null && op == null) {
            op = next;
            //System.out.println("op: "+op);
            continue;
         }

         if(op != null && context.containsKey(next)){
            compVal = context.get(next).get("value");
            continue;
         }
         if(op != null) {
            if(next.indexOf("\"") == 0 && next.lastIndexOf("\"") == next.length()-1){
               compVal = next.substring(1, next.length()-1);
            } else compVal = next;
            contextVal = evaluateLogicStatement(contextVal, op, compVal);
            op = null;
            compVal = null;
         }
      }
      if(contextVal != null && contextVal.equals("true")) return true;
      else return false;
   }

   private static String evaluateLogicStatement(Object field, String operator, Object value) {
      //System.out.println("els: "+field+", "+operator+", "+value);
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
      String buffer = new String();
      boolean inQuote = false;
      while(in.available() > 0){
         c = in.read();
         if(c == '"') {
            if(inQuote) inQuote = false;
            else inQuote = true;
         }
         if(c != '\n' && c != ' ' && c != ';' || inQuote) buffer += (char)c;
         else if(c == ';') {
            if(cmd != null){
               String[] args = {buffer.trim()};
               runCmd(cmd, args);
               cmd = null;
               buffer = null;
            }
         }
         if(commands.containsKey(buffer)){
           cmd = buffer;
           buffer = "";
         }       
      }

   }

   private static void runCmd(String cmd, String[] args) {
      switch(cmd){
         case "print":
            String s = args[0];
            if(s.indexOf("\"") == 0 && s.lastIndexOf("\"") == s.length()-1){
               System.out.println(s.substring(1, s.length()-1));
            } else if(context.containsKey(s)) {
               System.out.println(context.get(s).get("value"));
            }
            return;
      }
   }
}
