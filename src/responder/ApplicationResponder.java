package responder;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.text.DecimalFormat;

public class ApplicationResponder extends server.util.AbstractResponder
{

   private HashMap<String, String> localVariables;
   private server.util.AbstractXHTMLBasedFile template;

   public ApplicationResponder(String r, String ep)
   {
      super(r, ep);
      template = new server.util.AbstractXHTMLBasedFile(this.root+"/index.rjs"){
         protected String buildBody(){
            StringBuilder s = new StringBuilder();
            s.append("<body>");
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

   protected String getBody(String target) throws server.util.ResponderException
   {
      System.out.println("Application responder EP:"+this.getEndPoint());
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
         while(braceStack > 0 && temp.length() > end) {
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
      if(key.equals("factor:")){
         if(localVariables.containsKey("num")) {
            localVariables.put("num", convert(localVariables.get("num"), value, false));
         } else localVariables.put("factor", value);
         return "";
      }else if(key.equals("num:")){
         if(localVariables.containsKey("factor")) {
            localVariables.put("num", convert(value, localVariables.get("factor"), false));
         } else localVariables.put("num", value);
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


    //The following 3 were originally written for recipejar
   /**
    *
    * @param f
    * @return
    */
   public static String decimalToFraction(float f) {
      int wholepart = (int) f;
      f = f - wholepart;
      String output = "";
      if(Math.abs(wholepart) > 0) output = Integer.toString(wholepart)+" ";
      if(f == 0.75) output += "3/4";
      if(f == 0.5) output += "1/2";
      if(f > 0.3 && f < 0.334) output += "1/3";
      if(f == 0.25) output += "1/4";
      if(f == 0.2) output += "1/5";
      if(f == 0.125) output += "1/8";
      if(f == 0.0625) output += "1/16";
      if(!output.isEmpty()) return output;
      return new DecimalFormat("0.##").format(f);
   }


   private static float parseMixedNumber(String qty){
      if (!qty.trim().contains(" ") && qty.contains("/")) {
         //A fraction; if does contain a " " then it's a mixed number
         try {
            float num = Float.parseFloat(qty.substring(0, qty.indexOf("/")));
            float denom = Float.parseFloat(qty.substring(qty.indexOf("/") + 1));
            return num / denom;
         } catch (NumberFormatException numberFException) {
            return -1;//cannot convert
         }
      } else if (qty.trim().contains(" ") && qty.contains("/")) {
         //a mixed number
         try {
            float whole = Float.parseFloat(qty.substring(0, qty.indexOf(" ")));
            float num = Float.parseFloat(qty.substring(qty.indexOf(" ") + 1, qty.indexOf("/")));
            float denom = Float.parseFloat(qty.substring(qty.indexOf("/") + 1));
            return whole + num / denom;
         } catch (NumberFormatException numberFException) {
            return -1;//cannot convert
         }
      }
      return Float.parseFloat(qty);
   }

   /**
    * Parses the number out of the string given by qty,
    * and processes it with the function defined by factor,
    * then returns the result as a string.
    * @param qty
    * @param factor
    * @param outputFraction
    * @return
    */
   private static String convert(String qty, String factor, boolean outputFraction) {
      float x = 0;
      if (qty.isEmpty()) {
         x = 0;
      } else {//Parse out the value of qty
         if (qty.trim().contains("-")) {
            //A range
            return (convert(qty.substring(0, qty.indexOf("-")).trim(), factor, outputFraction) + "-"
                    + convert(qty.substring(qty.indexOf("-") + 1).trim(), factor, outputFraction));
         }
         try {
            x = parseMixedNumber(qty);
         } catch (NumberFormatException numberFormatException) {
            return qty;//cannot convert
         }
      }
      try {
         float result;
         //Plus or minus indicates a function.
         if (factor.contains("+") && factor.split("\\+").length == 2) {
            String[] formula = factor.split("\\+");
            float m = parseMixedNumber(formula[0]);
            float b = parseMixedNumber(formula[1]);
            result = m * x + b;
         } else if (factor.contains("-") && factor.split("-").length == 2) {
            String[] formula = factor.split("-");
            float m = parseMixedNumber(formula[0]);
            float b = parseMixedNumber(formula[1]);
            result = m * x - b;
         } else {
            result = Float.parseFloat(factor) * x;
         }
         if (outputFraction) {
            return decimalToFraction(result);
         } else {
            return (new DecimalFormat("0.##")).format(result);
         }
      } catch (NumberFormatException numberFormatException) {
         return qty;//Cannot parse number
      }
   }
   
}

