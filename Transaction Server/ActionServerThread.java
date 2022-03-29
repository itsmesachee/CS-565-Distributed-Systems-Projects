import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ActionServerThread extends Thread {

	
  private Socket actionSocket = null;
  private LockManager myLockManagerObject;
  private String myActionServerThreadName;
  private double mySharedVariable;
   Account account;
  //Setup the thread
  	public ActionServerThread(Socket actionSocket, String ActionServerThreadName, LockManager SharedObject) {
	
//	  super(ActionServerThreadName);
	  this.actionSocket = actionSocket;
	  myLockManagerObject = SharedObject;
	  myActionServerThreadName = ActionServerThreadName;
	}

  public void run() {
    try {
      System.out.println(myActionServerThreadName + "initialising.");
      PrintWriter out = new PrintWriter(actionSocket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(actionSocket.getInputStream()));
      String inputLine, outputLine;
String accountId=in.readLine();
account=new Account(accountId,10);

myLockManagerObject.accountArrayList.add(account);
        if(accountId.equals("1"))
            myLockManagerObject.account1=account;
        else if(accountId.equals("2"))
            myLockManagerObject.account2=account;
        else  if(accountId.equals("3"))
            myLockManagerObject.account3=account;
        else if(accountId.equals("4"))
            myLockManagerObject.account4=account;



        System.out.println("request received from AccountID:"+accountId);
      while ((inputLine = in.readLine()) != null) {
    	  // Get a lock first

    	  try { 
    		  myLockManagerObject.acquireLock();
    		  outputLine = myLockManagerObject.processInput(myActionServerThreadName, inputLine,in.readLine(),in.readLine(),account);
    		  out.println(outputLine);
    		  myLockManagerObject.releaseLock();
    	  } 
    	  catch(InterruptedException e) {
    		  System.err.println("Failed to get lock when reading:"+e);
    	  }
      }

       out.close();
       in.close();
       actionSocket.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}