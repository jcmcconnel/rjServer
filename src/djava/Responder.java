package djava;

public abstract class Responder
{
   // Static 
   private static String defaultErrorBody = "<!DOCTYPE html>\n"+
              "<html>\n"+
              "  <head>\n"+
              "  </head>\n"+
              "  <body>\n"+
              "    <p>"+"There has been an error"+"</p>\n"+
              "  </body>\n"+
              "</html>\n";

   private static String getDefaultErrorBody(){
      return defaultErrorBody;
   }

   public static setDefaultErrorBody(String body){
      this.defaultErrorBody = body;
   }

   // Instance 

   private String endPoint;
   private HashMap<String, String> response;

   private HashMap<String, String> errorResponse;

   public Responder(String endPoint)
   {
      endPoint = endPoint;
   }

   public HashMap<String, String> getResponse(HashMap<String, String> request){
      if(request.get("request-line").startsWith("GET")){
         return GETResponse(request);
      } else if(request.get("request-line").startsWith("POST")) {
         return POSTResponse(request);
      } else if(request.get("request-line").startsWith("PUT")) {
         return PUTResponse(request);
      }else {
         return ERRORResponse(request);
      }
   }

   private HashMap<String, String> GETResponse(HashMap<String, String> request){
      HashMap<String, String> response = new HashMap<String, String>();
      String responseBody = "";

      response.put("status-line","HTTP/1.0 200 OK");
      response.put("Content-Type", "text/html; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getBody(request.get("request-line").split(" ")[1]);
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
   }

   private HashMap<String, String> POSTResponse(HashMap<String, String> request){
      HashMap<String, String> response = new HashMap<String, String>();
      String responseBody = "";

      response.put("status-line","HTTP/1.0 200 OK");
      response.put("Content-Type", "text/html; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getBody(request.get("request-line").split(" ")[1]);
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
   }

   private HashMap<String, String> PUTResponse(HashMap<String, String> request){
      return ERRORREsponse(request);
   }

   protected HashMap<String, String> ERRORResponse(HashMap<String, String> request){
      response.put("status-line","HTTP/1.0 404 NOT FOUND");
      response.put("Content-Type", "text/html; charset=utf-8");

      responseBody = getErrorBody(request.get("request-line").split(" ")[1]);
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
   }
   
   protected abstract String getBody(String target);

   /**
    * Override this method for a custom message
    **/
   protected String getErrorBody(String target){
      djava.Responder.getDefaultErrorBody();
   }

}

