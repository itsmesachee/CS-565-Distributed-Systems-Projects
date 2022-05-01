package appserver.client.fibonacci;

import appserver.comm.Message;
import appserver.comm.MessageTypes;
import appserver.job.Job;
import utils.PropertyHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * Class [FibonacciClient] A primitive POC client that uses the Fibonacci tool
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */

public class FibonacciClient extends Thread implements MessageTypes {

    String host = null;
    int port;

    Properties properties;

    int number;

    public FibonacciClient(String serverPropertiesFile, int number) {
        try {
            properties = new PropertyHandler(serverPropertiesFile);
            host = properties.getProperty("HOST");
            System.out.println("[FibonacciClient.FibonacciClient] Host: " + host);
            port = Integer.parseInt(properties.getProperty("PORT"));
            System.out.println("[FibonacciClient.FibonacciClient] Port: " + port);
            this.number = number;
        } catch (IOException e) {
            System.err.println("[FibonacciClient.FibonacciClient] Error: Server Property File Not Found!");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            // connect to application server
            Socket server = new Socket(host, port);

            // hard-coded string of class, aka tool name ... plus one argument
            String classString = "appserver.job.impl.Fibonacci";

            // create job and job request message
            Job job = new Job(classString, number);
            Message message = new Message(JOB_REQUEST, job);

            // sending job out to the application server in a message
            ObjectOutputStream writeToNet = new ObjectOutputStream(server.getOutputStream());
            writeToNet.writeObject(message);

            // reading result back in from application server
            // for simplicity, the result is not encapsulated in a message
            ObjectInputStream readFromNet = new ObjectInputStream(server.getInputStream());
            int result = (int) readFromNet.readObject();
            System.out.println("Fibonacci of " + number + ": " + result);
        } catch (Exception e) {
            System.err.println("[FibonacciClient.run] Error occurred");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        for (int i = 46; i > 0; i--) {
            (new FibonacciClient("../../config/Server.properties", i)).start();
        }
    }
}