package model;

import client.Client;
import receiver.Receiver;

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
     *  - request sockets, send messages, shutdown etc.
     *
     * */

    private int type;
    private Object content;
    private NodeInfo originalSender;

    public Message(int type, Object content, NodeInfo currentClient)
    {
        this.type = type;
        this.content = content;
        this.originalSender = currentClient;
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
        originalSender.setNextNode(currentClient.getNextNode());

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
                    if (currentClient.getSocket() != null)
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
        // Request socket connection to the known IP and port - which will become the next node of current client.
        currentClient.setNextNode(new NodeInfo(parts1, parts2, name));
        currentClient.setSocket(new Socket(currentClient.getNextNode().getIP(), currentClient.getNextNode().getPort()));
        currentClient.setObjectOutputStream(new ObjectOutputStream(currentClient.getSocket().getOutputStream()));
        currentClient.setObjectInputStream(new ObjectInputStream(currentClient.getSocket().getInputStream()));

        // Set next node and self
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        currentClient.setNextNode(new NodeInfo(parts1, parts2, name));
        originalSender.setNextNode(currentClient.getNextNode());
        // Displaying debugging information for client's joining into chat in receiver

        Message msg = new Message(MessageTypes.JOIN, currentClient.getNextNode(), originalSender);
        currentClient.getObjectOutputStream().writeObject(msg);
        System.out.println("Chat Group Joined!");


        //creating a thread for the client with our required functionality with respect to the commands
        Thread thread1 = new MessageHandler(currentClient);
        thread1.start();
    }

    public void joinAsFirstNode(String name, Client currentClient) throws IOException
    {
        // The first node joining the chat has no other clients to connect to.
        // It connects to itself forming a ring with only one node.
        // Therefore, the node itself is its nextnode.

        joinRegular(currentClient.getIP(), currentClient.getPort(), name, currentClient);
    }

    public void leave(String name, Client currentClient) throws IOException
    {
        if (currentClient.getSocket() != null)
        {
            // If first word of string is LEAVE, leave chat without terminating the client
            /*
             * this code block is for the "LEAVE" command that states a particular client which executes
             * this command shall leave the chat
             */
            originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
            originalSender.setNextNode(currentClient.getNextNode());

            // Send a message that you are leaving to next node
            Message msg = new Message(MessageTypes.LEAVE, currentClient.getNextNode(), originalSender);
            currentClient.getObjectOutputStream().writeObject(msg);

            // Close socket connections
            currentClient.getSocket().close();
            currentClient.setSocket(null);
            currentClient.setObjectInputStream(null);
            currentClient.setObjectOutputStream(null);

            System.out.println("You have left chat group!");
        }
        else
        {
            System.out.println("You have not joined the chat!");
        }

    }

    public void sendNote(String text, String name, Client currentClient)
    {
        // Regular messages - forward to next node.
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setNextNode(currentClient.getNextNode());

        Message msg = new Message(MessageTypes.NOTE, name + ": " + text, originalSender);
        try
        {
            currentClient.getObjectOutputStream().writeObject(msg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void shutdown(String name, Client currentClient) throws IOException, InterruptedException
    {
        // If first word of string is SHUTDOWN, leave chat without terminating the client
        /*
         * this code block is for the "SHUTDOWN" command that states a particular client which executes
         * this command shall leave the chat and terminate itself.
         */

        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setNextNode(currentClient.getNextNode());

        if (currentClient.getSocket() != null)
        {
            Message msg = new Message(MessageTypes.SHUTDOWN, currentClient.getNextNode(), originalSender);
            currentClient.getObjectOutputStream().writeObject(msg);
            sleep(50);  // Used here since closing immediately disrupts the sending of previous message.
            currentClient.getSocket().close();
        }
        System.exit(0);         // Terminates the client
    }

    public void shutdownAll(String name, Client currentClient) throws IOException
    {
        // Shutdown all clients and the receiver
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setNextNode(currentClient.getNextNode());

        /*
         * This is a deadman switch to our receiver and to all the clients connected to the chat/receiver.
         * As if any client sends this command, all the connected clients and receiver it to be terminated/stopped at that instant.
         */
        if (currentClient.getSocket() != null)
        {
            Message msg = new Message(MessageTypes.SHUTDOWN_ALL, null, originalSender);
            currentClient.getObjectOutputStream().writeObject(msg);
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

                if (currentClient.getSocket() != null)
                {
                    Message msg1 = (Message) currentClient.getObjectInputStream().readObject();
                    if (msg1.getType() == MessageTypes.NOTE)
                    {
                        System.out.println(msg1.getContent());
                    }
                    else if (msg1.getType() == MessageTypes.SHUTDOWN_ALL)
                    {
                        System.out.println("Client is closed!");
                        currentClient.getSocket().close();
                        System.exit(0);
                    }
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                //e.printStackTrace();
            }
        }
    }
}
