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
    NodeInfo leftNode;
    NodeInfo rightNode;


    //instances of object - input & output streams, socket protocol

    ObjectInputStream clientLeftInputStream;
    ObjectOutputStream clientLeftOutputStream;

    ObjectInputStream clientRightInputStream;
    ObjectOutputStream clientRightOutputStream;

    Socket leftSocket, rightSocket;

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

    public void setClientLeftInputStream(ObjectInputStream clientLeftInputStream)
    {
        this.clientLeftInputStream = clientLeftInputStream;
    }

    public void setClientLeftOutputStream(ObjectOutputStream clientLeftOutputStream)
    {
        this.clientLeftOutputStream = clientLeftOutputStream;
    }

    public ObjectInputStream getClientRightInputStream()
    {
        return clientRightInputStream;
    }

    public void setClientRightInputStream(ObjectInputStream clientRightInputStream)
    {
        this.clientRightInputStream = clientRightInputStream;
    }

    public ObjectOutputStream getClientRightOutputStream()
    {
        return clientRightOutputStream;
    }

    public void setClientRightOutputStream(ObjectOutputStream clientRightOutputStream)
    {
        this.clientRightOutputStream = clientRightOutputStream;
    }

    public void listen() throws InterruptedException
    {

        Message message = new Message();
        message.readMessage(name, this);

    }

    public ObjectInputStream getClientLeftInputStream()
    {
        return clientLeftInputStream;
    }

    public ObjectOutputStream getClientLeftOutputStream()
    {
        return clientLeftOutputStream;
    }


    public String getIP()
    {
        return IP;
    }

    public int getPort()
    {
        return port;
    }

    public NodeInfo getLeftNode()
    {
        return leftNode;
    }

    public NodeInfo getRightNode()
    {
        return rightNode;
    }

    public String getName()
    {
        return name;
    }

    public void setLeftNode(NodeInfo leftNode)
    {
        this.leftNode = leftNode;
    }

    public NodeInfo rightNode()
    {
        return rightNode;
    }

    public void setRightNode(NodeInfo rightNode)
    {
        this.rightNode = rightNode;
    }

    public NodeInfo getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(NodeInfo currentNode)
    {
        this.currentNode = currentNode;
    }

    public Socket getLeftSocket()
    {
        return leftSocket;
    }

    public void setLeftSocket(Socket leftSocket)
    {
        this.leftSocket = leftSocket;
    }

    public Socket getRightSocket()
    {
        return rightSocket;
    }

    public void setRightSocket(Socket rightSocket)
    {
        this.rightSocket = rightSocket;
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
        } catch (FileNotFoundException e)
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

    @Override
    public void run()
    {
        Receiver receiver;
        try
        {
            receiver = new Receiver(currentClient);
            receiver.listenToClient(currentClient);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}