package org.codehaus.fitnesseweb.executor;

import fit.FixtureListener;
import fit.Parse;
import fit.Counts;

import java.util.List;
import java.util.ArrayList;

class TeamCityFixtureListener implements FixtureListener {
    private int testNumber = 0;

    public void tableFinished(Parse table) {
    }

    public void tablesFinished(Counts count) {
        testNumber++;
        String testName = "Fitnesse test nr " + testNumber;
        System.out.println("##teamcity[testStarted name='" + testName + "']");
        if (count.wrong > 0 || count.exceptions > 0) {
            System.out.println("##teamcity[testFailed name='" + testName + "' message='" + count.toString() + "']");
        }
        System.out.println("##teamcity[testFinished name='" + testName + "']");
    }
}