package org.codehaus.fitnesseweb.fixture;

import fit.FitServer;
import fit.FixtureLoader;
import org.springframework.context.support.StaticApplicationContext;

public class SpringFitServer {
    private SpringFitServer() {
    }

    public static void main(String argv[]) throws Exception {
        FixtureLoader.setInstance(new SpringFixtureLoader(new StaticApplicationContext()));
        FitServer fitServer = new FitServer();
        fitServer.run(argv);
        System.exit(fitServer.exitCode());
    }
}
