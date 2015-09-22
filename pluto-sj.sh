#/bin/sh

ARGS="build-monto build.pluto.buildmonto.ServicesJavaBuilder.factory build.pluto.buildmonto.ServicesJavaInput $@"

mvn compile exec:java -Dexec.args="$ARGS"
