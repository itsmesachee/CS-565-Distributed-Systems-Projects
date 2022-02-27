package receiver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import client.Client;
import model.Message;
import model.MessageTypes;
import model.NodeInfo;

public class Receiver implements MessageTypes
{

    private ServerSocket serverSocket;

    public Receiver(Client currentClient) throws IOException
    {
        serverSocket = new ServerSocket(currentClient.getPort());
    }

    public void listenToClient(Client currentClient) throws IOException
    {
        while (true)
        {
            Socket s;
            try
            {

                /*
                 * Opening the socket connection and creating streams laying paths for clients to communicate with receiver
                 * and between themselves.
                 */

                s = serverSocket.accept();
                ObjectInputStream receiverInputStream = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream receiverOutputStream = new ObjectOutputStream(s.getOutputStream());

                Thread t = new ClientHandler(s, receiverInputStream, receiverOutputStream, currentClient);
                t.start();

            } catch (IOException e)
            {
//                s.close();
                e.printStackTrace();
            }
        }

    }

    // ClientHandler class
    class ClientHandler extends Thread
    {

        /*
         * This class is specifically used to handle multiple clients and facilitate communication between them.
         */

        final ObjectInputStream receiverInputStream;
        final ObjectOutputStream receiverOutputStream;
        Client currentClient;
        final Socket socket;
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
                while (true)
                {
                    Message message = (Message) receiverInputStream.readObject();
                    /*
                     * Based on the type of message constant which matches to the command sent from a client to the receiver, respective
                     * code block goes lives on matching the condition
                     */
                    boolean isOriginalSender = ((((message.getOriginalSender(message)).getPort()) == (currentClient.getPort()))
                            && (((message.getOriginalSender(message)).getIP()).equals(currentClient.getIP())));
                    boolean isNextNode = (((((message.getOriginalSender(message)).getNextNode()).getPort()) == currentClient.getPort())
                            && ((((message.getOriginalSender(message)).getNextNode()).getIP()).equals((currentClient.getIP()))));
                    boolean isPredecessorJoin = (((((message.getOriginalSender(message)).getNextNode()).getPort()) == ((currentClient.getNextNode()).getPort()))
                            && ((((message.getOriginalSender(message)).getNextNode()).getIP()).equals((currentClient.getNextNode()).getIP())));
                    boolean isPredecessor = (((message.getOriginalSender(message).getPort()) == (currentClient.getNextNode().getPort()))
                            && (((message.getOriginalSender(message).getIP()).equals(currentClient.getNextNode().getIP()))));

                    if (message.getType() == MessageTypes.JOIN)
                    {
                        if (isNextNode)
                        {
                            currentClient.getCurrentNode().setObjectOutputStream(receiverOutputStream);
                        }

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

                                NodeInfo info = (NodeInfo) message.getContent();
                                System.out.println(info.getName() + " has joined!");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }

                        if (!(isOriginalSender || isPredecessorJoin))
                        {
                            NodeInfo info = (NodeInfo) message.getContent();
                            System.out.println(info.getName() + " has joined!");
                            forwardMessage(message);
                        }
                        System.out.println("Next Node = " + currentClient.getNextNode().getName());
                    }
                    else if (message.getType() == MessageTypes.LEAVE)
                    {
                        /* If user sends LEAVE message, remove user from and change nextNode for the Predecessor
                         *
                         * closing the streams and giving a prompt/debugging info to the client and on receiver too.
                         */
                        if ((!(isPredecessor)) && (!isOriginalSender))
                        {
                            NodeInfo info = (NodeInfo) message.getContent();
                            System.out.println(message.getOriginalSender(message).getName() + " has left!");
                            forwardMessage(message);
                            System.out.println("Next Node = " + currentClient.getNextNode().getName());
                        }
                        if (isNextNode)
                        {
                            try
                            {
                                currentClient.getCurrentNode().setObjectOutputStream(null);
                                receiverInputStream.close();
                                receiverOutputStream.close();
                                break;
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            System.out.println("Next Node = " + currentClient.getNextNode().getName());
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

                                NodeInfo info = (NodeInfo) message.getContent();
                                System.out.println(info.getName() + " has left!");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            System.out.println("Next Node = " + currentClient.getNextNode().getName());
                        }
                    }
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
                        System.out.println("Next Node = " + currentClient.getNextNode().getName());
                    }
                    else if (message.getType() == MessageTypes.SHUTDOWN)
                    {
                        /* If user sends SHUTDOWN message, remove user and change nextNode information of predecessor.
                         *
                         * closing the streams and giving a prompt/debugging info to the client and on receiver too.
                         */
                        if ((!(isPredecessor)) && (!isOriginalSender))
                        {
                            NodeInfo info = (NodeInfo) message.getContent();
                            System.out.println(info.getName() + " has shutdown!");
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
                            } catch (IOException e)
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


                                NodeInfo info = (NodeInfo) message.getContent();
                                System.out.println(info.getName() + " has shutdown!");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("Next Node = " + currentClient.getNextNode().getName());
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
                }
            } catch (Exception e)
            {
                //e.printStackTrace();
                try
                {
                    // closing resources
                    this.receiverInputStream.close();
                    this.receiverOutputStream.close();

                } catch (Exception ex)
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
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
