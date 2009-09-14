package org.codehaus.fitnesseweb.executor;

import fit.FixtureListener;
import fit.Parse;
import fit.Counts;

import java.util.List;
import java.util.ArrayList;

class TeamCityFixtureListener implements FixtureListener {
    private int testCounter = 0;
    private String testName;
    private boolean testInProgreess = false;
    private long startTime;

    public void tableFinished(Parse table) {
        if (!testInProgreess){
            testInProgreess = true;
            testCounter++;
            startTime = System.currentTimeMillis();
            int indexOfEOL = table.leader.indexOf('\n');

            if (indexOfEOL > 0){
                testName = table.leader.substring(0, indexOfEOL);
            }else {
                testName = "Fitnesse test nr "+ testCounter;
            }
            System.out.println("##teamcity[testStarted name='" + testName + "']");
        }
    }

    public void tablesFinished(Counts count) {
        testInProgreess = false;
        if (count.wrong > 0 || count.exceptions > 0) {
            System.out.println("##teamcity[testFailed name='" + testName + "' message='" + count.toString() + "']");
        }
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("##teamcity[testFinished name='" + testName + "' duration='"+duration+"']");
    }
}