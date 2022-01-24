package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.Message;
import model.NodeInfo;

public class Server {

    private static List<ObjectOutputStream> outStreams = new ArrayList<>();
    private ServerSocket ss;

    public Server() throws IOException {
        ss = new ServerSocket(1012);
        System.out.println("Waiting for client...");

        while (true) {
            Socket s = null;

            try {
                s = ss.accept();

                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

                Thread t = new ClientHandler(s, ois, oos);
                t.start();

            } catch (IOException e) {
                s.close();
                //e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
    }

// ClientHandler class
    class ClientHandler extends Thread {

        final ObjectInputStream ois;
        final ObjectOutputStream oos;
        final Socket s;
        final String sender = "abc";

        // Constructor
        public ClientHandler(Socket s, ObjectInputStream ois, ObjectOutputStream oos) {
            this.s = s;
            this.ois = ois;
            this.oos = oos;

        }

        @Override
        public void run() {
            try {

                while (true) {

                    Message message = (Message) ois.readObject();

                    if (message.getType().equals("JOIN")) {
                        // If new user sends JOIN message, add user to the Arraylist 
                        outStreams.add(oos);
                        NodeInfo info = (NodeInfo) message.getContent();
                        System.out.println(info.getName() + " has joined!");
                    } else if (message.getType().equals("LEAVE")) {
                        // If user sends LEAVE message, remove user from Arraylist
                        outStreams.remove(oos);
                        ois.close();
                        oos.close();
                        NodeInfo info = (NodeInfo) message.getContent();
                        System.out.println(info.getName() + " has left!");
                        break;
                    } else if (message.getType().equals("NOTE")) {
                        // If user sends a regular string, send the message string to all users
                        System.out.println(message.getContent());
                        if (outStreams.contains(oos)) {
                            for (ObjectOutputStream stream : outStreams) {
                                stream.writeObject(message);
                            }
                        }
                    } else if (message.getType().equals("SHUTDOWN ALL")) {
                        // If user sends SHUTDOWN ALL, terminate all connections and shut down the server
                        System.out.println("Server is shutting down...");
                        for (ObjectOutputStream stream : outStreams) {
                            stream.writeObject(message);
                        }
                        System.exit(0);
                    }

                }
            } catch (Exception e) {
                //e.printStackTrace();
                try {
                    outStreams.remove(oos);
                    // closing resources
                    this.ois.close();
                    this.oos.close();

                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }

        }
    }
}
