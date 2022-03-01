package receiver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import client.Client;
import model.Message;
import model.MessageTypes;
import model.NodeInfo;

public class Receiver implements MessageTypes
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

                // Accept socket connection when requested by left or right nodes
                socket = serverSocket.accept();

                ObjectInputStream receiverInputStream = null;   //Initialized in the thread function
                ObjectOutputStream receiverOutputStream = null;

                // Create thread to handle the messages from both left and right nodes.
                Thread thread = new ClientHandler(socket, receiverInputStream, receiverOutputStream, currentClient);
                thread.start();

            } catch (IOException e)
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

    transient ObjectInputStream rightInputStream = null;
    transient ObjectOutputStream rightOutputStream = null;

    transient ObjectInputStream leftInputStream = null;
    transient ObjectOutputStream leftOutputStream = null;

    Client currentClient;
    Socket socket;

    NodeInfo leftNode;
    NodeInfo rightNode;
    NodeInfo futureRightNode;


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
                // Read message from left or right node
                Message message;
                message = (Message) receiverInputStream.readObject();

                /*
                 * Based on the type of message constant which matches to the command sent from a client to the receiver, respective
                 * code block goes lives on matching the condition
                 */

                boolean isOriginalSender = false;
                boolean isLeftNode = false;
                boolean isRightNode = false;
                boolean isForwarderLeftNode = false;
                boolean isForwarderRightNode = false;
                boolean isFutureRightNode = false;

                /*
                *  The following boolean values determine whether the current Client receiving the message is the Original Sender,
                *  or the left node or right node of the original sender. This is useful to see if we need to perform additional
                *  operations apart from forwarding the message to the rest of the network.                *
                * */

                // This boolean value checks whether the client receiving the message is the original sender of the message
                //  by comparing the IP address and port set in originalSender with its own.
                isOriginalSender = ((((message.getOriginalSender(message)).getPort()) == (currentClient.getPort()))
                                 && (((message.getOriginalSender(message)).getIP()).equals(currentClient.getIP())));

                // isLeftNode of Original Sender
                if (((message.getOriginalSender(message)).getLeftNode(message)) != null)
                {
                    isLeftNode = (((((message.getOriginalSender(message)).getLeftNode(message)).getPort()) == currentClient.getPort())
                              && ((((message.getOriginalSender(message)).getLeftNode(message)).getIP()).equals((currentClient.getIP()))));
                }

                // isRightNode of Original Sender
                if (((message.getOriginalSender(message)).getRightNode(message)) != null)
                {
                    isRightNode = (((((message.getOriginalSender(message)).getRightNode(message)).getPort()) == currentClient.getPort())
                               && ((((message.getOriginalSender(message)).getRightNode(message)).getIP()).equals((currentClient.getIP()))));
                }

                /*
                *  FutureRightNode temporarily stores the node that had been the right node of Client known by a joining Client.
                *  This later becomes the right node of the Client that has just joined the network.
                * */
                if ((message.getFutureRightNode(message)) != null)
                {
                    isFutureRightNode = (((message.getFutureRightNode(message).getPort()) == (currentClient.getPort()))
                                     && (((message.getOriginalSender(message)).getIP()).equals(currentClient.getIP())));
                }

                /*
                *  isForwarderLeftNode and isForwarderRightNode are used to determine the
                *  direction from which a message is received. The message is then forwarded in
                *  the other direction.
                * */
                if ((currentClient.getLeftNode()) != null)
                {
                    isForwarderLeftNode = ((((message.getForwarder(message)).getPort()) == ((currentClient.getLeftNode()).getPort()))
                                        && (((message.getForwarder(message)).getIP()).equals((currentClient.getLeftNode()).getIP())));
                }
                if ((currentClient.getRightNode()) != null)
                {
                    isForwarderRightNode = ((((message.getForwarder(message)).getPort()) == ((currentClient.getRightNode()).getPort()))
                                         && (((message.getForwarder(message)).getIP()).equals((currentClient.getRightNode()).getIP())));
                }


                if (message.getType() == MessageTypes.JOIN)
                {
                    /*
                     *  Client receives a message of JOIN type.
                     *  If the current Client is the left node of original sender,
                     *  temporarily store the rightnode in another variable.
                     *  Then add the sender of the message as new right node.
                     * */

                    if (isLeftNode)
                    {
                        message.getOriginalSender(message).setLeftNode(currentClient.getCurrentNode());
                        if (currentClient.getRightNode() != null)
                        {
                            futureRightNode = new NodeInfo(currentClient.getRightNode().getIP(), currentClient.getRightNode().getPort(), currentClient.getRightNode().getName());
                            message.setFutureRightNode(futureRightNode);
                            message.getOriginalSender(message).setRightNode(currentClient.getRightNode());

                            // Send a message to the old right node to change its left node to the message sender.
                            Message messageToOldRightNode = new Message(MessageTypes.CHANGE_LEFT_NODE, message.getContent(), message.getOriginalSender(message), currentClient.getCurrentNode());
                            messageToOldRightNode.setFutureRightNode(futureRightNode);
                            currentClient.getClientRightOutputStream().writeObject(messageToOldRightNode);
                        }

                        // Add sender of message as right node, request a socket connection.
                        currentClient.getCurrentNode().setRightNodeInputStream(receiverInputStream);
                        currentClient.getCurrentNode().setRightNodeOutputStream(receiverOutputStream);

                        rightNode = new NodeInfo(message.getOriginalSender(message).getIP(),
                                message.getOriginalSender(message).getPort(),
                                message.getOriginalSender(message).getName());

                        currentClient.setRightNode(rightNode);

                        currentClient.setRightSocket(new Socket(currentClient.getRightNode().getIP(), currentClient.getRightNode().getPort()));
                        currentClient.setClientRightOutputStream(new ObjectOutputStream(currentClient.getRightSocket().getOutputStream()));
                        currentClient.setClientRightInputStream(new ObjectInputStream(currentClient.getRightSocket().getInputStream()));

                        System.out.println(message.getOriginalSender(message).getName() + " has joined!");
                        forwardMessage(message);
                    }

                    else if (isOriginalSender)
                    {
                        /*
                        * This node has to make a connection to its new right node, request a socket connection.
                        * */

                        currentClient.setLeftNode(message.getOriginalSender(message).getLeftNode(message));
                        if (message.getOriginalSender(message).getRightNode(message) != null)
                        {
                            currentClient.setRightNode(message.getFutureRightNode(message));

                            currentClient.setRightSocket(new Socket(currentClient.getRightNode().getIP(), currentClient.getRightNode().getPort()));
                            currentClient.setClientRightOutputStream(new ObjectOutputStream(currentClient.getRightSocket().getOutputStream()));
                            currentClient.setClientRightInputStream(new ObjectInputStream(currentClient.getRightSocket().getInputStream()));

                            System.out.println(message.getOriginalSender(message).getName() + " has joined!");
                        }
                        forwardToRightNode(message);

                    }
                    else
                    {
                        if (isForwarderLeftNode)
                            forwardToRightNode(message);

                        if (isForwarderRightNode)
                            forwardToLeftNode(message);

                        System.out.println(message.getOriginalSender(message).getName() + " has joined!");
                    }
                }
                else if (message.getType() == MessageTypes.CHANGE_LEFT_NODE)
                {
                    /*
                    *   This message type is only used in the backend of implementation and not by the user.
                    *   It is sent by the node initially receiving the new node in JOIN to its erstwhile right node.
                    *   informing it to change its left node to the new client.
                    * */

                    if (isFutureRightNode)
                    {
                        leftNode = new NodeInfo(message.getOriginalSender(message).getIP(),
                                message.getOriginalSender(message).getPort(),
                                message.getOriginalSender(message).getName());

                        currentClient.setLeftNode(leftNode);

                        currentClient.setLeftSocket(new Socket(currentClient.getLeftNode().getIP(), currentClient.getLeftNode().getPort()));
                        currentClient.setClientLeftOutputStream(new ObjectOutputStream(currentClient.getLeftSocket().getOutputStream()));
                        currentClient.setClientLeftInputStream(new ObjectInputStream(currentClient.getLeftSocket().getInputStream()));

                    }
                }

                else if (message.getType() == MessageTypes.LEAVE)
                {
                    /* If user sends LEAVE message, remove user from and change nextNode for the Predecessor
                     *
                     * closing the streams and giving a prompt/debugging info to the client and on receiver too.
                     */

                    if (!(isOriginalSender))
                    {
                        if (isForwarderLeftNode)
                            forwardToRightNode(message);

                        if (isForwarderRightNode)
                            forwardToLeftNode(message);

                        System.out.println(message.getOriginalSender(message).getName() + " has left!");

                    }

                    if (isLeftNode)
                    {
                        // Left node of leaving client has to change its right node to the right node of the leaving client.
                        // Close old socket connection and create new one.

                        currentClient.getCurrentNode().setRightNodeInputStream(null);
                        currentClient.getCurrentNode().setRightNodeOutputStream(null);
                        receiverInputStream.close();
                        receiverOutputStream.close();

                        currentClient.setRightNode(null);

                        if ((message.getOriginalSender(message).getRightNode(message)) != null)
                        {
                            rightNode = new NodeInfo(message.getOriginalSender(message).getRightNode(message).getIP(),
                                    message.getOriginalSender(message).getRightNode(message).getPort(),
                                    message.getOriginalSender(message).getRightNode(message).getName());

                            currentClient.setRightNode(rightNode);
                            currentClient.setRightSocket(new Socket(currentClient.getRightNode().getIP(), currentClient.getRightNode().getPort()));

                            currentClient.setClientRightOutputStream(new ObjectOutputStream(currentClient.getRightSocket().getOutputStream()));
                            currentClient.setClientRightInputStream(new ObjectInputStream(currentClient.getRightSocket().getInputStream()));
                        }

                    }

                    if (isRightNode)
                    {

                        // Right node of leaving client has to change its left node to the left node of the leaving client.
                        // Close old socket connection and create new one.

                        currentClient.getCurrentNode().setLeftNodeInputStream(null);
                        currentClient.getCurrentNode().setLeftNodeOutputStream(null);

                        receiverInputStream.close();
                        receiverOutputStream.close();

                        currentClient.setLeftNode(null);

                        if ((message.getOriginalSender(message).getLeftNode(message)) != null)
                        {
                            leftNode = new NodeInfo(message.getOriginalSender(message).getLeftNode(message).getIP(),
                                    message.getOriginalSender(message).getLeftNode(message).getPort(),
                                    message.getOriginalSender(message).getLeftNode(message).getName());

                            currentClient.setLeftNode(leftNode);
                            currentClient.setLeftSocket(new Socket(currentClient.getLeftNode().getIP(), currentClient.getLeftNode().getPort()));

                            currentClient.setClientLeftOutputStream(new ObjectOutputStream(currentClient.getLeftSocket().getOutputStream()));
                            currentClient.setClientLeftInputStream(new ObjectInputStream(currentClient.getLeftSocket().getInputStream()));
                        }
                    }
                }

                // Regular messages - just forward in opposite direction till you reach edge of network.
                else if (message.getType() == MessageTypes.NOTE)
                {
                    // If user sends a regular string, send the message string to all users
                    /*
                     * Receiver side implementation of passing/forwarding the messages/communication happening between clients
                     * facilitated by the chat connection through receiver.
                     */

                    System.out.println(message.getContent());

                    if (isForwarderLeftNode)
                        forwardToRightNode(message);

                    else if (isForwarderRightNode)
                        forwardToLeftNode(message);
                }

                else if (message.getType() == MessageTypes.SHUTDOWN)
                {
                    /* If user sends SHUTDOWN message, remove user from and change nextNode for the Predecessor
                     *
                     * closing the streams and giving a prompt/debugging info to the client and on receiver too.
                     *
                     * Finally, exit the client (from client side)
                     */

                    // Works similar to LEAVE, but terminates at the end.

                    try
                    {
                        if (!(isOriginalSender))
                        {
                            if (isForwarderLeftNode)
                                forwardToRightNode(message);

                            if (isForwarderRightNode)
                                forwardToLeftNode(message);

                            System.out.println(message.getOriginalSender(message).getName() + " has shut down!");

                        }

                        if (isLeftNode)
                        {
                            currentClient.getCurrentNode().setRightNodeInputStream(null);
                            currentClient.getCurrentNode().setRightNodeOutputStream(null);

                            receiverInputStream.close();
                            receiverOutputStream.close();

                            rightNode = new NodeInfo(message.getOriginalSender(message).getRightNode(message).getIP(),
                                    message.getOriginalSender(message).getRightNode(message).getPort(),
                                    message.getOriginalSender(message).getRightNode(message).getName());

                            currentClient.setRightNode(rightNode);
                            currentClient.setRightSocket(new Socket(currentClient.getRightNode().getIP(), currentClient.getRightNode().getPort()));

                            currentClient.setClientRightOutputStream(new ObjectOutputStream(currentClient.getRightSocket().getOutputStream()));
                            currentClient.setClientRightInputStream(new ObjectInputStream(currentClient.getRightSocket().getInputStream()));

                        }

                        if (isRightNode)
                        {
                            currentClient.getCurrentNode().setLeftNodeInputStream(null);
                            currentClient.getCurrentNode().setLeftNodeOutputStream(null);

                            receiverInputStream.close();
                            receiverOutputStream.close();

                            leftNode = new NodeInfo(message.getOriginalSender(message).getLeftNode(message).getIP(),
                                    message.getOriginalSender(message).getLeftNode(message).getPort(),
                                    message.getOriginalSender(message).getLeftNode(message).getName());

                            currentClient.setLeftNode(leftNode);
                            currentClient.setLeftSocket(new Socket(currentClient.getLeftNode().getIP(), currentClient.getLeftNode().getPort()));

                            currentClient.setClientLeftOutputStream(new ObjectOutputStream(currentClient.getLeftSocket().getOutputStream()));
                            currentClient.setClientLeftInputStream(new ObjectInputStream(currentClient.getLeftSocket().getInputStream()));

                        }
                    } catch (IOException e)
                    {
//                        e.printStackTrace();
                    }
                }
                else if (message.getType() == MessageTypes.SHUTDOWN_ALL)
                {
                    // If user sends SHUTDOWN ALL, terminate all connections and shut down the receiver
                    /*
                     * posting a message to all chat-connected clients and on serves as well.
                     */

                    System.out.println("Client is shutting down...");
                    if (currentClient.getLeftNode() != null)
                        forwardToLeftNode(message);

                    if (currentClient.getRightNode() != null)
                        forwardToRightNode(message);

                    System.exit(0);
                }

                if ((message.getType()) != 6)   // Do not need to print for MessageType CHANGE_LEFT_NODE
                {
                    if ((currentClient.getLeftNode()) != null)
                    {
                        System.out.println("Left Node = " + currentClient.getLeftNode().getName());
                    }
                    else
                    {
                        System.out.println("Left Node = NULL");
                    }
                    if ((currentClient.getRightNode()) != null)
                    {
                        System.out.println("Right Node = " + currentClient.getRightNode().getName());
                    }
                    else
                    {
                        System.out.println("Right Node = NULL");
                    }
                }
            }
        } catch (Exception e)
        {
            //e.printStackTrace();
            try
            {
                // closing resources
                this.receiverInputStream.close();
                this.receiverOutputStream.close();

                this.rightInputStream.close();
                this.rightOutputStream.close();

                this.leftInputStream.close();
                this.leftOutputStream.close();

            } catch (Exception ex)
            {
                //ex.printStackTrace();
            }
        }

    }

    public void forwardMessage(Message message)
    {
        {
            message.setForwarder(currentClient.getCurrentNode());
            forwardToLeftNode(message);
            forwardToRightNode(message);
        }
    }

    public void forwardToLeftNode(Message message)
    {
        if (currentClient.getLeftNode() != null)
        {
            try
            {
                message.setForwarder(currentClient.getCurrentNode());
                currentClient.getClientLeftOutputStream().writeObject(message);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void forwardToRightNode(Message message)
    {
        if (currentClient.getRightNode() != null)
        {
            try
            {
                message.setForwarder(currentClient.getCurrentNode());
                currentClient.getClientRightOutputStream().writeObject(message);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
