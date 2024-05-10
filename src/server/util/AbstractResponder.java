package server.util;

import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.io.StringReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.nio.file.Files;

public abstract class AbstractResponder
{
   /// ------------ Static ------------------///
   // Instance members start below line 131
   private static class ResponderTemplate {
      public String className;
      public File root;
      public String endPoint;

      public String[] fileExtensions; //The filetypes this responder will handle.

      public ResponderTemplate(String c, File f, String e, String[] exts){
         className = c;
         root = f;
         endPoint = e;
         fileExtensions = exts;
      }
   }

   private static HashMap<String, ResponderTemplate> templates = new HashMap<String, ResponderTemplate>();
   private static HashMap<String, server.util.AbstractResponder> responders = new HashMap<String, AbstractResponder>();
   private static AbstractResponder errorResponder = new AbstractResponder(new File("."), "error", new String[]{"html", "htm"}) {
      protected byte[] getBody(String target){
         return getDefaultErrorBody(this.request).getBytes();
      }
   };
   private static URLClassLoader responderLoader;

   private static AbstractResponder createResponder(String className, File rootDir, String endPoint, String[] exts) throws ReflectiveOperationException, FileNotFoundException {
      if(className.equals("error")){
         return new AbstractResponder(new File("."), "/error", new String[]{"html", "htm"}) {
            protected byte[] getBody(String target){
               return getDefaultErrorBody(this.request).getBytes();
            }
         };
      } else {
         System.out.println("adding responder");
         if(rootDir.isDirectory()) {
            Class rclass = responderLoader.loadClass(className);
            Constructor rConstructor = rclass.getConstructor(File.class, String.class, String[].class);
            server.util.AbstractResponder r;
            return (server.util.AbstractResponder)rConstructor.newInstance(rootDir, endPoint, exts);
         } else throw new FileNotFoundException("Application root is not a directory.");
      }
   }

   public static void createResponderTemplate(String className, String rootDir, String endPoint, String[] exts){
      File root = new File(rootDir);
      System.out.println("adding responder");
      if(root.isDirectory()) {
         templates.put(endPoint, new ResponderTemplate(className, root, endPoint, exts));
      } else System.out.println("Application root is not a directory.");
   }

   public static void removeResponderTemplate(String endPoint){
      templates.remove(endPoint);
   }

   private static void removeResponder(String endPoint){
      responders.remove(endPoint);
   }

   public static void addClassLoader(ClassLoader cl){
      responderLoader = (URLClassLoader)cl;
   }
   
   public static AbstractResponder getResponder(HashMap<String, String> request) throws ReflectiveOperationException, FileNotFoundException {
      String referer = removeHostName(request.get("referer"));
      String target = request.get("request-line").split(" ")[1];
      System.out.println();
      System.out.println("Getting responder: "+request.get("request-line"));
      System.out.println("referer: "+referer);
      System.out.println("target: "+target);
      String endPoint = null;
      if(referer == null || referer.equals("")) {
         System.out.println("Empty Referer");
         if(target.split("/").length > 0) endPoint = "/"+target.split("/")[1];
         else endPoint = target;
      } else {
         //Problem: Where in the referrer is the endpoint when the endpoint may dangle off the address?  
         //Ex: djava/recipes/style/default.css 
         // End point is /recipes, not /djava and not /style
         if(referer.split("/").length > 1) {
            String[] pathComp = referer.split("/");
            System.out.println("pathComp count: "+pathComp.length);
            for(int i=0; i < pathComp.length; i++) {
               System.out.println("Looking for: /"+pathComp[i]);
               if(!pathComp[i].equals("") && templates.containsKey("/"+pathComp[i])) {
                  endPoint = "/"+pathComp[i];
                  break;
               }
            }
            System.out.println("Determined EP:"+endPoint);
            if(endPoint == null) return createResponder("error", new File("."), "/error", new String[]{"html"});
         } else endPoint = referer;
      }
      if(templates.containsKey(endPoint)) {
         ResponderTemplate t = templates.get(endPoint);
         if(target.contains(endPoint)) {
            System.out.println("target contains endpoint");
            return createResponder(t.className, t.root, t.endPoint, t.fileExtensions);
         } else {
            System.out.println("Redirecting to: "+endPoint+target);
            return createResponder(t.className, t.root, t.endPoint, t.fileExtensions).getRedirect(endPoint+target);
         }
      } else return createResponder("error", new File("."), "/error", new String[]{"html"});
   }

   public static HashMap<String, Object> getErrorResponse(HashMap<String, String> request){
      HashMap<String, Object> response = new HashMap<String, Object>();
      byte[] responseBody = null;
      String target = request.get("request-line").split(" ")[1];

      response.put("status-line","HTTP/1.0 404 NOT FOUND");
      response.put("Content-Type", "text/html; charset=utf-8");

      responseBody = getDefaultErrorBody(request).getBytes();
      response.put("Content-Length", String.valueOf(responseBody.length));
      response.put("body", new String(responseBody));
      return response;
   }

   private static String removeHostName(String fqdn) {
      if(fqdn == null) return "";
      String temp = fqdn.substring(fqdn.indexOf("//")+2);
      temp = temp.substring(temp.indexOf('/'));
      return temp;
   }


   protected static String getDefaultErrorBody(HashMap<String, String> request){
      String errorMsg = "<p>There has been an error</p>"+
                        "<p>Could not retrieve: "+request.get("request-line").split(" ")[1]+"</p>";
      StringBuilder s = new StringBuilder();
      s.append("<!DOCTYPE html>\n");
      s.append("<html>\n");
      s.append("  <head>\n");
      s.append("  </head>\n");
      s.append("  <body>\n");
      s.append(errorMsg);
      s.append("  </body>\n");
      s.append("</html>\n");
      return s.toString();
   }

   /// ------------ Instance ------------------///

   /**
    * The pathname where this responder starts handling requests
    **/
   protected String endPoint;

   /**
    * The directory where resources for this responder are housed.
    **/
   protected File root;

   /**
    * The additional path components after the endpoint
    **/
   protected String path;

   protected String[] fileExtensions;

   protected String fileType;

   private HashMap<String, Object> response;

   private HashMap<String, Object> errorResponse;

   protected HashMap<String, String> request;

   protected HashMap<String, String> currentParameters;
   protected String currentHashId;

   private String redirect;

   /**
    * Creates a new responder
    *
    * @param r The application root directory, where this responder should look for resources.
    * @param ep The application endpoint.  This is the part of the url where this responder will start handling requests.
    **/
   public AbstractResponder(File r, String ep, String[] exts)
   {
      root = r;
      endPoint = ep;
      redirect = null;
      fileExtensions = exts;
      fileType = null;
   }

   public boolean equals(String ep){
      return endPoint.equals(ep);
   }
   public String getEndPoint(){
      return this.endPoint;
   }

   public HashMap<String, Object> getResponse(HashMap<String, String> request){
      if(redirect != null){
         HashMap<String, Object> response = new HashMap<String, Object>();

         response.put("status-line","HTTP/1.0 301 Moved Permanently");
         response.put("Location", redirect);

         redirect = null;

         return response;
      }
      this.request = request;
      try{
         if(request.get("request-line").startsWith("GET")){
            return GETResponse(request);
         } else if(request.get("request-line").startsWith("POST")) {
            return POSTResponse(request);
         } else if(request.get("request-line").startsWith("PUT")) {
            return PUTResponse(request);
         } else return ERRORResponse(request, "Request type: "+request.get("request-line").split(" ")[0]+" not supported");
      }
      catch(ResponderException e){
         return ERRORResponse(request, e.toString());
      }
   }

   private HashMap<String, Object> GETResponse(HashMap<String, String> request) throws ResponderException {
      HashMap<String, Object> response = new HashMap<String, Object>();
      byte[] responseBody = null;
      String target = request.get("request-line").split(" ")[1];

      this.path = parsePath(target);
      int extIndex = this.path.lastIndexOf(".");
      String ext = "html";
      if(extIndex > 0) ext = this.path.substring(extIndex+1);

      response.put("status-line","HTTP/1.0 200 OK");

      if(fileType != null && fileType.equals("jpg")) response.put("Content-Type", "image/"+ext+";");
      else response.put("Content-Type", "text/"+ext+"; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getBody(target);
      response.put("Content-Length", String.valueOf(responseBody.length));
      response.put("body", responseBody);
      return response;
   }

   private HashMap<String, Object> POSTResponse(HashMap<String, String> request) throws ResponderException {
      HashMap<String, Object> response = new HashMap<String, Object>();
      byte[] responseBody = null;
      String target = request.get("request-line").split(" ")[1];

      this.path = parsePath(target);

      response.put("status-line","HTTP/1.0 200 OK");
      response.put("Content-Type", "text/html; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getBody(target);
      response.put("Content-Length", String.valueOf(responseBody.length));
      response.put("body", new String(responseBody));
      return response;
   }

   private HashMap<String, Object> PUTResponse(HashMap<String, String> request) throws ResponderException {
      return ERRORResponse(request, "PUT Requests not supported");
   }

   protected HashMap<String, Object> ERRORResponse(HashMap<String, String> request, String errMsg){
      HashMap<String, Object> response = new HashMap<String, Object>();
      byte[] responseBody = null;
      String target = request.get("request-line").split(" ")[1];

      this.path = parsePath(target);

      response.put("status-line","HTTP/1.0 404 NOT FOUND");
      response.put("Content-Type", "text/html; charset=utf-8");

      responseBody = getErrorBody().getBytes();
      response.put("Content-Length", String.valueOf(responseBody.length));
      response.put("body", new String(responseBody));
      return response;
   }

    /**
     * Redirects relative paths to the correct address
     **/
    public AbstractResponder getRedirect(String s) {
      redirect = s;
      return this;
    }

   protected String parsePath(String target){
      ArrayList<String> pathComponents = new ArrayList<String>();

      StringReader in = new StringReader(target);

      try{
         if(target.startsWith(this.getEndPoint())) in.skip(this.getEndPoint().length());

         String temp = "";
         while(in.ready()) {
            int c = in.read();
            if(c == -1) {
               pathComponents.add(temp);
               temp = "";
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
         if(!temp.isEmpty()){
            pathComponents.add(temp);
         }
      }
      catch(IOException e) {
         System.out.println(e);
         return "";
      }
      String output = String.join("/", pathComponents);
      int pathExtIndex = output.lastIndexOf(".");
      if(pathExtIndex > 0) {
         String ext = output.substring(pathExtIndex+1);
         for(int i=0; i<fileExtensions.length; i++){
            if(ext.equals(fileExtensions[i])){
               fileType = output.substring(pathExtIndex+1);
               break;
            }
         }
      }

      return output;
   }

   protected String parseIdentifier(StringReader in) throws IOException {
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

   protected HashMap<String, String> parseParameters(StringReader in) throws IOException {
      HashMap<String, String> parameters = new HashMap<String, String>();
      String temp = "";
      while(in.ready()){
         int c = in.read();
         if(c == -1) {
            if(temp.contains("=")) parameters.put(temp.split("=")[0], temp.split("=")[1]);
            temp = "";
            break;
         }
         if(c == '&' || c == ';'){
            if(temp.contains("=")) parameters.put(temp.split("=")[0], temp.split("=")[1]);
            temp = "";
         } else temp = temp + (char)c;
      }
      if(!temp.isEmpty() && temp.contains("=")) parameters.put(temp.split("=")[0], temp.split("=")[1]);
      return parameters;
   }
   
   /**
    * Most responders will have some resource that they want to collect and utilize this handles the getting and input checking part.
    * @param target The request-line path part
    * @return The bytes of the resource
    *
    * */
   protected byte[] getResource(String target) throws ResponderException {
      File resource;
      if(fileType != null) {
         resource = new File(this.root+this.path);
      } else {
         resource = new File(this.root+"/index."+this.fileExtensions[1]);
         if(this.path == null || this.path.equals("") || this.path.equals("/")){
            for(int i=0; i<this.fileExtensions.length; i++){
               resource = new File(this.root+"/index."+this.fileExtensions[i]);
               if(resource.exists()) break;
            }
         } else {
            resource = new File(this.root+this.path+"/index."+this.fileExtensions[1]);
            for(int i=0; i<this.fileExtensions.length; i++){
               resource = new File(this.root+this.path+"/index."+this.fileExtensions[i]);
               if(resource.exists()) break;
            }
         }
      }
      System.out.println("EP:"+this.getEndPoint());
      System.out.println("root:"+this.root);
      System.out.println("path:"+this.path);
      System.out.println("resource:"+resource.toString());

      try {
         if(resource.exists()){
            return Files.readAllBytes(resource.toPath());
         } else throw new server.util.ResponderException("Resource: "+this.path+" does not exist");
      }
      catch(IOException e) {
         System.out.println(e);
         throw new server.util.ResponderException("IOException in getResource: "+e.toString());
      }
   }

   protected abstract byte[] getBody(String target) throws ResponderException;

   /**
    * Override this method for a custom message
    **/
   protected String getErrorBody(){
      return server.util.AbstractResponder.getDefaultErrorBody(request);
   }

}

