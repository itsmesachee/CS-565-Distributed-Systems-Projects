package model;

import client.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Message implements MessageTypes, Serializable
{
    /*
    *   This class is used to read user input and act accordingly
    * - request sockets, send messages, shutdown etc.
    *
    * */
    private int type;
    private Object content;
    private NodeInfo originalSender;
    private NodeInfo forwarder;
    private NodeInfo futureRightNode;


    public Message(int type, Object content, NodeInfo currentClient, NodeInfo forwarder)
    {
        this.type = type;
        this.content = content;
        this.originalSender = currentClient;
        this.forwarder = forwarder;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public Object getContent()
    {
        return content;
    }

    public void setContent(Object content)
    {
        this.content = content;
    }

    public NodeInfo getOriginalSender(Message message)
    {
        return message.originalSender;
    }

    public void setOriginalSender(NodeInfo originalSender)
    {
        this.originalSender = originalSender;
    }

    public NodeInfo getForwarder(Message message)
    {
        return message.forwarder;
    }

    public void setForwarder(NodeInfo forwarder)
    {
        this.forwarder = forwarder;
    }

    public NodeInfo getFutureRightNode(Message message)
    {
        return message.futureRightNode;
    }

    public void setFutureRightNode(NodeInfo futureRightNode)
    {
        this.futureRightNode = futureRightNode;
    }


    @Override
    public String toString()
    {
        return content.toString();
    }

    public Message()
    {
    }

    public void readMessage(String name, Client currentClient) throws InterruptedException
    {
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setLeftNode(currentClient.getLeftNode());
        originalSender.setRightNode(currentClient.getRightNode());

        while (true)
        {
            try
            {
                Scanner sc = new Scanner(System.in);

                String text = sc.nextLine();

                String[] parts = text.split(" "); // Split the message to identify any commands
                int size = parts.length;
                // takes in the clients name followed by command

                // First word of string is JOIN
                if ((parts[0].equals("JOIN")) && (size == 1))
                {
                    joinAsFirstNode(name, currentClient);
                }
                else if (parts[0].equals("JOIN"))
                {
                    //Read from file
                    // Parse the 2nd and 3rd parts of JOIN message to identify IP and port
                    // recognize the JOIN command and establish connection with name, IP, port and create streams
                    joinRegular(parts[1], Integer.parseInt(parts[2]), name, currentClient);
                }
                else if (parts[0].equals("LEAVE"))
                {
                    // If first word of string is LEAVE, leave chat without terminating the client
                    /*
                     * this code block is for the "LEAVE" command that states a particular client which executes
                     * this command shall leave the chat
                     */

                    leave(name, currentClient);
                }
                else if (parts[0].equals("SHUTDOWN_ALL"))
                {
                    // Shutdown all clients and the receiver

                    /*
                     * This is a deadman switch to our receiver and to all the clients connected to the chat/receiver.
                     * As if any client sends this command, all the connected clients and receiver it to be terminated/stopped at that instant.
                     */
                    shutdownAll(name, currentClient);
                }
                else if (parts[0].equals("SHUTDOWN"))
                {
                    // Terminates the client which sent this message
                    shutdown(name, currentClient);
                }
                else
                {
                    // If client tries to leave without joining first, print this message
                    if ((currentClient.getLeftSocket() != null) || (currentClient.getRightSocket() != null))
                    {
                        /*
                         * Here's the key part of our chat system that handles the messages/command sent by multiple clients through receiver.
                         */

                        sendNote(text, name, currentClient);
                    }
                    else
                    {
                        displayNotInChat();
                    }
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void joinRegular(String parts1, int parts2, String name, Client currentClient) throws IOException
    {
        currentClient.setLeftNode(new NodeInfo(parts1, parts2));

        try
        {
            // Request socket connection to the known IP and port - which will become the left node of current client.
            currentClient.setLeftSocket(new Socket(currentClient.getLeftNode().getIP(), currentClient.getLeftNode().getPort()));

            currentClient.setClientLeftOutputStream(new ObjectOutputStream(currentClient.getLeftSocket().getOutputStream()));
            currentClient.setClientLeftInputStream(new ObjectInputStream(currentClient.getLeftSocket().getInputStream()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Set next node and self
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setLeftNode(currentClient.getLeftNode());
        // Displaying debugging information for client's joining into chat in receiver

        Message msg = new Message(MessageTypes.JOIN, currentClient.getLeftNode(), originalSender, originalSender);
        currentClient.getClientLeftOutputStream().writeObject(msg);
        System.out.println("Chat Group Joined!");

        //creating a thread for the client with our required functionality with respect to the commands
        Thread thread1 = new MessageHandler(currentClient);
        thread1.start();
    }

    public void joinAsFirstNode(String name, Client currentClient) throws IOException
    {
        // The first node joining the chat has no other clients to connect to.
        // Therefore, no special operations are necessary.
        currentClient.setLeftNode(null);
        currentClient.setRightNode(null);
        System.out.println("You are the first to join this chat!");
    }

    public void leave(String name, Client currentClient) throws IOException
    {
        if ((currentClient.getLeftSocket() != null) || (currentClient.getRightSocket() != null))
        {
            // If first word of string is LEAVE, leave chat without terminating the client
            /*
             * this code block is for the "LEAVE" command that states a particular client which executes
             * this command shall leave the chat
             */
            originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
            originalSender.setLeftNode(currentClient.getLeftNode());
            originalSender.setRightNode(currentClient.getRightNode());

            Message msg = new Message(MessageTypes.LEAVE, currentClient.getLeftNode(), originalSender, originalSender);

            // Send a message that you are leaving to neighbor nodes
            if ((currentClient.getLeftNode()) != null)
            {
                currentClient.getClientLeftOutputStream().writeObject(msg);
            }

            if ((currentClient.getRightNode()) != null)
            {
                currentClient.getClientRightOutputStream().writeObject(msg);
            }

            // Close socket connections
            if ((currentClient.getLeftSocket()) != null)
            {
                currentClient.getLeftSocket().close();
                currentClient.setLeftSocket(null);
                currentClient.setClientLeftInputStream(null);
                currentClient.setClientLeftOutputStream(null);
                currentClient.setLeftNode(null);
            }

            if ((currentClient.getRightSocket()) != null)
            {
                currentClient.getRightSocket().close();
                currentClient.setRightSocket(null);
                currentClient.setClientRightInputStream(null);
                currentClient.setClientRightOutputStream(null);
                currentClient.setRightNode(null);
            }

            System.out.println("You have left chat group!");
        }
        else
        {
            System.out.println("You have not joined the chat!");
        }

    }

    public void sendNote(String text, String name, Client currentClient)
    {
        // Regular messages - forward to left and right nodes.
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setLeftNode(currentClient.getLeftNode());
        originalSender.setRightNode(currentClient.getRightNode());

        Message msg = new Message(MessageTypes.NOTE, name + ": " + text, originalSender, originalSender);
        try
        {
            if ((currentClient.getLeftNode()) != null)
                currentClient.getClientLeftOutputStream().writeObject(msg);

            if ((currentClient.getRightNode()) != null)
                currentClient.getClientRightOutputStream().writeObject(msg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void shutdown(String name, Client currentClient) throws IOException, InterruptedException
    {
        if ((currentClient.getLeftSocket() != null) || (currentClient.getRightSocket() != null))
        {
            // If first word of string is SHUTDOWN, leave chat without terminating the client
            /*
             * this code block is for the "SHUTDOWN" command that states a particular client which executes
             * this command shall leave the chat and terminate itself.
             */

            originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
            originalSender.setLeftNode(currentClient.getLeftNode());
            originalSender.setRightNode(currentClient.getRightNode());

            Message msg = new Message(MessageTypes.SHUTDOWN, currentClient.getLeftNode(), originalSender, originalSender);

            if ((currentClient.getLeftNode()) != null)
            {
                currentClient.getClientLeftOutputStream().writeObject(msg);
            }

            if ((currentClient.getRightNode()) != null)
            {
                currentClient.getClientRightOutputStream().writeObject(msg);
            }

            sleep(50);  // Used here since closing immediately disrupts the sending of previous message.

            currentClient.getLeftSocket().close();
            currentClient.setLeftSocket(null);

            currentClient.setClientLeftInputStream(null);
            currentClient.setClientLeftOutputStream(null);

            currentClient.getRightSocket().close();
            currentClient.setRightSocket(null);

            currentClient.setClientRightInputStream(null);
            currentClient.setClientRightOutputStream(null);
        }
        System.exit(0);         // Terminates the client
    }

    public void shutdownAll(String name, Client currentClient) throws IOException
    {
        // Shutdown all clients and the receiver
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setLeftNode(currentClient.getLeftNode());
        originalSender.setRightNode(currentClient.getRightNode());

        /*
         * This is a deadman switch to our receiver and to all the clients connected to the chat/receiver.
         * As if any client sends this command, all the connected clients and receiver it to be terminated/stopped at that instant.
         */
        if ((currentClient.getLeftSocket() != null) || (currentClient.getRightSocket() != null))
        {
            Message msg = new Message(MessageTypes.SHUTDOWN_ALL, null, originalSender, originalSender);
            currentClient.getClientLeftOutputStream().writeObject(msg);
        }
        else
        {
            System.out.println("You have not joined the chat!");
            System.exit(0);
        }
    }

    public void displayNotInChat()
    {
        System.out.println("You have not joined the chat!");
    }
}

class MessageHandler extends Thread implements Serializable
{
    Client currentClient;

    public MessageHandler(Client currentClient)
    {
        this.currentClient = currentClient;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {


                /*
                 * listen and transfer all messages as TYPE - NOTE to receiver until the input matches
                 * the pre-defined command 'SHUTDOWN ALL'.IF yes, then respective code for that command is executed,
                 * by closing the socket and giving debugging information to user about the status of the receiver.
                 */

                if ((currentClient.getLeftSocket() != null) || (currentClient.getRightSocket() != null))
                {
                    Message msg1 = (Message) currentClient.getClientLeftInputStream().readObject();
                    if (msg1.getType() == MessageTypes.NOTE)
                    {
                        System.out.println(msg1.getContent());
                    }
                    else if (msg1.getType() == MessageTypes.SHUTDOWN_ALL)
                    {
                        System.out.println("Client is closed!");
                        currentClient.getLeftSocket().close();
                        currentClient.getRightSocket().close();
                        System.exit(0);
                    }
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
//                    e.printStackTrace();
            }
        }
    }
}