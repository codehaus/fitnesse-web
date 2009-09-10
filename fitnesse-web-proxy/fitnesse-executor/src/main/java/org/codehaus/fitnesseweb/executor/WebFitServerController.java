package org.codehaus.fitnesseweb.executor;

import fit.FixtureLoader;
import fit.FixtureListener;
import org.codehaus.fitnesseweb.fixture.SpringFixtureLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class WebFitServerController implements Runnable, ApplicationContextAware {
    private int socketPort;
    private boolean verbose = false;
    private ApplicationContext applicationContext;
    private ServerSocket serverSocket;
    private TaskExecutor taskExecutor;
    private FixtureListener fixtureListener;
    private boolean closing = false;

    @Required
    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }

    @Required
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void run() {
        try {
            while (!closing) {
                Socket socket = serverSocket.accept();
                WebFitServer webFitServer = new WebFitServer(socket, verbose);
                webFitServer.setFixtureListener(fixtureListener);
                webFitServer.run();
            }
        } catch (SocketException se) {
            System.out.println("server socket closed");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void init() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(socketPort);
        registerSpringAwareFixtureLoader();
        taskExecutor.execute(this);
    }

    private void registerSpringAwareFixtureLoader() throws ClassNotFoundException {
        FixtureLoader.setInstance(new SpringFixtureLoader(this.applicationContext));
    }

    public void destroy() throws IOException {
        closing = true;
        serverSocket.close();

    }

    public static void main(String[] args) {
        WebFitServerController fitServerController = new WebFitServerController();
        fitServerController.setSocketPort(7777);
        fitServerController.run();
    }

    public void setFixtureListener(FixtureListener fixtureListener) {
        this.fixtureListener = fixtureListener;
    }
}