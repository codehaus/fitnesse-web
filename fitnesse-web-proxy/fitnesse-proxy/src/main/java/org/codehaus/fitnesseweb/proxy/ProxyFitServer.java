package org.codehaus.fitnesseweb.proxy;

import fit.Counts;
import fit.Fixture;
import fit.Parse;
import fit.FixtureListener;
import fit.exception.FitParseException;
import fitnesse.components.CommandLine;
import fitnesse.components.FitProtocol;
import fitnesse.util.StreamReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ProxyFitServer {
    private String input;
    private Parse tables;
    private Fixture fixture = new Fixture();
    private Counts counts = new Counts();
    private Socket serverSocket;
    private OutputStream serverSocketOutput;
    private StreamReader serverSocketReader;
    private OutputStream remoteSocketOutput;
    private StreamReader remoteSocketReader;
    public FixtureListener fixtureListener = null;
    private boolean verbose = false;
    private String host;
    private int port;
    private int remotePort = 7777;
    private int socketToken;

    public ProxyFitServer(String host, int port, int remotePort, boolean verbose) {
        this.host = host;
        this.port = port;
        this.remotePort = remotePort;
        this.verbose = verbose;
    }

    public ProxyFitServer() {
        // do nothing
    }

    public static void main(String[] argv) throws Exception {
        ProxyFitServer fitServer = new ProxyFitServer();
        fitServer.run(argv);
        System.exit(fitServer.exitCode());
    }

    public void run(String[] argv) throws Exception {
        args(argv);
        establishConnection();
        validateConnection();
        process();
        closeConnection();
        exit();
    }

    public void closeConnection() throws IOException {
        serverSocket.close();
    }

    public void process() throws Exception {
        Socket webSocket = new Socket(host, remotePort);

        try {
            remoteSocketOutput = new BufferedOutputStream(webSocket.getOutputStream());
            remoteSocketReader = new StreamReader(new BufferedInputStream(webSocket.getInputStream()));
            FitProtocol.writeSize(0, remoteSocketOutput); // first size should be zero for connection validation
            int size;
            //noinspection NestedAssignment
            while ((size = FitProtocol.readSize(serverSocketReader)) != 0) {
                try {
                    String document = FitProtocol.readDocument(serverSocketReader, size);
                    FitProtocol.writeData(document, remoteSocketOutput);
                    forwardResponse();
                } catch (FitParseException e) {
                    exception(e);
                } catch (IOException e) {
                    exception(e);
                }
            }
            FitProtocol.writeSize(0, remoteSocketOutput);

            print("completion signal recieved" + "\n");
        } catch (Exception e) {
            exception(e);
        } finally {
            try {
                webSocket.close();
            } catch (IOException e) {
                exception(e);
            }
        }
    }

    public void forwardResponse() throws Exception {
        int remoteSize;
        //noinspection NestedAssignment
        while ((remoteSize = FitProtocol.readSize(remoteSocketReader)) != 0) {
            String remoteDocument = FitProtocol.readDocument(remoteSocketReader, remoteSize);
            FitProtocol.writeData(remoteDocument, serverSocketOutput);
            if (fixtureListener != null){
                fixtureListener.tableFinished(new Parse(remoteDocument));
            }

        }
        Counts remoteCounts = FitProtocol.readCounts(remoteSocketReader);
        FitProtocol.writeCounts(remoteCounts, serverSocketOutput);
        if (fixtureListener != null){
            fixtureListener.tablesFinished(remoteCounts);
        }
    }

    public void args(String[] argv) {
        CommandLine commandLine = new CommandLine("[-v] remote-port host port socketToken");
        if (commandLine.parse(argv)) {
            host = commandLine.getArgument("host");
            port = Integer.parseInt(commandLine.getArgument("port"));
            remotePort = Integer.parseInt(commandLine.getArgument("remote-port"));
            socketToken = Integer.parseInt(commandLine.getArgument("socketToken"));
            verbose = commandLine.hasOption("v");
        } else {
            usage();
        }
    }

    private void usage() {
        System.out.println("usage: java fit.FitServer [-v] remote-port host port socketTicket");
        System.out.println("\t-v\tverbose");
//      SystemExitUtils.exitWithAbnormalCode();
    }

    protected void exception(Exception e) {
        print("Exception occurred!" + "\n");
        print("\t" + e.getMessage() + "\n");
        tables = new Parse("span", "Exception occurred: ", null, null);
        fixture.exception(tables, e);
        counts.exceptions += 1;
        fixture.listener.tableFinished(tables);
        fixture.listener.tablesFinished(counts); //TODO shouldn't this be fixture.counts
    }

    public void exit() throws Exception {
        print("exiting" + "\n");
        print("\tend results: " + counts.toString() + "\n");
    }

    public int exitCode() {
        return counts.wrong + counts.exceptions;
    }

    public void establishConnection() throws Exception {
        establishConnection(makeHttpRequest());
    }

    public void establishConnection(String httpRequest) throws Exception {
        serverSocket = new Socket(host, port);
        serverSocketOutput = new BufferedOutputStream(serverSocket.getOutputStream());
        serverSocketReader = new StreamReader(new BufferedInputStream(serverSocket.getInputStream()));
        byte[] bytes = httpRequest.getBytes("UTF-8");
        serverSocketOutput.write(bytes);
        serverSocketOutput.flush();
        print("http request sent" + "\n");
    }

    private String makeHttpRequest() {
        return "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
    }

    public void validateConnection() throws Exception {
        print("validating connection...");
        int statusSize = FitProtocol.readSize(serverSocketReader);
        if (statusSize == 0) {
            print("...ok" + "\n");
        } else {
            String errorMessage = FitProtocol.readDocument(serverSocketReader, statusSize);
            print("...failed bacuase: " + errorMessage + "\n");
            System.out.println("An error occured while connecting to client.");
            System.out.println(errorMessage);
//         SystemExitUtils.exitWithAbnormalCode();
        }
    }

    public String readDocument() throws Exception {
        int size = FitProtocol.readSize(serverSocketReader);

        return FitProtocol.readDocument(serverSocketReader, size);
    }

    public Counts getCounts() {
        return counts;
    }

    private void print(String message) {
        if (verbose) {
            System.out.print(message);
        }
    }
}
