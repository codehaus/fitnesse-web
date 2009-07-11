package org.codehaus.fitnesseweb.example.standalone;

import org.codehaus.fitnesseweb.fixture.SeleniumControlFixture;

public class MyControlFixture extends SeleniumControlFixture {
    public MyControlFixture() {
        applicationRoot = "http://localhost:8025/MyProject.SimpleSuite.FirstTest";
    }
}
