package receiver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import client.Client;
import model.Message;
import model.MessageTypes;
import model.NodeInfo;

public class Receiver implements MessageTypes, Serializable
{
    /*
     *   Class that deals with messages received from other clients.
     * */

    private final ServerSocket serverSocket;

    public Receiver(Client currentClient) throws IOException
    {
        // Create Server Socket at the port entered by user
        serverSocket = new ServerSocket(currentClient.getPort());
    }

    public void listenToClient(Client currentClient) throws IOException
    {
        while (true)
        {
            Socket socket;
            try
            {

                /*
                 * Opening the socket connection and creating streams laying paths for clients to communicate with receiver
                 * and between themselves.
                 */

                // Accept socket connection when requested by predecessor node
                socket = serverSocket.accept();
                ObjectInputStream receiverInputStream = null;   //Initialized in the thread function
                ObjectOutputStream receiverOutputStream = null;

                // Create thread to handle the messages from predecessor node.
                Thread t = new ClientHandler(socket, receiverInputStream, receiverOutputStream, currentClient);
                t.start();

            }
            catch (IOException e)
            {
//                s.close();
                e.printStackTrace();
            }
        }

    }
}

// ClientHandler class
class ClientHandler extends Thread implements Serializable
{

    /*
     * This class is specifically used to handle multiple clients and facilitate communication between them.
     */

    // All connections between nodes are transient and not fixed permanently.
    transient ObjectInputStream receiverInputStream;
    transient ObjectOutputStream receiverOutputStream;

    Client currentClient;
    Socket socket;
    NodeInfo nextNode;

    // Constructor
    public ClientHandler(Socket socket, ObjectInputStream receiverInputStream, ObjectOutputStream receiverOutputStream, Client currentClient)
    {
        this.socket = socket;
        this.receiverInputStream = receiverInputStream;
        this.receiverOutputStream = receiverOutputStream;
        this.currentClient = currentClient;
    }

    @Override
    public void run()
    {
        try
        {
            receiverInputStream = new ObjectInputStream(socket.getInputStream());
            receiverOutputStream = new ObjectOutputStream(socket.getOutputStream());

            while (true)    // Keep receiving messages until shut down
            {
                // Read message from node's Input Stream
                Message message = (Message) receiverInputStream.readObject();

                /*
                 * Based on the type of message constant which matches to the command sent from a client to the receiver, respective
                 * code block goes lives on matching the condition
                 */


                /*
                 *  The following boolean values determine whether the current Client receiving the message is the Original Sender,
                 *  or the predecessor node or successor node (nextNode) of the original sender. This is useful to see if we need
                 *  to perform additional operations apart from forwarding the message to the rest of the network.                *
                 * */

                // This boolean value checks whether the client receiving the message is the original sender of the message
                //  by comparing the IP address and port set in originalSender with its own.
                boolean isOriginalSender = ((((message.getOriginalSender(message)).getPort()) == (currentClient.getPort()))
                        && (((message.getOriginalSender(message)).getIP()).equals(currentClient.getIP())));

                // isNextNode of Original Sender
                boolean isNextNode = (((((message.getOriginalSender(message)).getNextNode()).getPort()) == currentClient.getPort())
                        && ((((message.getOriginalSender(message)).getNextNode()).getIP()).equals((currentClient.getIP()))));

                // is Future PredecessorNode of Original Sender, i.e., it will become the predecessor after the node joins ring
                boolean isPredecessorJoin = (((((message.getOriginalSender(message)).getNextNode()).getPort()) == ((currentClient.getNextNode()).getPort()))
                        && ((((message.getOriginalSender(message)).getNextNode()).getIP()).equals((currentClient.getNextNode()).getIP())));

                // is PredecessorNode of Original Sender during regular ring operations
                boolean isPredecessor = (((message.getOriginalSender(message).getPort()) == (currentClient.getNextNode().getPort()))
                        && (((message.getOriginalSender(message).getIP()).equals(currentClient.getNextNode().getIP()))));

                if (message.getType() == MessageTypes.JOIN)
                {
                    /*
                     *  Client receives a message of JOIN type.
                     *  If the current Client is the next node of original sender, accept the socket connection.
                     *  If node is future predecessor of new node, request socket connection to new node,
                     *  since it is the new next node of that node.
                     *  Other nodes just forward it around the network.
                     * */

                    if (isNextNode)
                    {
                        currentClient.getCurrentNode().setObjectOutputStream(receiverOutputStream);
                        message.getOriginalSender(message).setNextNode(currentClient.getCurrentNode());
                    }

                    // Previously OriginalSender wouldn't have known the name of its successor, only IP and Port.
                    // So, it is set here so that it can be used later.
                    if(isOriginalSender)
                    {
                        currentClient.setNextNode(message.getOriginalSender(message).getNextNode());
                    }

                    // !!isOriginalSender is used to avoid this block while network has only one node.
                    if ((isPredecessorJoin) && (!isOriginalSender))
                    {
                        try
                        {
                            nextNode = new NodeInfo(message.getOriginalSender(message).getIP(),
                                                    message.getOriginalSender(message).getPort(),
                                                    message.getOriginalSender(message).getName());
                            currentClient.setNextNode(nextNode);
                            currentClient.setSocket(new Socket(currentClient.getNextNode().getIP(), currentClient.getNextNode().getPort()));
                            currentClient.setObjectOutputStream(new ObjectOutputStream(currentClient.getSocket().getOutputStream()));
                            currentClient.setObjectInputStream(new ObjectInputStream(currentClient.getSocket().getInputStream()));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }

                    // Other nodes forward message around the network
                    if (!(isOriginalSender))
                    {
                        System.out.println(message.getOriginalSender(message).getName() + " has joined!");
                        forwardMessage(message);
                    }
                }
                else if (message.getType() == MessageTypes.LEAVE)
                {
                    /* If user sends LEAVE message, remove user from and change nextNode for the Predecessor
                     *
                     * closing the streams and giving a prompt/debugging info to the client and on receiver too.
                     */

                    // Nodes not neighboring the leaving client just forward message around the network.
                    if ((!(isPredecessor)) && (!isOriginalSender))
                    {
                        System.out.println(message.getOriginalSender(message).getName() + " has left!");
                        forwardMessage(message);
                    }
                    if (isNextNode)
                    {
                        // Successor node closes the socket it originally accepted from the leaving client.

                        try
                        {
                            currentClient.getCurrentNode().setObjectOutputStream(null);
                            receiverInputStream.close();
                            receiverOutputStream.close();
                            break;
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if (isPredecessor)
                    {
                        // Predecessor of leaving client has to change its next node to the nextNode of the leaving client.
                        // Close old socket connection and create new one.

                        try
                        {
                            nextNode = new NodeInfo(message.getOriginalSender(message).getNextNode().getIP(), message.getOriginalSender(message).getNextNode().getPort(), message.getOriginalSender(message).getNextNode().getName());
                            currentClient.setNextNode(nextNode);
                            currentClient.setSocket(new Socket(currentClient.getNextNode().getIP(), currentClient.getNextNode().getPort()));
                            currentClient.setObjectOutputStream(new ObjectOutputStream(currentClient.getSocket().getOutputStream()));
                            currentClient.setObjectInputStream(new ObjectInputStream(currentClient.getSocket().getInputStream()));

                            System.out.println(message.getOriginalSender(message).getName() + " has left!");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                // Regular messages - just forward in opposite direction till you reach predecessor
                // (i.e.,covered all the nodes in network).
                else if (message.getType() == MessageTypes.NOTE)
                {
                    // If user sends a regular string, send the message string to all users
                    /*
                     * Receiver side implementation of passing/forwarding the messages/communication happening between clients
                     * facilitated by the chat connection through receiver.
                     */

                    System.out.println(message.getContent());

                    if (!(isPredecessor))
                        forwardMessage(message);
                }

                else if (message.getType() == MessageTypes.SHUTDOWN)
                {
                    /* If user sends SHUTDOWN message, remove user and change nextNode information of predecessor.
                     *
                     * closing the streams and giving a prompt/debugging info to the client and on receiver too.
                     * Finally exit the client (from client side)
                     */

                    // Works similar to LEAVE, but terminates at the end.

                    if ((!(isPredecessor)) && (!isOriginalSender))
                    {
                        System.out.println(message.getOriginalSender(message).getName() + " has shutdown!");
                        forwardMessage(message);
                    }
                    if (isNextNode)
                    {
                        try
                        {
                            currentClient.getCurrentNode().setObjectOutputStream(null);
                            receiverInputStream.close();
                            receiverOutputStream.close();
                            break;
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (isPredecessor)
                    {
                        try
                        {
                            nextNode = new NodeInfo(message.getOriginalSender(message).getNextNode().getIP(), message.getOriginalSender(message).getNextNode().getPort(), message.getOriginalSender(message).getNextNode().getName());
                            currentClient.setNextNode(nextNode);
                            currentClient.setSocket(new Socket(currentClient.getNextNode().getIP(), currentClient.getNextNode().getPort()));
                            currentClient.setObjectOutputStream(new ObjectOutputStream(currentClient.getSocket().getOutputStream()));
                            currentClient.setObjectInputStream(new ObjectInputStream(currentClient.getSocket().getInputStream()));


                            System.out.println(message.getOriginalSender(message).getName() + " has shutdown!");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else if (message.getType() == MessageTypes.SHUTDOWN_ALL)
                {
                    // If user sends SHUTDOWN ALL, terminate all connections and shut down the receiver


                    /*
                     * posting a message to all chat-connected clients and on serves as well.
                     */

                    System.out.println("Client is shutting down...");
                    if (currentClient.getNextNode() != null)
                        forwardMessage(message);
                    System.exit(0);
                }

                System.out.println("Next Node = " + currentClient.getNextNode().getName());
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            try
            {
                // closing resources
                this.receiverInputStream.close();
                this.receiverOutputStream.close();

            }
            catch (Exception ex)
            {
                //ex.printStackTrace();
            }
        }

    }

    public void forwardMessage(Message message)
    {
        try
        {
            currentClient.getObjectOutputStream().writeObject(message);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
