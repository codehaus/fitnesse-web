package org.codehaus.fitnesseweb.executor;

import fit.FixtureListener;
import fit.Parse;
import fit.Counts;

import java.util.List;
import java.util.ArrayList;

class CompositeFixtureListener implements FixtureListener {
    List<FixtureListener> fixtureListeners = new ArrayList<FixtureListener>();


    public void tableFinished(Parse table) {
        for (FixtureListener listener : fixtureListeners) {
            listener.tableFinished(table);
        }
    }

    public void tablesFinished(Counts count) {
        for (FixtureListener listener : fixtureListeners) {
            listener.tablesFinished(count);
        }
    }

    public void addFixtureListener(FixtureListener fixtureListener) {
        this.fixtureListeners.add(fixtureListener);
    }
}
