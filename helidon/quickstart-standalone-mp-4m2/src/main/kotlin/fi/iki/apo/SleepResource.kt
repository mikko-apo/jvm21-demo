package fi.iki.apo;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.time.Duration;

@Path("/sleep")
@RequestScoped
public class SleepResource {

    /**
     * Sleep x seconds
     *
     * @param sleepSeconds JSON containing the new greeting
     * @return {@link Response}
     */
    @Path("{sleepSeconds}")
    @GET
    public String sleep(@PathParam("sleepSeconds") long sleepSeconds) {
        try {
            Thread.sleep(Duration.ofSeconds(sleepSeconds));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Slept " + sleepSeconds + " seconds!";
    }

}
