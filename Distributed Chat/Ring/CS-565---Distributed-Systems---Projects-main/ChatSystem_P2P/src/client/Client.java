package client;

import java.io.*;

import model.Message;
import model.MessageTypes;

import java.net.Socket;
import java.util.Scanner;

import model.NodeInfo;
import receiver.Receiver;

public class Client implements MessageTypes, Serializable
{
    /*
     * Class which starts the main program
     * */

    //variables to hold the name of client per that instance and nodeInfo for which client has established connection

    private final String name;

    NodeInfo currentNode;
    NodeInfo nextNode;


    //instances of object - input & output streams, socket protocol

    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    Socket socket;

    String IP;
    int port;

    public Client(String name, String IP, int port)
    {
        this.name = name;
        this.IP = IP;
        this.port = port;
        this.setCurrentNode(new NodeInfo(IP, port, name));
        System.out.println("Write commands..");
    }


    public void setObjectInputStream(ObjectInputStream objectInputStream)
    {
        this.objectInputStream = objectInputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream)
    {
        this.objectOutputStream = objectOutputStream;
    }

    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    public void listen() throws InterruptedException
    {

        Message message = new Message();
        message.readMessage(name, this);

    }

    public ObjectInputStream getObjectInputStream()
    {
        return objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream()
    {
        return objectOutputStream;
    }

    public Socket getSocket()
    {
        return socket;
    }


    public String getIP()
    {
        return IP;
    }

    public int getPort()
    {
        return port;
    }

    public NodeInfo getNextNode()
    {
        return nextNode;
    }

    public void setNextNode(NodeInfo nextNode)
    {
        this.nextNode = nextNode;
    }

    public NodeInfo getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(NodeInfo currentNode)
    {
        this.currentNode = currentNode;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException
    {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: "); // Enter human-readable client name
        String name = sc.nextLine();

        System.out.print("Enter your IP and port: "); // Enter human-readable client name
        String IPAndPort = sc.nextLine();
        String[] parts = IPAndPort.split(" ");
        String IP = parts[0];
        int port = Integer.parseInt(parts[1]);

        Client currentClient = new Client(name, IP, port);
        Thread thread2 = new UserHandler(currentClient);
        thread2.start();

        currentClient.listen();
    }

    private NodeInfo readConnectionInfo()
    {
        try
        {

            /*
             * Reading the details from properties file, as described in the requirements.
             */

            Scanner scanner = new Scanner(new FileInputStream("properties.txt"));
            String[] parts = scanner.nextLine().split(" ");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            return new NodeInfo(ip, port, name);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found!");
        }
        return null;
    }
}

class UserHandler extends Thread implements Serializable
{
    Client currentClient;

    public UserHandler(Client currentClient)
    {
        this.currentClient = currentClient;
    }

    public void run()
    {
        Receiver server;
        try
        {
            server = new Receiver(currentClient);
            server.listenToClient(currentClient);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
