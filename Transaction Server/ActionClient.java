import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class ActionClient {
    public static void main(String[] args) throws IOException {


        if(args.length==0)
        {
            System.out.println("PLease Provide run time argument Example--> java ActionClient 1  ");
            System.exit(0);
        }


        String idd=args[0];

        Socket ActionClientSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        int ActionSocketNumber = 4545;
        String ActionServerName = "localhost";
        String ActionClientID = "ActionClient1";

        try {
            ActionClientSocket = new Socket(ActionServerName, ActionSocketNumber);
            out = new PrintWriter(ActionClientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(ActionClientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost ");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: "+ ActionSocketNumber);
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromServer;
        String fromUser;

        System.out.println("Initialised " + ActionClientID + " client and IO connections");

        out.println(idd);


        while (true) {
            System.out.println("Choose an option: ");
            System.out.println("1.Add Money,\t 2.Withdraw Money \t 3.Transfer Money \t 4.Show Balance");
            String choice=stdIn.readLine();
            if(choice.trim().equalsIgnoreCase("1"))
            {
                System.out.println("Enter amount to add:");
                int amount=Integer.parseInt(stdIn.readLine());
                System.out.println(ActionClientID + " sending ADD Money to ActionServer");
                out.println("ADD");
                out.flush();
                out.println(amount);
                out.flush();
                out.println("0");
                out.flush();
            }
            else
            if(choice.trim().equalsIgnoreCase("2"))
            {
                System.out.println("Enter amount to add:");
                int amount=Integer.parseInt(stdIn.readLine());

                System.out.println(ActionClientID + " sending Withdraw to ActionServer");
                out.println("SUB");
                out.flush();
                out.println(amount);
                out.flush();
                out.println("0");
                out.flush();
            }
            else   if(choice.trim().equalsIgnoreCase("3"))
            {
                System.out.println("Enter account id to transfer:");
                int id=Integer.parseInt(stdIn.readLine());
                System.out.println("Enter amount to transfer:");
                int amount=Integer.parseInt(stdIn.readLine());
                System.out.println(ActionClientID + " sending Money  to Account ID:"+id);
                out.println("SEND");
                out.flush();
                out.println(id);
                out.flush();
                out.println(amount);
                out.flush();
            }
            else     if(choice.trim().equalsIgnoreCase("4"))
            {

                System.out.println("Sending Balance inquiry to ActionServer");
                out.println("BALANCE");
                out.flush();
                out.println("0");
                out.flush();
                out.println("0");
                out.flush();
                String bal=in.readLine();
                System.out.println("Your Balance is "+bal);
                continue;

            }
            else
            {
                System.out.println("Invalid Choice");
                continue;
            }

            fromServer = in.readLine();
            System.out.println(ActionClientID + " received " + fromServer + " from ActionServer");
        }


    }
}
