package org.codehaus.fitnesseweb.fixture;

import com.thoughtworks.selenium.DefaultSelenium;
import fit.Parse;
import fit.Fixture;
import org.springframework.beans.factory.annotation.Required;


public class IsOnPageFixture extends Fixture {
    private DefaultSelenium selenium;

    @Override
    public void doTable(Parse table) {
        super.doTable(table);
        String location = getArgs()[0];
        boolean timeouted = false;
        try {
            selenium.waitForPageToLoad("2000");
        } catch (Exception e) {
            timeouted = true;
        } finally {
            String currentLocation = selenium.getLocation();
            if (currentLocation.contains(location)) {
                this.right(table.at(0, 0, 1));
            } else {
                this.wrong(table.at(0, 0, 1), currentLocation);
            }
        }

    }

    @Required
    public void setSelenium(DefaultSelenium selenium) {
        this.selenium = selenium;
    }
}
