package build.pluto.buildmonto;

import java.io.File;
import java.util.Arrays;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.output.None;

public class Main {

	public static void main(String[] args) throws Throwable {
//		Log.log.setLoggingLevel(Log.DETAIL);
		
		ServicesJava.Input javaInput = new ServicesJava.Input(
      		  new File ("target/services-java"),
      		  new File ("target/services-java.jar"));
        
		ServicesJavascript.Input jsInput = new ServicesJavascript.Input(
    		  new File ("target/services-javascript"),
    		  new File ("target/services-javascript.jar"));
		
		BuildManagers.<None>buildAll(Arrays.asList(
				new BuildRequest<>(ServicesJava.factory, javaInput), 
				new BuildRequest<>(ServicesJavascript.factory, jsInput)));
	}

}
