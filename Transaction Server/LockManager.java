import com.oracle.tools.packager.Log;

import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class LockManager {
	
	private LockManager mySharedObj;
	private String myThreadName;
	private double mySharedVariable;
	private boolean accessing=false; // true a thread has a lock, false otherwise
	private int threadsWaiting=0; // number of waiting writers
public Account account1;
public Account account2;
public Account account3;
public Account account4;
public ArrayList<Account> accountArrayList=new ArrayList<>();
Logger logger;
// Constructor	
	
	LockManager(double SharedVariable) {
		mySharedVariable = SharedVariable;
		logger= Logger.getLogger(getClass().getName());
		try {
			FileHandler fileHandler=new FileHandler("log.log");
			logger.addHandler(fileHandler);

		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

//Attempt to aquire a lock
	
	  public synchronized void acquireLock() throws InterruptedException{
	        Thread me = Thread.currentThread(); // get a ref to the current thread

		  logger.info(me.getName()+" is attempting to acquire a lock!");

	        System.out.println(me.getName()+" is attempting to acquire a lock!");
	        ++threadsWaiting;
		    while (accessing) {  // while someone else is accessing or threadsWaiting > 0
				logger.info(me.getName()+"waiting to get a lock as someone else is accessing...");

				System.out.println(me.getName()+" waiting to get a lock as someone else is accessing...");
		      //wait for the lock to be released - see releaseLock() below
		      wait();
		    }
		    // nobody has got a lock so get one
		    --threadsWaiting;
		    accessing = true;
		    System.out.println(me.getName()+" got a lock!"); 
		  }

		  // Releases a lock to when a thread is finished
		  
		  public synchronized void releaseLock() {
			  //release the lock and tell everyone
		      accessing = false;
		      notifyAll();
		      Thread me = Thread.currentThread(); // get a ref to the current thread
		      System.out.println(me.getName()+" released a lock!");
		  }
	
	
    /* The processInput method */

	public synchronized String processInput(String myThreadName, String theInput,String secondArg,String thirdArg,Account account) {
    	return new AccountManager(accountArrayList).processInput(myThreadName,theInput,secondArg,thirdArg,account);
		//return new AccountManager(account1,account2,account3,account4).processInput(myThreadName,theInput,secondArg,thirdArg,account);
    	}	


}

