package org.codehaus.fitnesseweb.fixture;

import fit.Fixture;
import fit.FixtureLoader;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringFixtureLoader extends FixtureLoader {
    private ApplicationContext applicationContext;

    public SpringFixtureLoader(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Fixture disgraceThenLoad(String tableName) throws Throwable {
        Fixture fixture = super.disgraceThenLoad(tableName);
        applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(fixture,
                AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        if (fixture instanceof ApplicationContextAware) {
            ((ApplicationContextAware) fixture).setApplicationContext(applicationContext);
        }

        return fixture;
    }
}
