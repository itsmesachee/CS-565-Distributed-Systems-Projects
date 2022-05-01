package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.job.Tool;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;
import appserver.server.Server;

/**
 * Class [Satellite] Instances of this class represent computing nodes that
 * execute jobs by calling the callback method of tool a implementation, loading
 * the tool's code dynamically over a network or locally from the cache, if a
 * tool got executed before.
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Satellite extends Thread
{

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader;
    private Hashtable<String, Tool> toolsCache;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile)
    {

        // read this satellite's properties and populate satelliteInfo object,
        // which later on will be sent to the server
        // --------------------------
        // Read Name and Port number of Satellite and set for the satelliteInfo using those values
        try
        {
            PropertyHandler satelliteProperties = new PropertyHandler(satellitePropertiesFile);
            satelliteInfo.setName(satelliteProperties.getProperty("NAME"));
            satelliteInfo.setPort(Integer.parseInt(satelliteProperties.getProperty("PORT")));
            satelliteInfo.setHost("127.0.0.1");
        } catch (IOException e)
        {
            System.err.println("[Satellite.Satellite] Error: Satellite Property File Not Found!");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // read properties of the application server and populate serverInfo object
        // other than satellites, the application server doesn't have a human-readable name, so leave it out
        // --------------------------------
        // Read Port number and Host IP address of Server and set for the serverInfo using those values
        try
        {
            PropertyHandler serverProperties = new PropertyHandler(serverPropertiesFile);
            serverInfo.setPort(Integer.parseInt(serverProperties.getProperty("PORT")));
            serverInfo.setHost(serverProperties.getProperty("HOST"));
        } catch (IOException e)
        {
            System.err.println("[Satellite.Satellite] Error: Server Property File Not Found!");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // read properties of the code server and create class loader
        // -------------------
        try
        {
            PropertyHandler classLoaderProperties = new PropertyHandler(classLoaderPropertiesFile);
            classLoader = new HTTPClassLoader(classLoaderProperties.getProperty("HOST"), Integer.parseInt(classLoaderProperties.getProperty("PORT")));
        } catch (IOException e)
        {
            System.err.println("[Satellite.Satellite] Error: HTTPClassLoader Property File Not Found!");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // create tools cache
        // -------------------
        toolsCache = new Hashtable();

    }

    @Override
    public void run()
    {

        // register this satellite with the SatelliteManager on the server
        // ---------------------------------------------------------------
        try {
            Socket serverSocket = new Socket(serverInfo.getHost(), serverInfo.getPort());
            Message registrationMessage = new Message(REGISTER_SATELLITE, satelliteInfo);
            ObjectOutputStream writeToNet = new ObjectOutputStream(serverSocket.getOutputStream());
            System.out.println("[Satellite.run] Sending message to server to register satellite: " + satelliteInfo.getName() + "...");
            writeToNet.writeObject(registrationMessage);
        } catch (IOException e)
        {
            System.err.println("[Satellite.run]  Error: Satellite Registration unsuccessful!");
            e.printStackTrace(System.err);
        }

    
    // ...
    // create server socket
    // ---------------------------------------------------------------    
    
    try 
    {
        ServerSocket serverSocket = new ServerSocket(satelliteInfo.getPort());
        System.out.println("[Satellite.run] ServerSocket created on port #:" + satelliteInfo.getPort());

        // start taking job requests in a server loop
        // ---------------------------------------------------------------
        while (true)
        { 
            new SatelliteThread(serverSocket.accept(), this).run();
        }
    }
    catch (IOException e)
    {
        System.err.println("[Satellite.run] Error creating ServerSocket");
        e.printStackTrace(System.err);
    }
}
    

    // inner helper class that is instanciated in above server loop and processes single job requests
    private class SatelliteThread extends Thread
    {

        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite)
        {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run()
        {
            // setting up object streams
            // ...
            try
            {
                readFromNet = new ObjectInputStream(jobRequest.getInputStream());
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());

                // reading message
                // ...
                message = (Message) readFromNet.readObject();

                switch (message.getType())
                {
                    case JOB_REQUEST:
                            // processing job request
                            // ...
                            try
                        {
                            // Use Tool Interface to convert client's object to job parameters
                            // Use job parameters to perform operation and then return result
                            Job job = (Job) message.getContent();
                            String toolClassString = job.getToolName();
                            Tool tool;
                            try
                            {
                                tool = getToolObject(toolClassString);
                                Object result = tool.go(job.getParameters());
                                writeToNet.writeObject(result);
                            } catch (NoSuchMethodException ex)
                            {
                                Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalArgumentException ex)
                            {
                                Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InvocationTargetException ex)
                            {
                                Logger.getLogger(Satellite.class.getName()).log(Level.SEVERE, null, ex);
                            }
                    } catch (UnknownToolException | InstantiationException | IllegalAccessException | ClassNotFoundException e)
                    {
                        System.err.println("[SatelliteThread.run] Error processing job request.");
                        e.printStackTrace(System.err);
                    }
                    break;

                    default:
                        System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
                }
            } catch (IOException | ClassNotFoundException e)
            {
                System.out.println("[Satellite.SatelliteThread] Error setting up job streams");
            }

        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string If
     * the tool has been used before, it is returned immediately out of the cache,
     * otherwise it is loaded dynamically
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {

        Tool toolObject = null;

        // ...
        toolObject = toolsCache.get(toolClassString);

        if (toolObject == null)
        {
            System.out.println("[Satellite.getToolObject] Tool Class not in cache");
            System.out.println("[Satellite.getToolObject] Tool Class : " + toolClassString);
            if (toolClassString == null)
            {
                throw new UnknownToolException();
            }
            Class toolClass = classLoader.loadClass(toolClassString);
            toolObject = (Tool) toolClass.getDeclaredConstructor().newInstance();
            toolsCache.put(toolClassString, toolObject);
        } else
        {
            System.out.println("[Satellite.getToolObject] Tool Class: " + toolClassString + " already in Cache");
        }

        return toolObject;
    }

    public static void main(String[] args)
    {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.start();
    }
}
