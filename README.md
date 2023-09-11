# jvm21-demos
Demos for showcasing JVM 21 features

# Notes on JDK 21
Loom
* HttpClient has race conditions when 200 requests fail. HttpClientImpl.awaitTermination gets stuck waiting on HttpClientImpl$SelectorManager

# Helidon 4.0.0-M2

* Directory [quickstart-standalone-mp-4m2](helidon%2Fquickstart-standalone-mp-4m2)
* Deps: JDK21, there is no graalvm21
* OpenApi, metrics, health

## Open api generation

```
brew install openapi-generator
curl http://localhost:8080/openapi > greet-openapi.yaml
openapi-generator generate -i greet-openapi.yaml -g typescript-fetch -o greet-ts-fetch-client/
npx openapi-zod-client greet-openapi.yaml -o ../../zodios-client/greet-zodios-client.ts
```

# Helidon 4.0.0-M1

* Directory: [quickstart-standalone](helidon%2Fquickstart-standalone) 
* Deps: JDK20, GraalVM20
    * Download JDK20 somehow
* Does not include CDI, fast startup
* Working graalvm compilation

## Native image compilation
* Download graalvm-community-jdk20
  * https://github.com/graalvm/homebrew-tap `brew install --cask graalvm/tap/graalvm-community-jdk20`
* To create native image executable files set JAVA_HOME and PATH manually to terminal window

       JDK_PATH=/Library/Java/JavaVirtualMachines/graalvm-community-openjdk-20.0.2+9.1/Contents/Home
       export JAVA_HOME=${JDK_PATH}
       export PATH=${JDK_PATH}/bin:$PATH
* run `mvn clean compile package -Pnative-image`

note:
* Native image packaging does not work out of the box with Helidon 4.0.0-M1 quickstart-standalone
  * Delete `quickstart-standalone/src/main/resources/META-INF/native-image`
  * Uncomment line in quickstart-standalone/pom.xml exec-maven-plugin
  * Use command line: `mvn exec:exec` to generate new files to META-INF/native-image
  * Add item to reflect-config.json: `{"name": "io.helidon.nima.webserver.LoomServer"}`
  * Run the package command from above
* When creating the native image, for class not found errors see
  * https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/
  * https://www.graalvm.org/latest/reference-manual/native-image/metadata/
