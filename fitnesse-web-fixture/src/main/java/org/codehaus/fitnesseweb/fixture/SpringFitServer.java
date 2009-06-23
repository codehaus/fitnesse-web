package org.codehaus.fitnesseweb.fixture;

import fit.FitServer;
import fit.FixtureLoader;
import org.springframework.context.support.StaticApplicationContext;

public class SpringFitServer extends FitServer{
    @Override
    public void run(String[] argv) throws Exception {
        FixtureLoader.setInstance(new SpringFixtureLoader(new StaticApplicationContext()));
        super.run(argv);
    }
}
