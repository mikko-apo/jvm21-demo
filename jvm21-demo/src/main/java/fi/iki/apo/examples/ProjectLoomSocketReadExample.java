package fi.iki.apo.examples;

import java.io.InputStream;
import java.net.URI;

public class ProjectLoomSocketReadExample {

    public ProjectLoomSocketReadExample() {
        System.out.println(getUrlContents("pow"));
    }

    public String getUrlContents(String url) {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            byte[] bytes = in.readAllBytes(); // Socket read() blocks OS thread!!
            return new String(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
