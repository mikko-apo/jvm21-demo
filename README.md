# jvm21-demos
Demos for showcasing JVM 21 features

# Getting things working

* Download graalvm-community-jdk20
  * https://github.com/graalvm/homebrew-tap `brew install --cask graalvm/tap/graalvm-community-jdk20`
  * Helidon Nima uses JDK20 (4.9.2023)
* Use that as the JDK in Idea

# Native image compilation
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
