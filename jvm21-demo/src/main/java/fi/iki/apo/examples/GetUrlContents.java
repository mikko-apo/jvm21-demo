package fi.iki.apo.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class GetUrlContents {
    public String getUrlContents(String url)  {
        try(InputStream in  = URI.create(url).toURL().openStream()) {
            byte[] bytes = in.readAllBytes(); // Socket read() blocks OS Thread!!
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
