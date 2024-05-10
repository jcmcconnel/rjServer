package responder;

import java.io.File;

public class BasicResponder extends server.util.AbstractResponder
{

   public BasicResponder(File root, String ep, String[] exts)
   {
      super(root, ep, exts);
   }

   /**
    *
    **/
   protected byte[] getBody(String target) throws server.util.ResponderException
   {
      System.out.println("Basic responder top: "+target);
      return super.getResource(target);
   }
   
}

