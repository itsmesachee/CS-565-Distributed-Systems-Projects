package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import model.Message;
import model.MessageTypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import model.NodeInfo;

public class Client {

    private String name;
    NodeInfo nodeInfo;

    ObjectInputStream ois;
    ObjectOutputStream oos;

    Socket socket;

    public Client() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: "); // Enter human readable client name
        name = sc.nextLine();

        System.out.println("Write commands..");
        while (true) {

            try {

                String text = sc.nextLine();
                String[] parts = text.split(" "); // Split the message to identify any commands
                
                // First word of string is JOIN 
                if (parts[0].equals("JOIN")) {
                    //Read from file
                    // Parse the 2nd and 3rd parts of JOIN message to identify IP and port
                    nodeInfo = new NodeInfo(parts[1], Integer.parseInt(parts[2]), name);
                    socket = new Socket(nodeInfo.getIP(), nodeInfo.getPort());
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());
                    Message msg = new Message(MessageTypes.JOIN, nodeInfo);
                    oos.writeObject(msg);
                    System.out.println("Chat Group Joined!");

                    Thread thread = new Thread(() -> {
                        while (true) {
                            try {
                                if (socket != null) {
                                    Message msg1 = (Message) ois.readObject();
                                    if (msg1.getType() == MessageTypes.NOTE) {
                                        System.out.println(msg1.getContent());
                                    } else if (msg1.getType() == MessageTypes.SHUTDOWN_ALL) {
                                        System.out.println("Server is closed!");
                                        socket.close();
                                        System.exit(0);
                                    }
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                //e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

                } else if (parts[0].equals("LEAVE")) {
                    // If first word of string is LEAVE, leave chat without terminating
                    Message msg = new Message(MessageTypes.LEAVE, nodeInfo);
                    oos.writeObject(msg);
                    socket.close();
                    socket = null;
                    ois = null;
                    oos = null;

                    System.out.println("You have left chat group!");

                } else if (parts[0].equals("SHUTDOWN_ALL")) {
                    // Shutdown all clients and the server
                    if (socket != null) {
                        Message msg = new Message(MessageTypes.SHUTDOWN_ALL, null);
                        oos.writeObject(msg);
                    }
                } else if (parts[0].equals("SHUTDOWN")) {
                    if (socket != null) {
                        Message msg = new Message(MessageTypes.SHUTDOWN, nodeInfo);
                        oos.writeObject(msg);
                        socket.close();
                    }
                    System.exit(0);
                } else {

                    if (socket != null) {
                        Message msg = new Message(MessageTypes.NOTE, name + ": " + text);
                        oos.writeObject(msg);
                    } else {
                        System.out.println("You have not joined the chat!");
                    }

                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new Client();
    }

    private NodeInfo readConnectionInfo() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("properties.txt"));
            String[] parts = scanner.nextLine().split(" ");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            return new NodeInfo(ip, port, name);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
        return null;
    }

}
