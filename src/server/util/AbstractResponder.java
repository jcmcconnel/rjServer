package server.util;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.io.StringReader;

public abstract class AbstractResponder
{
   // Static 

   protected static String getDefaultErrorBody(String errMsg){
      return "<!DOCTYPE html>\n"+
             "<html>\n"+
                   "  <head>\n"+
                   "  </head>\n"+
                   "  <body>\n"+
                   "    <p>"+"There has been an error: "+errMsg+"</p>\n"+
                   "  </body>\n"+
                   "</html>\n";
   }

   // Instance 

   /**
    * The pathname where this responder starts handling requests
    **/
   protected String endPoint;

   /**
    * The directory where resources for this responder are housed.
    **/
   protected String root;

   /**
    * ??? The additional path components after the endpoint?
    **/
   protected String path;

   private HashMap<String, String> response;

   private HashMap<String, String> errorResponse;

   protected HashMap<String, String> request;

   protected ArrayList<String> currentParameters;
   protected String currentHashId;

   private String redirect;

   /**
    * Creates a new responder
    *
    * @param r The application root directory, where this responder should look for resources.
    * @param ep The application endpoint.  This is the part of the url where this responder will start handling requests.
    **/
   public AbstractResponder(String r, String ep)
   {
      root = r;
      endPoint = ep;
      redirect = null;
   }

   public boolean equals(String ep){
      return endPoint.equals(ep);
   }
   public String getEndPoint(){
      return this.endPoint;
   }

   public HashMap<String, String> getResponse(HashMap<String, String> request){
      if(redirect != null){
         HashMap<String, String> response = new HashMap<String, String>();

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

   private HashMap<String, String> GETResponse(HashMap<String, String> request) throws ResponderException {
      HashMap<String, String> response = new HashMap<String, String>();
      String responseBody = "";
      String target = request.get("request-line").split(" ")[1];

      this.path = parsePath(target);

      response.put("status-line","HTTP/1.0 200 OK");
      response.put("Content-Type", "text/html; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getBody(target);
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
      return response;
   }

   private HashMap<String, String> POSTResponse(HashMap<String, String> request) throws ResponderException {
      HashMap<String, String> response = new HashMap<String, String>();
      String responseBody = "";
      String target = request.get("request-line").split(" ")[1];

      this.path = parsePath(target);

      response.put("status-line","HTTP/1.0 200 OK");
      response.put("Content-Type", "text/html; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getBody(target);
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
      return response;
   }

   private HashMap<String, String> PUTResponse(HashMap<String, String> request) throws ResponderException {
      return ERRORResponse(request, "PUT Requests not supported");
   }

   protected HashMap<String, String> ERRORResponse(HashMap<String, String> request, String errMsg){
      HashMap<String, String> response = new HashMap<String, String>();
      String responseBody = "";
      String target = request.get("request-line").split(" ")[1];

      this.path = parsePath(target);

      response.put("status-line","HTTP/1.0 404 NOT FOUND");
      response.put("Content-Type", "text/html; charset=utf-8");

      responseBody = getErrorBody(target, errMsg);
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
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
         if(!temp.isEmpty()) pathComponents.add(temp);
      }
      catch(IOException e) {
         System.out.println(e);
         return "";
      }
      return String.join("/", pathComponents);
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

   protected ArrayList<String> parseParameters(StringReader in) throws IOException {
      ArrayList<String> parameters = new ArrayList<String>();
      String temp = "";
      while(in.ready()){
         int c = in.read();
         if(c == -1) {
            parameters.add(temp);
            temp = "";
            break;
         }
         if(c == '&' || c == ';'){
            parameters.add(temp);
            temp = "";
         } else temp = temp + (char)c;
      }
      if(!temp.isEmpty()) parameters.add(temp);
      return parameters;
   }
   
   protected abstract String getBody(String target) throws ResponderException;

   /**
    * Override this method for a custom message
    **/
   protected String getErrorBody(String target, String errMsg){
      return server.util.AbstractResponder.getDefaultErrorBody(errMsg);
   }

}

