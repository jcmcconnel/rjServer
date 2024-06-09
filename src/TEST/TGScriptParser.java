package TEST;

import java.io.*;
import java.util.*;
public class TGScriptParser {

   private static HashMap<String, String> tokens = new HashMap<String, String>();
   private HashMap<String, HashMap<String, Object>> context = new HashMap<String, HashMap<String, Object>>();

   private static HashMap<String, String> commands = new HashMap<String, String>();

   public TGScriptParser(File inFile){
      try{
        tokens.put("string", "variable");
        tokens.put("if", "branch");
        tokens.put("while", "loop");

        commands.put("print", "");
        process(new FileInputStream(inFile));
      } 
      catch(IOException e){
         System.out.println(e);
      } 
   }

   public static void main(String args[]) {
      TGScriptParser p;
      if(args.length == 0) {
         System.out.println("Usage: [OPTIONS] [FILE]");
      } else if(args.length == 1) {
         File inFile = new File(args[0]);
         if(inFile.exists()) {
            p = new TGScriptParser(inFile);
         } else return;
      }
   }

   private void process(InputStream in) throws IOException {
      int c;
      String buffer = new String();
      while(in.available() > 0){
         c = in.read();
         if(c != '\n' && c != ' ') buffer += (char)c;
         if(tokens.containsKey(buffer.trim().toLowerCase())) {
            processToken(in, buffer.trim().toLowerCase());
            buffer = "";
         } //else System.out.println(buffer);
      }
   }

   private void processToken(InputStream in, String token) throws IOException {
      System.out.println("found token: "+token);
      if(tokens.get(token).equals("branch")){
         System.out.println("parsing for branch");
         int c = 0;
         String buffer = "";
         while(in.available() > 0){
            c = in.read();
            if(c == '\n' || c == ' ') continue; 
            if(c == '(') {
               break;
            } else throw new IOException("'(' Expected");
         }
         if(processCondition(in)){
            System.out.println("Following true path");
            String body = getBody(in);
            System.out.println(body);
            processBody(new ByteArrayInputStream(body.getBytes()));
            //Find and reject else
            while(in.available() > 0 && buffer.length() < 5){
               c = in.read();
               if(c != '\n' && c != ' ') buffer += (char)c;
               if(buffer.length() == 4 && buffer.equals("else")){
                  System.out.println("Rejected: ");
                  System.out.println(getBody(in));
                  buffer = "";
               } else if(tokens.containsKey(buffer.trim().toLowerCase())){
                  processToken(in, buffer.trim().toLowerCase());
                  break;
               }
            }
            return;
         } else {
            System.out.println("Following false path");
            //Reject next command/block
            //If followed by else processBody it.
            System.out.println("Rejected: ");
            System.out.println(getBody(in));
            buffer = "";
            while(in.available() > 0 && buffer.length() < 5){
               c = in.read();
               if(c != '\n' && c != ' ') buffer += (char)c;
               if(buffer.length() == 4 && buffer.equals("else")){
                  System.out.println("else body:");
                  String body = getBody(in);
                  System.out.println(body);
                  processBody(new ByteArrayInputStream(body.getBytes()));
                  buffer = "";
               } else if(tokens.containsKey(buffer.trim().toLowerCase())){
                  processToken(in, buffer.trim().toLowerCase());
                  break;
               }
            }
         }
         return;
      } else if(tokens.get(token).equals("loop")){
         System.out.println("Found loop");
         String condition = "("+getCondition(in)+")";
         System.out.println("Condition: "+condition);
         String body = getBody(in);
         System.out.println("Body: "+body);
         while(processCondition(new ByteArrayInputStream(condition.getBytes()))) {
            System.out.println("while condition is true");
            processBody(new ByteArrayInputStream(body.getBytes()));
         }
         System.out.println("loop exited");
         return;
      } else if(tokens.get(token).equals("variable")){
         System.out.println("parsing for variable");
         processVarDeclaration(token, in);
         return;
      }
      return;
   }

   private boolean processCondition(InputStream in) throws IOException {
      int c;
      String content = "";
      while(in.available() > 0){
         c = in.read();
         if(c == '(') {
            if(processCondition(in)) content += "true";
            else content += "false";
            if(in.available() == 0) return processLogicStatement(new Scanner(content));
         } else if(c == ')'){
            return processLogicStatement(new Scanner(content));
         } else content += (char)c;
      }
      return false;
   }

   private String getCondition(InputStream in) throws IOException {
      int c;
      int stack = 0;
      String content = "";
      while(in.available() > 0){
         c = in.read();
         if(c == '(') {
            if(stack > 0) content += (char)c;
            stack++;
         } else if(c == ')'){
            stack--;
            if(stack == 0) return content;
            content += (char)c;
         } else content += (char)c;
      }
      return content;
   }

   private boolean processLogicStatement(Scanner in) throws IOException {
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
               else return processLogicStatement(in);
            }
            if(next.equals("&&")) {
               if(contextVal.equals("true")) return processLogicStatement(in);
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

   private String evaluateLogicStatement(Object field, String operator, Object value) {
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

   private void processVarDeclaration(String dataType, InputStream in) throws IOException {
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

   private void processBody(InputStream in) throws IOException {
      //Assume that the input has been normalized to just the content.
      int c = 0;
      String cmd = null;
      String assignmentVar = null;
      int assignmentOp = 0;
      String buffer = new String();
      c = in.read();
      while(in.available() > 0){
         while(in.available() > 0 && (c == '\n' || c == '\r' || c == ' ')) c = in.read();
         if(c == '"') {
            c = in.read();
            while(c != '"') {
               if(c == 0) throw new IOException("'\"' expected");
               buffer += (char)c;
               c = in.read();
            }
            if(cmd != null){
               String[] args = {"\""+buffer.trim()+"\""};
               runCmd(cmd, args);
               cmd = null;
               buffer = null;
            } else if(assignmentVar != null && assignmentOp == '=' && !buffer.equals("")) {
               System.out.println("found assignment value");
               context.get(assignmentVar).put("value", buffer);
               System.out.println(assignmentVar+": New val: "+buffer);
               buffer = "";
               assignmentVar = "";
               assignmentOp = 0;
            }
            c = in.read();
            continue;
         }
         if(c != ';') {
            buffer += (char)c;
            if(cmd == null && assignmentVar == null) {
               if(commands.containsKey(buffer)){
                 cmd = buffer;
                 buffer = "";
               } else if(context.containsKey(buffer)){
                  assignmentVar = buffer;
                  buffer = "";
               }
               c = in.read();
               continue;
            }
            if(assignmentVar != null && c == '=') assignmentOp = c;
            c = in.read();
         } 
         if(c == ';') {
            if(cmd != null){
               String[] args = {buffer.trim()};
               runCmd(cmd, args);
               cmd = null;
               buffer = null;
            } else if(assignmentVar != null && assignmentOp == '=' && !buffer.equals("")) {
               if(assignmentVar != null && assignmentOp == '=' && !buffer.trim().equals("")) {
                  context.get(assignmentVar).put("value", buffer);
               }
               context.get(assignmentVar).put("value", context.get(buffer.trim().toLowerCase()).get("value"));
               buffer = "";
               assignmentVar = "";
               assignmentOp = 0;
            }
         } else if(c == '{') {
            if(cmd != null) throw new IOException("';' expected");
            c = 0;
            int stack = 1;
            buffer = "";
            while(in.available() > 0 && (c == '\n' || c == ' ')) {
               c = in.read();
               buffer += (char)c;
            }
            while(in.available() > 0){
               c = in.read();
               if(c == ';' && stack == 0) {
                  buffer += (char)c;
                  break;
               } else if(c == '{') {
                  if(stack > 0) buffer += (char)c;
                  stack ++;
               } else if(c == '}') {
                  stack --;
                  if(stack == 0) break;
                  buffer += (char)c;
               } else buffer += (char)c;
            }
            processBody(new ByteArrayInputStream(buffer.getBytes()));
            buffer = "";
         } 
         if(in.available() > 0) c = in.read();
      }

   }

   private String getBody(InputStream in) throws IOException {
      int c = 0;
      int stack = 0;
      String buffer = new String();
      while(in.available() > 0 && (c == '\n' || c == ' ')) {
         c = in.read();
         buffer += (char)c;
      }
      while(in.available() > 0){
         c = in.read();
         if(c == ';' && stack == 0) {
            buffer += (char)c;
            return buffer;
         } else if(c == '{') {
            if(stack > 0) buffer += (char)c;
            stack ++;
         } else if(c == '}') {
            stack --;
            if(stack == 0) return buffer;
            buffer += (char)c;
         } else buffer += (char)c;
      }
      throw new IOException("'}' or ';' expected");
   }

   private void runCmd(String cmd, String[] args) {
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
