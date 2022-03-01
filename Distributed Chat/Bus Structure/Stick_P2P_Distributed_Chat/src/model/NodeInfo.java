package model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NodeInfo implements Serializable
{

    /*
    *  This class stores information about the current client such as the IP address, port, name,
    *  and left and right nodes. Getters and setters are used to access this data from other classes.
    * */

    private String IP;
    private int port;
    private String name;

    transient ObjectInputStream leftNodeInputStream;
    transient ObjectOutputStream leftNodeOutputStream;

    transient ObjectInputStream rightNodeInputStream;
    transient ObjectOutputStream rightNodeOutputStream;

    NodeInfo leftNode;
    NodeInfo rightNode;

    public NodeInfo(String IP, int port, String name)
    {
        this.IP = IP;
        this.port = port;
        this.name = name;
    }

    public NodeInfo(String IP, int port)
    {
        this.IP = IP;
        this.port = port;
    }

    public String getIP()
    {
        return IP;
    }

    public int getPort()
    {
        return port;
    }


    public NodeInfo getLeftNode(Message message)
    {
        return leftNode;
    }

    public void setLeftNode(NodeInfo leftNode)
    {
        this.leftNode = leftNode;
    }

    public NodeInfo getRightNode(Message message)
    {
        return rightNode;
    }

    public void setRightNode(NodeInfo rightNode)
    {
        this.rightNode = rightNode;
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

    public ObjectInputStream getLeftNodeInputStream()
    {
        return leftNodeInputStream;
    }

    public void setLeftNodeInputStream(ObjectInputStream leftNodeInputStream)
    {
        this.leftNodeInputStream = leftNodeInputStream;
    }

    public ObjectOutputStream getLeftNodeOutputStream()
    {
        return leftNodeOutputStream;
    }

    public void setLeftNodeOutputStream(ObjectOutputStream leftNodeOutputStream)
    {
        this.leftNodeOutputStream = leftNodeOutputStream;
    }

    public ObjectInputStream getRightNodeInputStream()
    {
        return rightNodeInputStream;
    }

    public void setRightNodeInputStream(ObjectInputStream rightNodeInputStream)
    {
        this.rightNodeInputStream = rightNodeInputStream;
    }

    public ObjectOutputStream getRightNodeOutputStream()
    {
        return rightNodeOutputStream;
    }

    public void setRightNodeOutputStream(ObjectOutputStream rightNodeOutputStream)
    {
        this.rightNodeOutputStream = rightNodeOutputStream;
    }
}
