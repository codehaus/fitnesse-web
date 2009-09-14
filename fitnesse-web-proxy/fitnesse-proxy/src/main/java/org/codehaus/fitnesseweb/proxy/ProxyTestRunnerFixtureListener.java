package org.codehaus.fitnesseweb.proxy;

import fit.Counts;
import fit.FitServer;
import fit.FixtureListener;
import fit.Parse;
import fitnesse.responders.run.TestSummary;
import fitnesse.runner.PageResult;

/**
 * Copied from TestRunnerFixtureListener
 */
public class ProxyTestRunnerFixtureListener implements FixtureListener {
   public Counts counts = new Counts();
   private boolean atStartOfResult = true;
   private PageResult currentPageResult;
   private ProxyTestRunner runner;

   public ProxyTestRunnerFixtureListener(ProxyTestRunner runner) {
      this.runner = runner;
   }

    public void tableFinished(Parse table) {
      try {
         String data = new String(FitServer.readTable(table), "UTF-8");
         if (atStartOfResult) {
            int indexOfFirstLineBreak = data.indexOf("\n");
            String pageTitle = data.substring(0, indexOfFirstLineBreak);
            data = data.substring(indexOfFirstLineBreak + 1);
            currentPageResult = new PageResult(pageTitle);
            atStartOfResult = false;
         }
         currentPageResult.append(data);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void tablesFinished(Counts count) {
      try {
         currentPageResult.setTestSummary(new TestSummary(count.right, count.wrong, count.ignores, count.exceptions));
         runner.acceptResults(currentPageResult);
         atStartOfResult = true;
         counts.tally(count);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}
