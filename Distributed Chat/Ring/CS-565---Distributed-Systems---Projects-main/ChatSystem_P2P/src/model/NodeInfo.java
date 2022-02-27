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

    public NodeInfo(String IP, int port, String name)
    {
        this.IP = IP;
        this.port = port;
        this.name = name;
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

    public void setIP(String IP)
    {
        this.IP = IP;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

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
