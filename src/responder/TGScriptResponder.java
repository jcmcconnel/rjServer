package responder;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Stack;
import java.util.Scanner;
import java.lang.*;

import server.util.AbstractXHTMLBasedFile;

public class TGScriptResponder extends server.util.AbstractResponder
{

   public TGScriptResponder(File root, String ep, String[] exts)
   {
      super(root, ep, exts);
   }

   /**
    *
    **/
   protected String getBody(String target) throws server.util.ResponderException
   {
      String body = "";
      File resource = new File(this.root+"/index."+this.fileExtensions[1]);
      if(this.path == null || this.path.equals("") || this.path.equals("/")){
         for(int i=0; i<this.fileExtensions.length; i++){
            resource = new File(this.root+"/index."+this.fileExtensions[i]);
            if(resource.exists()) break;
         }
      } else {
         int pathExtIndex = this.path.lastIndexOf(".");
         String pathExt = "";
         if(pathExtIndex > 0) pathExt = this.path.substring(pathExtIndex);
         System.out.println("Ext: "+pathExt);
         resource = new File(this.root+this.path);
         for(int i=0; i<this.fileExtensions.length; i++){
            if(pathExtIndex > 0){
               if(pathExt.equals(this.fileExtensions[i])) break;
            } else {
               resource = new File(this.root+this.path+"/index."+this.fileExtensions[i]);
               if(resource.exists()) break;
            }
         }
      }

      try {
         if(resource.exists()) body = Files.readString(resource.toPath());
         else throw new server.util.ResponderException("Path: "+this.path+" does not exist");
         body = parse(body);
      }
      catch(IOException e) {
         System.out.println(e);
      }
      return body+"\n";
   }

   private String processTGScript(String body) throws IOException {
      Scanner in = new Scanner(body);
      StringBuilder out = new StringBuilder();
      while(in.hasNextLine()){
         String line = in.nextLine();
         if(line.trim().startsWith("//")) continue;
         if(line.trim().startsWith("echo")){
            int start = line.indexOf("\"")+1;
            int end = line.lastIndexOf("\"");
            String content = line.substring(start, end);
            if(content.trim().startsWith("$param")){
               start = content.indexOf("$param(")+8;
               end = content.indexOf("\')", start);
               String key = content.substring(start, end);
               if(currentParameters.containsKey(key)) {
                  ProcessBuilder pb = new ProcessBuilder("pandoc", "docs/"+currentParameters.get(key));
                  pb.directory(this.root);
                  InputStream r = pb.start().getInputStream();
                  int c = r.read();
                  while(c != -1){
                     out.append((char)c);
                     c = r.read();
                  }
               }
            } else out.append(content);
         } else out.append(line);
      }

      return out.toString();
   }

   private String parse(String body) throws IOException {
      StringReader in = new StringReader(body);
      StringBuilder out = new StringBuilder();
      char buffer = 0;
      int c = in.read();
      while (c != -1) {
         if (c == '<') {
            buffer = (char)c;
            c = in.read();
            //If encountered thing is an instruction of some kind...
            if (c == '!' || c == '?') {
               out.append((char)buffer);
               out.append((char)c);
               out.append(getCommentsEtc(in));
               c = in.read();
               buffer = 0;
            } else {
               //this is an element.
               String tag = new String();
               while (c != '>') {
                  tag = tag + (char) c;
                  c = in.read();
               }
               server.util.XHTMLElement e = server.util.XHTMLElement.parse(tag);

               if (e != null) {
                  if (!e.isSelfClosing()) {
                     String content = parseForContentAsText(e.getName(), in);
                     //what do I do with the tag, once I have it?
                     if(e.getName().equals("tgscript")) out.append(processTGScript(content));
                     else {
                        e.setContent(parse(content));
                        out.append(e.toString());
                     }
                     c = in.read();
                  }
               }
            }
         } else {
            if (c != -1) {
               out.append((char)c);
               c = in.read();
            }
         }
      }
      
      return out.toString();
   }

   /**
    * Simply returns all the text within the element with the name "name". 
    * Assumes that the beginning tag (top tag) has already been removed from
    * the stream.
    *  
    * @param name Name of the element of interest.
    * @param in The stream to read from.
    * @return Content of the element.
    * @throws java.io.IOException Thrown by anyone of the many read statements.
    */
   protected String parseForContentAsText(String name, Reader in) throws IOException {
      Stack<Integer> stack = new Stack<Integer>();
      stack.push(1);

      StringWriter content = new StringWriter();
      char buffer = 0;
      int c = in.read();
      while (c != -1) {
         if (c == '<') {
            buffer = (char)c;
            c = in.read();
            //If encountered thing is an instruction of some kind...
            if (c == '!' || c == '?') {
               content.write(buffer);
               content.write(c);
               content.write(getCommentsEtc(in));
               c = in.read();
               buffer = 0;
            } else if (c == '/') {
               //this is an end tag.
               StringWriter tagname = new StringWriter();
               //c contains the first character in the tag name.
               c = in.read();
               while (c != '>') {
                  tagname.write(c);
                  c = in.read();
               }

               if (tagname.toString().equals(name)) {
                  stack.pop();
                  if (stack.empty()) {
                     return content.toString();//this should be the only way out of the function.
                  }

               } else {
                  content.write("</");
                  content.write(tagname.toString());
                  content.write(">");
               }
               c = in.read();
            } else {
               //this is an element.
               StringWriter tagname = new StringWriter();
               //c contains the first character in the tag name.
               while (Character.isLetterOrDigit(c)) {
                  tagname.write(c);
                  c = in.read();
               }
               if (tagname.toString().equals(name)) {
                  stack.push(1);
               }
               content.write("<");
               content.write(tagname.toString());
               while (c != '>') {
                  content.write(c);
                  c = in.read();
               }

               content.write(c);
               c = in.read();
            }

         } else {
            if (c != -1) {
               content.write(c);
               c = in.read();
            }
         }

      }
      throw new IOException("End of Stream unexpectedly reached: ");
   }
   
   /**
    * Returns comments from the input stream.
    * @param in
    * @return The read comment or whatever
    * @throws IOException
    */
   protected String getCommentsEtc(Reader in) throws IOException {
      StringBuilder out = new StringBuilder();
      int c = in.read();
      out.append((char)c);
      if (c == '-') { //First dash, the comments can contain anything, including '>' chars.
         c = in.read();
         out.append((char)c);
         if (c == '-') {//second dash
            c = in.read();
            out.append((char)c);
            while (c != -1) {
               c = in.read();
               out.append((char)c);
               if(c == '-'){
                  c = in.read();
                  out.append((char)c);
                  if(c == '-'){
                     c = in.read();
                     out.append((char)c);
                     if(c == '>'){
                        return out.toString();
                     }
                  }
               }
            }
         }
      }
      while (c != '>') {
         c = in.read();
         out.append((char)c);
      }
      return out.toString();
   }
   
}

