package org.codehaus.fitnesseweb.fixture;

import com.thoughtworks.selenium.DefaultSelenium;
import fit.Fixture;
import fit.Parse;
import org.springframework.beans.factory.annotation.Required;

public class GoToPageFixture extends Fixture {
    private DefaultSelenium selenium;

    @Override
    public void doTable(Parse table) {
        super.doTable(table);
        String location = getArgs()[0];
        boolean timeouted = false;
        selenium.open(location);
        try {
            selenium.waitForPageToLoad("20000");
        } catch (Exception e) {
            timeouted = true;
        } finally {
        }
        this.right(table.at(0, 0, 1));
    }

    @Required
    public void setSelenium(DefaultSelenium selenium) {
        this.selenium = selenium;
    }
}