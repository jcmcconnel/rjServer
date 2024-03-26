/*
 * AbstractXHTMLBasedFile.java
 * 
 * October 1, 2008
 * @Author James McConnel
 *
 * License: Artistic, http://www.opensource.org/licenses/artistic-license-2.0.php
 *
 * Originally created for recipejar
 */
//package recipejar.lib;
package djava.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;
import djava.util.XHTMLElement;

/**
 * Defines general functions that are used by the recipejar.recipe.RecipeFile
 * and recipejar.Index.IndexFile classes.
 * 
 * @author James McConnel
 */
public abstract class AbstractXHTMLBasedFile extends File {

   /*
   Public Members
    */

   /**
    * Use the same way as the File(String) constructor.
    *
    * @param name The name of the file.
    */
   public AbstractXHTMLBasedFile(String name) {
      super(name);
      initialize();
   }

   /**
    * This Constructor does NOT absorb the File provided.
    * It creates a new instance from the getPath() method of the given file.
    *
    * @param f The file to read.
    */
   public AbstractXHTMLBasedFile(File f) {
      super(f.getPath());
      initialize();
   }

   /**
    * Returns the content attribute of the meta tag that has the name attribute
    * given by the parameter, or if a meta tag with this name does not
    * exist returns null.
    *
    * @param name The name to compare meta names to.
    * @return The content of the meta tag or null if it does not exist.
    */
   public String getMetaData(String name) {
      for (int i = 0; i < MetaData.size(); i++) {
         if (MetaData.get(i).hasAttribute("name") && MetaData.get(i).getAttribute("name").equals(name)) {
            return MetaData.get(i).getAttribute("content");
         }
      }
      return null;
   }

   /**
    * Adds an element to the MetaData ArrayList with a "name" attribute given by
    * the value of the name parameter, and a "content" attribute given the
    * value of the content parameter.
    *
    * Replaces any previous values.
    *
    * @param name A String.  The name of the new meta data.
    * @param content A String. The content of the new meta data.
    */
   public void setMetaData(String name, String content) {
      if (getMetaData(name) == null) {
         XHTMLElement e;
         e = new XHTMLElement("meta");
         e.setAttribute("name", name);
         e.setAttribute("content", content);
         MetaData.add(e);
      } else {
         for (int i = 0; i < MetaData.size(); i++) {
            String s = MetaData.get(i).getAttribute("name");
            if (s != null && s.equals(name)) {
               MetaData.get(i).setAttribute("content", content);
               return;
            }
         }
      }
   }

   /**
    * Returns the requested element.
    * @param name The name attribute of the element required.
    * @return
    */
   public XHTMLElement getMetaElement(String name) {
      for (int i = 0; i < MetaData.size(); i++) {
         if (MetaData.get(i).hasAttribute("name") && MetaData.get(i).getAttribute("name").equals(name)) {
            return MetaData.get(i);
         }
      }
      return null;
   }

   /**
    *
    * @return The content of the &lt;title&gt; element.
    */
   public String getTitle() {
      if (!DataElements.containsKey("title")) {
         this.DataElements.put("title", new XHTMLElement("title"));
      }
      return this.DataElements.get("title").getContent();
   }

   /**
    * Changes the content of the &lt;title&gt; element.
    *
    * @param newTitle The new &lt;title&gt; content.
    */
   public void setTitle(String newTitle) {
      if (!DataElements.containsKey("title")) {
         XHTMLElement e = new XHTMLElement("title");
         e.setContent(newTitle);
         this.DataElements.put("title", e);
      }
      this.DataElements.get("title").setContent(newTitle);
   }

   /**
    *
    * @return The content of the &lt;style&gt; element.
    */
   public String getStyle() {
      if (!DataElements.containsKey("style")) {
         XHTMLElement e = new XHTMLElement("style");
         e.setAttribute("type", "text/css");
         this.DataElements.put("style", e);
      }
      return this.DataElements.get("style").getContent();
   }

   /**
    * Changes the content of the &lt;style&gt; element.
    *
    * @param newStyle the new content.
    */
   public void setStyle(String newStyle) {
      if (!DataElements.containsKey("style")) {
         XHTMLElement e = new XHTMLElement("style");
         e.setAttribute("type", "text/css");
         e.setContent(newStyle);
         this.DataElements.put("style", e);
      }
      this.DataElements.get("style").setContent(newStyle);
   }

   /**
    * Writes the String given by the toString() function to this file on disk.
    *
    * @see #toString() toString()
    * @throws java.io.IOException
    */
   public void save() throws IOException {
      prepforSave();
      XHTMLElement e;
      if (!this.exists()) {
         e = new XHTMLElement("meta");
         e.setAttribute("name", "created");
         e.setAttribute("content", Calendar.getInstance().getTime().toString());
         MetaData.add(e);
         createNewFile();
      } else if (getMetaData("created") == null) {
         e = new XHTMLElement("meta");
         e.setAttribute("name", "created");
         e.setAttribute("content", "Sometime before, " + Calendar.getInstance().getTime().toString());
         MetaData.add(e);
      }
      if ((e = getMetaElement("last saved")) == null) {
         e = new XHTMLElement("meta");
         e.setAttribute("name", "last saved");
         MetaData.add(e);
      }
      e.setAttribute("content", Calendar.getInstance().getTime().toString());
      FileWriter out = new FileWriter(this);
      out.write(toString());
      out.close();
      cleanUpAfterSave();
   }

   /**
    * An exact String representation of this file.  This is exactly
    * what is written to disk when the save() function is called.
    *
    * @see #save() save()
    * @return A String representation of this file.
    */
   @Override
   public String toString() {
      StringWriter s = new StringWriter();
      s.write(doctype + "\n<html>");
      s.write(buildHead());
      s.write(buildBody());
      s.write("</html>");
      return s.toString();
   }

   /**
    * Adds tokens to the parsing system.
    * @param s
    */
   public void addToken(String s){
      tokens.add(s);
   }

   /*
   Protected Members
   */

   /**
    * The doctype that this file will have if saved using the save function.
    */
   protected static final String doctype =
           "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
   /**
    * These are the element names that the parse function looks for.
    */
   private ArrayList<String> tokens = new ArrayList<String>();

   /**
    * This is the a list of all the meta data found by the parse function,
    * or added at runtime.
    */
   private ArrayList<XHTMLElement> MetaData;

   public HashMap<String, XHTMLElement> getDataElements() {
      return DataElements;
   }

   public ArrayList<XHTMLElement> getMetaItems() {
      return MetaData;
   }

   /**
    * Copied almost wholesale from MacroTextAction.
    * replaces (for example) "[title]" in the given string with the title of this recipe.
    * Should probably be used *only* by the buildBody method.
    * Replaces several commands.
    * @param macroString
    * @return
    */
   protected String processMacros(String macroString) {
      try {
         StringReader in = new StringReader(macroString);
         int c = in.read();
         StringWriter textStroke = new StringWriter();//the final processed string.
         while (c != -1) {
            if (c == '[') {
               StringWriter s = new StringWriter();
               s.write(c);
               c = in.read();
               while (c != -1 && c != ']') {
                  //in case of an unpaired bracket, need to start this loop again.
                  if (c == '[') {
                     textStroke.write(s.toString());//Nothing to process, add to string and
                     s = new StringWriter();//reinitialize the buffer.
                  }
                  s.write(c);
                  c = in.read();
               }
               //picks up the closing bracket.
               if (c != -1) {
                  s.write(c);
               }
               if (s.toString().toUpperCase().equals("[TITLE]")) {
                  textStroke.write(this.getTitle());
               } else if (s.toString().toUpperCase().equals("[LASTSAVE]")) {
                  String savedMeta;
                  if ((savedMeta = getMetaData("last saved")) != null) {
                     textStroke.write(savedMeta);
                  } else {
                     textStroke.write((new Date(this.lastModified())).toString());
                  }
               } else if (s.toString().toUpperCase().equals("[CREATED]")) {
                  String savedMeta;
                  if ((savedMeta = getMetaData("created")) != null) {
                     textStroke.write(savedMeta);
                  } else {
                     textStroke.write("Unknown.");
                  }
               } else if (s.toString().toUpperCase().equals("[VERSION]")) {
                  try {
                     InputStream inAbout = ClassLoader.getSystemResourceAsStream("about.txt");
                     String temp = new String();
                     int d = inAbout.read();
                     while (d != '\n' && d != '\r') {
                        temp = temp + (char) d;
                        d = inAbout.read();
                     }
                     textStroke.write(temp);
                  } catch (IOException ex) {
                     textStroke.write("RecipeJar");
                  }
               } else {
                  textStroke.write(getMacroText(s.toString()));
               }
            } else {
               textStroke.write(c);
            }
            c = in.read();
         }
         return textStroke.toString();
      } catch (IOException ex) {
         return macroString;
      }
   }

   /**
    * Allows subclasses to provide their own specific commands.
    * @param macro
    * @return
    */
   protected abstract String getMacroText(String macro);

   /**
    * Adds a new element to the DataElements ArrayList.
    *
    * @param title name of the new element
    * @param attr The attributes of the new element
    * @param content the content of the new element
    * @return the new element
    */
   protected XHTMLElement setDataElement(String title, HashMap<String, String> attr, String content) {
      XHTMLElement e = new XHTMLElement(title, attr);
      e.setContent(content);
      DataElements.put(tokenize(e), e);
      return e;
   }

   /**
    * Puts the supplied element into the set of DataElements
    *
    * @param title
    * @param e
    * @return
    */
   protected XHTMLElement setDataElement(String title, XHTMLElement e) {
      DataElements.put(title, e);
      return e;
   }

   protected void removeDataElement(String s){
      DataElements.remove(s);
   }

   /**
    *
    * @param s
    * @return
    */
   public boolean dataElementExists(String s) {
      return DataElements.containsKey(s);
   }

   /**
    *
    * @param s
    * @return
    */
   public XHTMLElement getDataElement(String s) {
      return this.DataElements.get(s);
   }

   /**
    * Runs the parse function on this file.
    * 
    * @throws java.io.IOException 
    */
   public void load() throws IOException {
      parse(new FileReader(this));
   }

   /**
    * Builds and returns a String which contains the &lt;body&gt; element (inclusive) of this
    * file.  In other words the String returned by this function should begin (ignoring whitespace)
    * with the &lt;body&gt; tag and end (also ignoring whitespace) with the &lt;/body&gt; tag.
    * To put it another way, buildBody().trim() will always begin with &lt;body&gt; and
    * end with &lt;/body&gt;.
    * 
    * This function is called by the toString() function.  
    * 
    * @return A String containing the body.
    */
   protected abstract String buildBody();

   /**
    * Builds and returns a String which contains the &lt;head&gt; element (inclusive) of this
    * file.  In other words the String returned by this function begins (ignoring whitespace)
    * with the &lt;head&gt; tag and ends (also ignoring whitespace) with the &lt;/head&gt; tag.
    * To put it another way, buildHead().trim() will always begin with &lt;head&gt; and
    * end with &lt;/head&gt;.
    * 
    * This function is called by the toString() function.  
    * 
    * @return A String containing the body.
    */
   protected String buildHead() {
      StringWriter writer = new StringWriter();
      writer.write("\n  <head>\n    ");
      if (MetaData != null) {
         for (int i = 0; i < MetaData.size(); i++) {
            writer.write(processMacros(MetaData.get(i).toString()) + "\n    ");
         }
      }
      if (DataElements.containsKey("title")) {
         writer.write(DataElements.get("title").toString() + "\n    ");
      }
      if (DataElements.containsKey("style")) {
         writer.write(DataElements.get("style").toString() + "\n    ");
      }
      writer.write("\n  </head>");
      return writer.toString();
   }

   /*
   Private Members
    */
   /**
    * These are the elements that the parse function identifies
    * as and saves because they were named in the tokens ArrayList.
    */
   private HashMap<String, XHTMLElement> DataElements;

   private void initialize() {
      tokens.add("title");
      tokens.add("style");
      DataElements = new HashMap<String, XHTMLElement>();
      MetaData = new ArrayList<XHTMLElement>();
      XHTMLElement e = new XHTMLElement("meta");
      e.setAttribute("http-equiv", "Content-Type");
      e.setAttribute("content", "text/html; charset=UTF-8");
      MetaData.add(e);
      e = new XHTMLElement("meta");
      e.setAttribute("name", "generator");
      e.setAttribute("content", "RecipeJar");
      MetaData.add(e);
   }

   /**
    * Used by parse to determine if the given element is one of interest.
    *
    * @return the token associated with this element if there is one, otherwise null.
    */
   private String tokenize(XHTMLElement e) {
      for (int i = 0; i < tokens.size(); i++) {
         //for instance: the style tag.
         if (tokens.get(i).equals(e.getName())) {
            return e.getName();
         }
         if (e.hasAttribute("id") && tokens.get(i).equals(e.getAttribute("id"))) {
            return e.getAttribute("id");
         }
         if (e.hasAttribute("class") && tokens.get(i).equals(e.getAttribute("class"))) {
            return e.getAttribute("class");
         }
      }
      return null;
   }

   /**
    * Removes comments from the input stream. Returns last char read.
    * @param in
    * @return The last char read
    * @throws IOException
    */
   protected int loseCommentsEtc(Reader in) throws IOException {
      int c = in.read();
      if (c == '-') { //First dash, the comments can contain anything, including '>' chars.
         c = in.read();
         if (c == '-') {//second dash
            c = in.read();
            while (c != -1) {
               c = in.read();
               if(c == '-'){
                  c = in.read();
                  if(c == '-'){
                     c = in.read();
                     if(c == '>'){
                        return c;
                     }
                  }
               }
            }
         }
      }
      while (c != '>') {
         c = in.read();
      }
      return c;
   }

   /**
    * For use only by the parse function.
    * @param e
    * @return
    */
   private boolean isNewMetaData(XHTMLElement e) {
      for (int i = 0; i < MetaData.size(); i++) {
         if (MetaData.get(i).hasAttribute("name") && e.hasAttribute("name")) {
            if (MetaData.get(i).getAttribute("name").equals(e.getAttribute("name"))) {
               return false;
            }
         }
         if (MetaData.get(i).hasAttribute("http-equiv") && e.hasAttribute("http-equiv")) {
            if (MetaData.get(i).getAttribute("http-equiv").equals(e.getAttribute("http-equiv"))) {
               return false;
            }
         }
         if (MetaData.get(i).equals(e)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Searches the xhtml file for elements of interest as defined by the names
    * in the tokens list and adds them to the DataElements object.  Also adds meta tags to the MetaData ArrayList.
    * 
    * @param in Reader for the input source for this object.
    * @throws java.io.IOException Thrown by any one of the many "reads".
    */
   private void parse(Reader in) throws IOException {
      int c = in.read();
      while (c != -1) {
         if (c == '<') {
            c = in.read();
            //If encountered thing is an instruction of some kind...
            if (c == '!' || c == '?') {
               loseCommentsEtc(in);
               c = in.read();
            } else {
               //this is an element.
               String tag = new String();
               while (c != '>') {
                  tag = tag + (char) c;
                  c = in.read();
               }
               XHTMLElement e = XHTMLElement.parse(tag);

               if (e != null) {
                  if (e.getName().toLowerCase().equals("meta")) {
                     if (isNewMetaData(e)) {
                        MetaData.add(e);
                     }
                  } else if (!e.isSelfClosing()) {
                     String content = parseForContentAsText(e.getName(), in);
                     //what do I do with the tag, once I have it?
                     String tokenName;
                     if ((tokenName = tokenize(e)) != null && //is wanted,
                             !DataElements.containsKey(tokenName)) {//but only once.
                        e.setContent(content);
                        DataElements.put(tokenName, e);
                     } else {
                        parse(new StringReader(content));
                     }
                  }
               }
            }
         } else {
            if (c != -1) {
               c = in.read();
            }
         }
      }
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
      int c = in.read();
      while (c != -1) {
         if (c == '<') {
            c = in.read();
            //If encountered thing is an instruction of some kind...
            if (c == '!' || c == '?') {
               loseCommentsEtc(in);
               c = in.read();
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
      throw new IOException("End of Stream unexpectedly reached: " + this.getTitle());
   }

   protected abstract void prepforSave();

   protected abstract void cleanUpAfterSave();
}
