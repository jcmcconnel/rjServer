package djava;

public abstract class PageResponder
{
   public PagesResponder(String ep)
   {
      super(ep);
   }

   protected abstract String getBody(String target)
   {
      FileInputStream in = new FileInputStream("pages"+this.endPoint);
      String body = IOUtils.toString(in);
      return body+"\n";
   }
}

