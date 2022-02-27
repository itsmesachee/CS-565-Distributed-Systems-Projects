package model;

import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NodeInfo implements Serializable
{

    private String IP;
    private int port;
    private String name;
    ObjectOutputStream objectOutputStream;

    NodeInfo nextNode;
    // This method holds the current node information
    public NodeInfo(String IP, int port, String name)
    {
        this.IP = IP;
        this.port = port;
        this.name = name;
    }

    // This method is used to get the IP Address
    public String getIP()
    {
        return IP;
    }
    // This method is used to get the Port number
    public int getPort()
    {
        return port;
    }
    // This method is used to get the data in the next node
    public NodeInfo getNextNode()
    {
        return nextNode;
    }
    // This method is used to set the data in the next node 
    public void setNextNode(NodeInfo nextNode)
    {
        this.nextNode = nextNode;
    }
     // This method is used to set the IP Address while joining the chat
    public void setIP(String IP)
    {
        this.IP = IP;
    }
    // This method is used to set the port number while joining the chat
    public void setPort(int port)
    {
        this.port = port;
    }
    // This method is used to get the name of next node
    public String getName()
    {
        return name;
    }
    public ObjectOutputStream getObjectOutputStream()
    {
        return objectOutputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream)
    {
        this.objectOutputStream = objectOutputStream;
    }
}
