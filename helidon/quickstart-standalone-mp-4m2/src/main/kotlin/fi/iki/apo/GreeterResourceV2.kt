package fi.iki.apo

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response

@Path("/v2/greet")
@RequestScoped
open class GreetResourceV2 {
    /**
     * Sleep x seconds
     *
     * @param sleepSeconds JSON containing the new greeting
     * @return [Response]
     */
    @Path("{message}")
    @GET
    open fun greet(@PathParam("message") message: String): String {
        return "Hello $message!"
    }
}

