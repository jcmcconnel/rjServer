/**
 * Element.java
 * 
 * @Author James McConnel
 *
 * License: Artistic, http://www.opensource.org/licenses/artistic-license-2.0.php
 * Originally created for recipejar
 */
//package recipejar.lib;
package djava.util;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Stores data about elements in the file.
 * Useful mainly as a bookkeeping structure for the parse functions.
 *
 * However, some elements are retained in the DataElements Vector, because
 * this allows for quick retrieval of important information at runtime.
 * The Elements retained by the AbstractXHTMLFile also make it easy to
 * recreate the file from scratch as happens in the save function.
 */
public class XHTMLElement{

   private String name;
   private HashMap<String, String> attributes;
   private String content = "";
   private boolean selfclosing = false;

   public XHTMLElement(String s) {
      name = s;
      if(name.toLowerCase().equals("meta")){
         selfclosing = true;
      }
      attributes = new HashMap<String, String>();
   }

   public XHTMLElement(String s, HashMap<String, String> attr) {
      name = s;
      if(name.toLowerCase().equals("meta")){
         selfclosing = true;
      }
      attributes = attr;
   }

   /**
    * Just parses out the opening tag, not the content.
    * @param s
    * @return
    */
   public static XHTMLElement parse(String s) {
      if (s.length() == 0) {
         return null;
      }
      String name = new String();
      char c;
      int index = 0;
      //c contains the first character in the tag name.
      for (; index < s.length(); index++) {
         c = s.charAt(index);
         if (!Character.isLetterOrDigit(c)) {
            break;
         } else {
            name = name + c;
         }
      }
      XHTMLElement e = new XHTMLElement(name);

      String rot = s.substring(index).trim();//rest of tag
      //For each attribute.
      while (!rot.isEmpty() && rot.indexOf("=") != -1) {
         rot = rot.trim();
         String key = rot.substring(0, rot.indexOf("="));
         rot = rot.substring(rot.indexOf("=") + 2);//the '=' and the '"'
         //the following is to account for escaped chars.
         StringWriter value = new StringWriter();
         for (int j = 0; j < rot.length(); j++) {
            if (rot.charAt(j) == '\\') {
               value.write(rot.charAt(j));
               j++;
               value.write(rot.charAt(j));
            } else if (rot.charAt(j) == '"') {
               rot = rot.substring(j + 1);
               break;
            } else {
               value.write(rot.charAt(j));
            }
         }
         e.setAttribute(key, value.toString());
      }
      if (!rot.isEmpty() && rot.charAt(rot.length()-1) == '/') {
         e.selfclosing = true;
      }

      if (!e.isSelfClosing() &&
              (e.getName().toLowerCase().equals("hr") ||
              e.getName().toLowerCase().equals("br") ||
              e.getName().toLowerCase().equals("meta"))) {
         e.selfclosing = true;
      }
      return e;
   }

   @Override
   public String toString() {
      StringWriter s = new StringWriter();
      s.write("<");
      s.write(buildTopTagWOBraces());
      if (content.isEmpty() && selfclosing) {
         s.write("/>");
      } else {
         s.write(">" + content + "</" + name + ">");
      }
      return s.toString();
   }

   public void setAttribute(String key, String value) {
      attributes.put(key, value);
   }

   protected String buildTopTagWOBraces() {
      StringWriter s = new StringWriter();
      s.write(name);
      if (!attributes.isEmpty()) {
         //These attributes should always be first, but only one should be in any element.
         if (attributes.containsKey("id")) {
            s.write(" id=\"" + attributes.get("id") + "\"");
         } else if (attributes.containsKey("name")) {
            s.write(" name=\"" + attributes.get("name") + "\"");
         } else if (attributes.containsKey("http-equiv")) {
            s.write(" http-equiv=\"" + attributes.get("http-equiv") + "\"");
         }
         Object[] keys = attributes.keySet().toArray();
         for (int i = 0; i < keys.length; i++) {
            if (!keys[i].equals("id") && !keys[i].equals("name") && !keys[i].equals("http-equiv")) { //already output.
               s.write(" " + keys[i] + "=\"" + attributes.get(keys[i]) + "\"");
            }
         }
      }
      return s.toString();
   }

   public String getName() {
      return name;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String c) {
      content = c;
   }

   public String getAttribute(String key) {
      if (attributes.containsKey(key)) {
         return attributes.get(key);
      } else {
         return null;
      }
   }

   /**
    *
    * @param key the Attribute to check for
    * @return true if this attribute already exists.
    */
   public boolean hasAttribute(String key) {
      return attributes.containsKey(key);
   }

   public boolean equals(XHTMLElement e) {
      if (toString().equals(e.toString())) {
         return true;
      } else {
         return false;
      }
   }

   @Override
   public XHTMLElement clone() {
      XHTMLElement e = new XHTMLElement(this.name);
      Iterator<String> i = this.attributes.keySet().iterator();
      while (i.hasNext()) {
         String s = i.next();
         e.setAttribute(s, this.getAttribute(s));
      }
      e.setContent(this.getContent());
      return e;
   }

   public boolean isSelfClosing() {
      return selfclosing;
   }
}
