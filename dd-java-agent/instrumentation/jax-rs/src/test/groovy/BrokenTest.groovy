import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/test")
class BrokenTest {
  @POST
  @Path("/hello/{name}")
  public String addBook(@PathParam("name") final String name) {
    return "Hello " + name + "!"
  }
}
