package fi.iki.apo;

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import java.time.Duration

@Path("/sleep")
@RequestScoped
class SleepResource {
    /**
     * Sleep x seconds
     *
     * @param sleepSeconds JSON containing the new greeting
     * @return [Response]
     */
    @Path("{sleepSeconds}")
    @GET
    fun sleep(@PathParam("sleepSeconds") sleepSeconds: Long): String {
        Thread.sleep(Duration.ofSeconds(sleepSeconds))
        return "Slept $sleepSeconds seconds!"
    }
}

