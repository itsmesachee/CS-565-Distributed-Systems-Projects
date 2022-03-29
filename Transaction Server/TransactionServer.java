import java.io.IOException;
import java.net.ServerSocket;


public class TransactionServer {
  public static void main(String[] args) throws IOException {

	ServerSocket ActionServerSocket = null;
    boolean listening = true;
    String ActionServerName = "ActionServer";
    int ActionServerNumber = 4545;
    
    double SharedVariable = 100;

    //Create the shared object in the global scope...
    
    LockManager ourLockManagerObject = new LockManager(SharedVariable);
        
    // Make the server socket

    try {
      ActionServerSocket = new ServerSocket(ActionServerNumber);
    } catch (IOException e) {
      System.err.println("Could not start " + ActionServerName + " specified port.");
      System.exit(-1);
    }
    System.out.println(ActionServerName + " started");

    //Got to do this in the correct order with only four clients!  Can automate this...
    
    while (listening){
      new ActionServerThread(ActionServerSocket.accept(), "ActionServerThread1", ourLockManagerObject).start();
      new ActionServerThread(ActionServerSocket.accept(), "ActionServerThread2", ourLockManagerObject).start();
      new ActionServerThread(ActionServerSocket.accept(), "ActionServerThread3", ourLockManagerObject).start();
      new ActionServerThread(ActionServerSocket.accept(), "ActionServerThread4", ourLockManagerObject).start();
      System.out.println("New " + ActionServerName + " thread started.");
    }
    ActionServerSocket.close();
  }
}