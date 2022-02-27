package model;

import client.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Message implements MessageTypes,Serializable {

    private int type;
    private Object content;
    private NodeInfo originalSender;

    //Constructor to initialize an instance with the user input and the sender client node's info as well.
    public Message(int type, Object content, NodeInfo currentClient) {
        this.type = type;
        this.content = content;
        this.originalSender = currentClient;
    }

    // Getter and Setter methods for ease of access & manipulation of Message Class Attributes
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
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
    public String toString() {
        return content.toString();
    }

    public Message()
    {
    }
    
//Takes the user input and classifes for the command and calls the respective function with the argument
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
                if ((parts[0].equals("JOIN"))&&(size == 1))
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

                    leave(name,currentClient);
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
                        // This call is made, when a client isn't connected to a chat and inputs his commands/text.
                        displayNotInChat();
                    }

                }

            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

    }

    //This is the standard join method, a client joining  a chat ring that has already been established and is running.
    
    public void joinRegular(String parts1, int parts2, String name, Client currentClient) throws IOException
    {
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

        Thread thread1 = new Thread(() ->
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
                } catch (IOException | ClassNotFoundException e)
                {
                    //e.printStackTrace();
                }
            }
        });
        thread1.start();
    }

    //When there is no exisiting chat ring, this function establishes the chat with this application.
    
    public void joinAsFirstNode(String name, Client currentClient) throws IOException
    {
        joinRegular(currentClient.getIP(), currentClient.getPort(), name, currentClient);
    }
      /*
    This method is used to leave the chat by any particular client 
    */
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

            Message msg = new Message(MessageTypes.LEAVE, currentClient.getNextNode(), originalSender);
            currentClient.getObjectOutputStream().writeObject(msg);
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

    /*
    This method is used for sending note to all the clients connected in the chat
    */
    public void sendNote(String text, String name, Client currentClient)
    {
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setNextNode(currentClient.getNextNode());

        Message msg = new Message(MessageTypes.NOTE, name + ": " + text, originalSender);
        try
        {
            currentClient.getObjectOutputStream().writeObject(msg);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /*
    This method is used to terminate a specific client from the connection
    */
    public void shutdown(String name, Client currentClient) throws IOException, InterruptedException
    {
        // Terminates the client which sent this message
        originalSender = (new NodeInfo(currentClient.getIP(), currentClient.getPort(), name));
        originalSender.setNextNode(currentClient.getNextNode());

        if (currentClient.getSocket() != null)
        {
            Message msg = new Message(MessageTypes.SHUTDOWN, currentClient.getNextNode(), originalSender);
            currentClient.getObjectOutputStream().writeObject(msg);
            sleep(50);
            currentClient.getSocket().close();
        }
        System.exit(0);
    }
    /*
    This method is used to terminate all of the clients from the connection
    */
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
    /*
    This method is used to display the user that he has not joined the chat yet
    */
    public void displayNotInChat()
    {
        System.out.println("You have not joined the chat!");
    }


}
