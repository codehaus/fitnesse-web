package org.codehaus.fitnesseweb.proxy;

import fit.Counts;
import fitnesse.components.CommandLine;
import fitnesse.responders.run.TestSummary;
import fitnesse.runner.CachingResultFormatter;
import fitnesse.runner.FormattingOption;
import fitnesse.runner.PageResult;
import fitnesse.runner.StandardResultHandler;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/*
* Copied from TestRunner, but uses ProxyFitServer
*/
public class ProxyTestRunner {
    private String host;
    private int port;
    private int remotePort;
    private String pageName;
    private ProxyFitServer fitServer;
    public ProxyTestRunnerFixtureListener fixtureListener;
    public CachingResultFormatter handler;
    private PrintStream output;
    public List formatters = new LinkedList();
    private boolean debug;
    public boolean verbose;
    public boolean usingDownloadedPaths = true;

    public ProxyTestRunner() throws Exception {
        this(System.out);
    }

    public ProxyTestRunner(PrintStream output) throws Exception {
        this.output = output;
        handler = new CachingResultFormatter();
    }

    public static void main(String[] args) throws Exception {
        ProxyTestRunner runner = new ProxyTestRunner();
        runner.run(args);
        System.exit(runner.exitCode());
    }

    public void args(String[] args) throws Exception {
        CommandLine commandLine =
                new CommandLine("[-debug] [-v] [-results file] [-html file] [-xml file] [-nopath] host port pageName");
        if (!commandLine.parse(args)) {
            usage();
        }

        host = commandLine.getArgument("host");
        port = Integer.parseInt(commandLine.getArgument("port"));
        String remotePortTxt = System.getProperty("remote-port");
        if (remotePortTxt != null)
            remotePort = Integer.parseInt(remotePortTxt);
        pageName = commandLine.getArgument("pageName");

        if (commandLine.hasOption("debug")) {
            debug = true;
        }
        if (commandLine.hasOption("v")) {
            verbose = true;
            handler.addHandler(new StandardResultHandler(output));
        }
        if (commandLine.hasOption("nopath")) {
            usingDownloadedPaths = false;
        }
        if (commandLine.hasOption("results")) {
            formatters.add(new FormattingOption("raw",
                    commandLine.getOptionArgument("results", "file"),
                    output,
                    host,
                    port,
                    pageName));
        }
        if (commandLine.hasOption("html")) {
            formatters.add(new FormattingOption("html",
                    commandLine.getOptionArgument("html", "file"),
                    output,
                    host,
                    port,
                    pageName));
        }
        if (commandLine.hasOption("xml")) {
            formatters.add(new FormattingOption("xml",
                    commandLine.getOptionArgument("xml", "file"),
                    output,
                    host,
                    port,
                    pageName));
        }
    }

    private void usage() {
        System.out.println("usage: java [-Dremote-port] fitnesse.runner.TestRunner [options] host port page-name");
        System.out.println("\t-v \tverbose: prints test progress to stdout");
        System.out.println("\t-results <filename|'stdout'>\tsave raw test results to a file or dump to standard output");
        System.out
                .println("\t-html <filename|'stdout'>\tformat results as HTML and save to a file or dump to standard output");
        System.out.println("\t-debug \tprints FitServer protocol actions to stdout");
        System.out.println("\t-nopath \tprevents downloaded path elements from being added to classpath");
//      SystemExitUtils.exitWithAbnormalCode();
    }

    public void run(String[] args) throws Exception {
        args(args);
        fitServer = new ProxyFitServer(host, port, remotePort, debug);
        fixtureListener = new ProxyTestRunnerFixtureListener(this);
//      fitServer.fixtureListener = fixtureListener;
        fitServer.establishConnection(makeHttpRequest());
        fitServer.validateConnection();
        if (usingDownloadedPaths) {
            processClasspathDocument();
        }
        fitServer.process();
        finalCount();
        fitServer.closeConnection();
        fitServer.exit();
        doFormatting();
        handler.cleanUp();
    }

    private void processClasspathDocument() throws Exception {
        String classpathItems = fitServer.readDocument();
        if (verbose) {
            output.println("Adding to classpath: " + classpathItems);
        }
        addItemsToClasspath(classpathItems);
    }

    private void finalCount() throws Exception {
        Counts counts = fitServer.getCounts();
        handler.acceptFinalCount(new TestSummary(counts.right, counts.wrong, counts.ignores, counts.exceptions));
    }

    public int exitCode() {
        return fitServer == null ? -1 : fitServer.exitCode();
    }

    public String makeHttpRequest() {
        String request = "GET /" + pageName + "?responder=fitClient";
        if (usingDownloadedPaths) {
            request += "&includePaths=yes";
        }
        return request + " HTTP/1.1\r\n\r\n";
    }

    public Counts getCounts() {
        return fitServer.getCounts();
    }

    public void acceptResults(PageResult results) throws Exception {
//        Counts counts = results.counts();
//      fitServer.writeCounts(counts);
        handler.acceptResult(results);
    }

    public void doFormatting() throws Exception {
        for (Object formatter : formatters) {
            FormattingOption option = (FormattingOption) formatter;
            if (verbose) {
                output.println(new StringBuilder().append("Formatting as ")
                        .append(option.format)
                        .append(" to ")
                        .append(option.filename).toString());
            }
            option.process(handler.getResultStream(), handler.getByteCount());
        }
    }

    public static void addItemsToClasspath(String classpathItems) throws Exception {
        String[] items = classpathItems.split("[:;]");
        for (String item : items) {
            addUrlToClasspath(new File(item).toURL());
        }
    }

    public static void addUrlToClasspath(URL u) throws Exception {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        Method method = sysclass.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(sysloader, u);
    }
}
