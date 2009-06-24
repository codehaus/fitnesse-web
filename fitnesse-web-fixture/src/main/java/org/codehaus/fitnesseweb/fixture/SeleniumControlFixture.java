package org.codehaus.fitnesseweb.fixture;

import com.thoughtworks.selenium.DefaultSelenium;
import fitlibrary.DoFixture;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SeleniumControlFixture extends DoFixture implements ApplicationContextAware {
    private enum State {
        STARTED, STOPPED
    }

    private static final String SELENIUM_BEAN_NAME = "selenium";
    private String browserName = "*iexploreproxy";
    private ApplicationContext applicationContext;
    protected String applicationRoot = "";
    private State state = State.STOPPED;

    public void theBrowserIs(String browserName) {
        this.browserName = browserName;
    }

    public void applicationLocationIs(String applicationRoot) {
        this.applicationRoot = applicationRoot;
    }

    public DefaultSelenium getSelenium() {
        if (applicationContext.containsBean(SELENIUM_BEAN_NAME)) {
            return (DefaultSelenium) applicationContext.getBean(SELENIUM_BEAN_NAME);
        }
        return null;
    }

    public void startSelenium() {
        DefaultSelenium selenium = getSelenium();
        if (selenium == null) {
            selenium = new DefaultSelenium("localhost", 4444, browserName, applicationRoot);
            ((ConfigurableApplicationContext) applicationContext).getBeanFactory().
                    registerSingleton(SELENIUM_BEAN_NAME, selenium);
        }
        if (state == State.STOPPED) {
            selenium.start();
            state = State.STARTED;
        }
    }

    public void stopSelenium() {
        DefaultSelenium selenium = getSelenium();
        if (selenium != null) {
            selenium.stop();
            state = State.STOPPED;
        }
    }

    // Start a Selenium session, probably call this
    // in SetUp
    public void startApplication() {
        startSelenium();
    }

    // Shut down the Selenium session and free the
    // resources.  Either do this in TearDown or at
    // the end of every, single test.
    public void closeApplication() {
        stopSelenium();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

