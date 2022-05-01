package appserver.server;

import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.comm.ConnectivityInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import utils.PropertyHandler;

/**
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Server
{

    // Singleton objects - there is only one of them. For simplicity, this is not enforced though ...
    static SatelliteManager satelliteManager = null;
    static LoadManager loadManager = null;
    static ServerSocket serverSocket = null;

    public Server(String serverPropertiesFile)
    {

        // create satellite manager and load manager
        // ...
        satelliteManager = new SatelliteManager();
        loadManager = new LoadManager();

        // read server properties and create server socket
        // ...
        try
        {
            PropertyHandler serverProperties = new PropertyHandler(serverPropertiesFile);
            int port = Integer.parseInt(serverProperties.getProperty("PORT"));
            serverSocket = new ServerSocket(port);
            System.out.println("[Server.Server] ServerSocket created on Port " + port);
        } catch (IOException e)
        {
            System.err.println("[Server.Server]  Error: Server Property File Not Found!");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public void run()
    {
        // serve clients in server loop ...
        // when a request comes in, a ServerThread object is spawned
        // ...
        while (true)
        {
            try
            {
                (new ServerThread(serverSocket.accept())).start();
            } catch (IOException e)
            {
                System.err.println("[Server.run] Error: Accepting new connections failed at serverSocket!");
                e.printStackTrace(System.err);
            }
        }
    }

    // objects of this helper class communicate with satellites or clients
    private class ServerThread extends Thread
    {

        Socket client = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        private ServerThread(Socket client)
        {
            this.client = client;
        }

        @Override
        public void run()
        {
            // set up object streams and read message
            // ...
            try
            {
                readFromNet = new ObjectInputStream(client.getInputStream());
                writeToNet = new ObjectOutputStream(client.getOutputStream());

                message = (Message) readFromNet.readObject();

            } catch (IOException | ClassNotFoundException e)
            {
                System.err.println("[ServerThread.run] Error: Setting up object streams failed!");
                e.printStackTrace(System.err);
            }

            // process message
            switch (message.getType())
            {
                case REGISTER_SATELLITE:
                    // read satellite info
                    // ...
                    ConnectivityInfo newSatelliteInfo = (ConnectivityInfo) message.getContent();

                    // register satellite
                    synchronized (Server.satelliteManager)
                    {
                        // ...
                        Server.satelliteManager.registerSatellite(newSatelliteInfo);
                    }

                    // add satellite to loadManager
                    synchronized (Server.loadManager)
                    {
                        // ...
                        Server.loadManager.satelliteAdded(newSatelliteInfo.getName());
                    }

                    break;

                case JOB_REQUEST:
                    System.err.println("\n[ServerThread.run] Received job request");

                    String satelliteName = null;
                    try
                    {
                        synchronized (Server.loadManager)
                        {
                            // get next satellite from load manager
                            // ...
                            satelliteName = Server.loadManager.nextSatellite();
                            System.out.println("[ServerThread.run] Requesting next satellite name: " + satelliteName);

                            // get connectivity info for next satellite from satellite manager
                            // ...
                            ConnectivityInfo satelliteInfo = Server.satelliteManager.getSatelliteForName(satelliteName);
                            System.out.println("[ServerThread.run] Requesting next satellite connectivity info: " + satelliteName);

                            Socket satellite = null;
                            // connect to satellite
                            // ...
                            satellite = new Socket(satelliteInfo.getHost(), satelliteInfo.getPort());
                            System.out.println("[ServerThread.run] Established Socket connection to satellite: " + satelliteName);

                            // open object streams,
                            ObjectOutputStream writeToSat = new ObjectOutputStream(satellite.getOutputStream());
                            // forward message (as is) to satellite,
                            writeToSat.writeObject(message);
                            System.out.println("[ServerThread.run] Job request sent to satellite: " + satelliteName);

                            // receive result from satellite and
                            ObjectInputStream readFromSat = new ObjectInputStream(satellite.getInputStream());
                            Object result = readFromSat.readObject();

                            // write result back to client
                            // ...
                            writeToNet.writeObject(result);
                        }

//                        Socket satellite = null;
//                        // connect to satellite
//                        // ...
//                        Socket satellite = new Socket(satelliteInfo.getHost(), satelliteInfo.getPort());
//                        System.out.println("[ServerThread.run] Established Socket connection to satellite: " + satelliteName);
//
//                        // open object streams,
//                        ObjectOutputStream writeToSat = new ObjectOutputStream(satellite.getOutputStream());
//                        // forward message (as is) to satellite,
//                        writeToSat.writeObject(message);
//                        System.out.println("[ServerThread.run] Job request sent to satellite: " + satelliteName);
//
//                        // receive result from satellite and
//                        ObjectInputStream readFromSat = new ObjectInputStream(satellite.getInputStream());
//                        Object result = readFromSat.readObject();
//
//                        // write result back to client
//                        // ...
//                        writeToNet.writeObject(result);
//                        
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        System.err.println("[ServerThread.run] Error processing job request.");
                    }

                    break;

                default:
                    System.err.println("[ServerThread.run] Warning: Message type not implemented");
            }
        }
    }

    // main()
    public static void main(String[] args)
    {
        // start the application server
        Server server = null;
        if (args.length == 1)
        {
            server = new Server(args[0]);
        } else
        {
            server = new Server("../../config/Server.properties");
        }
        server.run();
    }
}
