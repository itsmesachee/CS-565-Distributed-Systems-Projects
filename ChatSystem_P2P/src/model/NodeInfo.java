package model;

import java.io.Serializable;

public class NodeInfo implements Serializable{

    private String IP;
    private int port;
    private String name;

    public NodeInfo(String IP, int port, String name) {
        this.IP = IP;
        this.port = port;
        this.name = name;
    }

    public String getIP() {
        return IP;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }

}
