package org.codehaus.fitnesseweb.executor;

import fit.Counts;
import fit.Fixture;
import fit.FixtureListener;
import fit.Parse;
import fit.exception.FitParseException;
import fitnesse.components.FitProtocol;
import fitnesse.util.StreamReader;

import java.io.*;
import java.net.Socket;

public class WebFitServer {
    private int testNumber = 0;
    private Parse tables;
    private Fixture fixture = new Fixture();
    private FixtureListener fixtureListener = new TablePrintingFixtureListener();
    private Counts counts = new Counts();
    private OutputStream socketOutput;
    private StreamReader socketReader;
    private boolean verbose = false;
    private Socket socket;

    public WebFitServer(Socket socket, boolean verbose) {
        this.socket = socket;
        this.verbose = verbose;
    }

    public WebFitServer() {
        // empty body
    }


    public void establishConnection() throws Exception {
        establishConnection("");
    }

    public void run() throws Exception {
        establishConnection();
        validateConnection();
        process();
        closeConnection();
    }

    public void closeConnection() throws IOException {
        socket.close();
    }

    public void process() {
        fixture.listener = fixtureListener;
        try {
            int size;
            //noinspection NestedAssignment
            while ((size = FitProtocol.readSize(socketReader)) != 0) {
                try {
                    print("processing document of size: " + size + "\n");
                    String document = FitProtocol.readDocument(socketReader, size);
                    //TODO MDM if the page name was always the first line of the body, it could be printed here.
                    tables = new Parse(document);
                    newFixture().doTables(tables);
                    print("\tresults: " + fixture.counts() + "\n");
                    counts.tally(fixture.counts);
                } catch (FitParseException e) {
                    exception(e);
                }
            }
            print("completion signal recieved" + "\n");
        }
        catch (Exception e) {
            exception(e);
        }
    }

    public String readDocument() throws Exception {
        int size = FitProtocol.readSize(socketReader);

        return FitProtocol.readDocument(socketReader, size);
    }

    private Fixture newFixture() {
        fixture = new Fixture();
        fixture.listener = fixtureListener;
        return fixture;
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


    public void establishConnection(String httpRequest) throws Exception {
        socketOutput = new BufferedOutputStream(socket.getOutputStream());
        socketReader = new StreamReader(new BufferedInputStream(socket.getInputStream()));
        byte[] bytes = httpRequest.getBytes("UTF-8");
        socketOutput.write(bytes);
        socketOutput.flush();
        print("http request sent" + "\n");
    }

    public void validateConnection() throws Exception {
        print("validating connection...");
        int statusSize = FitProtocol.readSize(socketReader);
        if (statusSize == 0) {
            print("...ok" + "\n");
        } else {
            String errorMessage = FitProtocol.readDocument(socketReader, statusSize);
            print("...failed becuase: " + errorMessage + "\n");
            System.out.println("An error occured while connecting to client.");
            System.out.println(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    public Counts getCounts() {
        return counts;
    }

    private void print(String message) {
        if (verbose) {
            System.out.print(message);
        }
    }

    public static byte[] readTable(Parse table) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(byteBuffer, "UTF-8");
        PrintWriter writer = new PrintWriter(streamWriter);
        Parse more = table.more;
        table.more = null;
        if (table.trailer == null) {
            table.trailer = "";
        }
        table.print(writer);
        table.more = more;
        writer.close();
        return byteBuffer.toByteArray();
    }

    public void writeCounts(Counts count) throws IOException {
        //TODO This can't be right.... which counts should be used?
        FitProtocol.writeCounts(counts, socketOutput);
    }

    public void setFixtureListener(FixtureListener fixtureListener) {
        if (fixtureListener != null) {
            CompositeFixtureListener compositeFixtureListener = new CompositeFixtureListener();
            compositeFixtureListener.addFixtureListener(this.fixtureListener);
            compositeFixtureListener.addFixtureListener(fixtureListener);
            this.fixtureListener = compositeFixtureListener;
        }
    }

    class TablePrintingFixtureListener implements FixtureListener {
        public void tableFinished(Parse table) {
            try {
                byte[] bytes = readTable(table);
                if (bytes.length > 0) {
                    FitProtocol.writeData(bytes, socketOutput);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void tablesFinished(Counts count) {
            try {
                FitProtocol.writeCounts(count, socketOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}