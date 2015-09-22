#/bin/sh

ARGS="build-monto build.pluto.buildmonto.ServicesBaseJavaBuilder.factory build.pluto.buildmonto.ServicesBaseJavaInput $@"

mvn compile exec:java -Dexec.args="$ARGS"
