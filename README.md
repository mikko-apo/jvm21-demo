# jvm21-demos
Demos for showcasing JVM 21, Kotlin, GraalVM features

# Notes
Loom
* HttpClient has race conditions when 200 requests fail. This affects jvm21-demo/src/main/java/fi/iki/apo/HttpGetBurst.java
  * HttpClientImpl.awaitTermination gets stuck waiting on HttpClientImpl$SelectorManager. See  [JDK-8316580](https://bugs.java.com/bugdatabase/view_bug?bug_id=JDK-8316580)

# General setup instructions for Intellij Idea

* Download graalvm-community-jdk20 (for testing GraalVM Native compilation with nima-quickstart-standalone-graalvm20)
  * https://github.com/graalvm/homebrew-tap `brew install --cask graalvm/tap/graalvm-community-jdk20`
* Download Intellij Idea CE and start it
* Intellij Idea: Get from VCS > git@github.com:mikko-apo/jvm21-demo.git > Clone
* Maven build scripts found > Load
* File > Project Structure > SDK > Add SDK > Download JDK > OpenJDK 21
* Download JDK-21
* Ensure that File Project Structure > Modules > * > Dependencies > Module SDK is 
  * JDK 21 for jvm21-demo and helidon-standalone-quickstart-mp
  * GraalVM20 for helidon/quickstart-standalone-graalvm20 (add GraalVM20 from file system)
* Running files in jvm21-demo
  * Project > navigate to jvm21-demo/src/main/java/fi/iki/apo/HttpGetBurst.java
  * Right click > Run ‘HttpGetBurst.main()’
  * If you get -> warning about “--enable-preview”
     * Find the application config from top toolbar, click arrow down on the right side
     * Edit configurations > Modify options > Add VM Options > Put --enable-preview to “VM options” input
* Running quickstart-standalone-mp-4m2
  * Triangle down in top right group of toolbar > Edit Configurations... > + > Add New Configuration > Application
    * Main class: io.helidon.microprofile.cdi.Main

# Helidon 4.0.0-M2

* Directory [!quickstart-standalone-mp-4m2](helidon%2Fquickstart-standalone-mp-4m2)
* Deps: JDK21
  *  GraalVM is buggy
* OpenApi, metrics, health

## Open api generation

```
brew install openapi-generator
curl http://localhost:8080/openapi > greet-openapi.yaml
openapi-generator generate -i greet-openapi.yaml -g typescript-fetch -o greet-ts-fetch-client/
npx openapi-zod-client greet-openapi.yaml -o ../../zodios-client/greet-zodios-client.ts
```

to cache the openapi use

```
mvn process-classes 
```

## Metrics

curl http://localhost:8080/metrics


# Helidon 4.0.0-M1 for Native image compilation

* Directory: [nima-quickstart-standalone-graalvm20](helidon%2Fquickstart-standalone-graalvm20) 
* Deps: GraalVM JDK20
    * Download JDK20 somehow
* Does not include CDI, fast startup
* Working graalvm compilation

## Running in Idea:

* Open StandaloneQuickstartMain, right click Run >  JDK20

## Compiling GraalVM Native image:

* To create native image executable files set JAVA_HOME and PATH manually to terminal window

       JDK_PATH=/Library/Java/JavaVirtualMachines/graalvm-community-openjdk-20.0.2+9.1/Contents/Home
       export JAVA_HOME=${JDK_PATH}
       export PATH=${JDK_PATH}/bin:$PATH
* run `mvn clean compile package -Pnative-image`
* Exe is available in ./target/helidon-nima-examples-quickstart-standalone

### note:
* Native image packaging does not work out of the box with Helidon 4.0.0-M1 quickstart-standalone
  * Delete `quickstart-standalone/src/main/resources/META-INF/native-image`
  * Uncomment line in quickstart-standalone/pom.xml exec-maven-plugin
  * Use command line: `mvn exec:exec` to generate new files to META-INF/native-image
  * Add item to reflect-config.json: `{"name": "io.helidon.nima.webserver.LoomServer"}`
  * Run the package command from above
* When creating the native image, for class not found errors see
  * https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/
  * https://www.graalvm.org/latest/reference-manual/native-image/metadata/
