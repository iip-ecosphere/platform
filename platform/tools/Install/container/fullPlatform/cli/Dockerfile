FROM openjdk:16-alpine3.13
COPY plJars/ plJars
CMD java -cp "plJars/*" --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true "de.iip_ecosphere.platform.platform.Cli" $*

