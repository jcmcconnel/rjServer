package djava;

public class Responder
{
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

      responseBody = getResponseFor((String)responders.get(responders.indexOf(request.get("request-line").split(" ")[1])));
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
   }

   private HashMap<String, String> POSTResponse(HashMap<String, String> request){
      HashMap<String, String> response = new HashMap<String, String>();
      String responseBody = "";

      response.put("status-line","HTTP/1.0 200 OK");
      response.put("Content-Type", "text/html; charset=utf-8");
      response.put("Content-Length", "0");

      responseBody = getResponseFor((String)responders.get(responders.indexOf(request.get("request-line").split(" ")[1])));
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
   }

   private HashMap<String, String> PUTResponse(HashMap<String, String> request){
      return ERRORREsponse(request);
   }

   private HashMap<String, String> ERRORResponse(HashMap<String, String> request){
      response.put("status-line","HTTP/1.0 404 NOT FOUND");
      response.put("Content-Type", "text/html; charset=utf-8");

      responseBody = getResponseFor("error");
      response.put("Content-Length", String.valueOf(responseBody.length()));
      response.put("body", responseBody);
   }
   
   private String getBody(String target){
      return "<!DOCTYPE html>\n"+
         "<html>\n"+
         "  <head>\n"+
         "  </head>\n"+
         "  <body>\n"+
         "    <p>"+"Index"+"</p>\n"+
         "  </body>\n"+
         "</html>\n";
   }
   
   
}

