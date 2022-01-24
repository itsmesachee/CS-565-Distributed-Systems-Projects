package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.Message;
import model.MessageTypes;
import model.NodeInfo;

public class Server implements MessageTypes {

    private static List<ObjectOutputStream> outStreams = new ArrayList<>();
    private ServerSocket ss;

    public Server() throws IOException {
        
    	//for reducing ambiguity, i have hardcardoded the port number to avoid any unexpected closed ports.
        
        ss = new ServerSocket(1012);
        System.out.println("Waiting for client...");

        while (true) {
            Socket s = null;

            try {
                
            	/*
            	 * Opening the socket connection and creating streams laying paths for clients to communicate with server
            	 * and between themselves.
            	 */
                
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
        
        
        // Creating a server object through main() which initiates the next steps in commencement of server.
 
    }

// ClientHandler class
    class ClientHandler extends Thread {

    	/*
    	 * This class is specifically used to handle multiple clients and facilitate communication betwwen them.
    	 */
        
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
                    
                    /*
                     * Based on the type of message constant which matches to the command sent from a client to the server, respective
                     * code block goes lives on matching the condition
                     */



                    if (message.getType() == MessageTypes.JOIN) {
                        // If new user sends JOIN message, add user to the Arraylist
                        outStreams.add(oos);
                        NodeInfo info = (NodeInfo) message.getContent();
                        System.out.println(info.getName() + " has joined!");
                    } else if (message.getType() == MessageTypes.LEAVE) {
                        /* If user sends LEAVE message, remove user from Arraylist
                         *
                    	 * closing the streams and giving a prompt/debugging info to the client and on server too.
                    	 */
                        
                        outStreams.remove(oos);
                        ois.close();
                        oos.close();
                        NodeInfo info = (NodeInfo) message.getContent();
                        System.out.println(info.getName() + " has left!");
                        break;
                    } else if (message.getType() == MessageTypes.NOTE) {
                        // If user sends a regular string, send the message string to all users
                        /*
                    	 * Server side implementation of passing/forwading the messages/communication happening between clients
                    	 * facilitated by the chat connection through server.
                    	 */
                        
                        System.out.println(message.getContent());
                        if (outStreams.contains(oos)) {
                            for (ObjectOutputStream stream : outStreams) {
                                stream.writeObject(message);
                            }
                        }
                    } else if (message.getType() == MessageTypes.SHUTDOWN_ALL) {
                        // If user sends SHUTDOWN ALL, terminate all connections and shut down the server
                        
                        
                    	/*
                    	 * posting a message to all chat-connected clients and on serves as well.
                    	 */
                        
                        System.out.println("Server is shutting down...");
                        for (ObjectOutputStream stream : outStreams) {
                            stream.writeObject(message);
                        }
//                        System.exit(0);
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
