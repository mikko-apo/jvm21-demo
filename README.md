# jvm21-demos
Demos for showcasing JVM 21 features

# Getting things working

* Download graalvm-community-jdk20
  * https://github.com/graalvm/homebrew-tap `brew install --cask graalvm/tap/graalvm-community-jdk20`
  * Helidon Nima uses JDK20 (4.9.2023)
* Use that as the JDK in Idea

# Native image compilation
* To create native image executable files, use command line: `mvn exec:exec`
  * set JAVA_HOME and PATH manually to terminal window
  
         JDK_PATH=/Library/Java/JavaVirtualMachines/graalvm-community-openjdk-20.0.2+9.1/Contents/Home
         export JAVA_HOME=${JDK_PATH}
         export PATH=${JDK_PATH}/bin:$PATH
* When running the create app, for class not found errors see
  * https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/
  * https://www.graalvm.org/latest/reference-manual/native-image/metadata/
