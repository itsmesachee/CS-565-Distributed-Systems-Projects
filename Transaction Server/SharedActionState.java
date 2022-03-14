import java.net.*;
import java.io.*;

public class SharedActionState{
	
	private SharedActionState mySharedObj;
	private String myThreadName;
	private double mySharedVariable;
	private boolean accessing=false; // true a thread has a lock, false otherwise
	private int threadsWaiting=0; // number of waiting writers
private Account account1;
private Account account2;
private Account account3;
private Account account4;
// Constructor	
	
	SharedActionState(double SharedVariable) {
		mySharedVariable = SharedVariable;
	}

//Attempt to aquire a lock
	
	  public synchronized void acquireLock() throws InterruptedException{
	        Thread me = Thread.currentThread(); // get a ref to the current thread
	        System.out.println(me.getName()+" is attempting to acquire a lock!");	
	        ++threadsWaiting;
		    while (accessing) {  // while someone else is accessing or threadsWaiting > 0
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
    		System.out.println(myThreadName + " received "+ theInput);
    		String theOutput = null;
    		// Check what the client said
    		if (theInput.equalsIgnoreCase("ADD")) {
    			//Correct request

    				/*  Add 20 to the variable
    					multiply it by 5
    					divide by 3.
    				 */
					account.amount+=Integer.parseInt(secondArg);

					//updating account

    				mySharedVariable = mySharedVariable + 20;
       				mySharedVariable = mySharedVariable * 5;
       				mySharedVariable = mySharedVariable / 3;
					System.out.println(myThreadName + " New Balance" + account.amount);
					   System.out.println(myThreadName + " made the SharedVariable " + mySharedVariable);
    				theOutput = "Do action completed.  Shared Variable now = " + mySharedVariable;

    		}

    	else if (theInput.equalsIgnoreCase("SUB")) {
				//Correct request

    				/*  Add 20 to the variable
    					multiply it by 5
    					divide by 3.
    				 */
					account.amount-=Integer.parseInt(secondArg);
					mySharedVariable = mySharedVariable + 20;
					mySharedVariable = mySharedVariable * 5;
					mySharedVariable = mySharedVariable / 3;
					System.out.println(myThreadName + " New Balance" + account.amount);
					System.out.println(myThreadName + " made the SharedVariable " + mySharedVariable);
					theOutput = "Do action completed.  Shared Variable now = " + mySharedVariable;


			}

			else if (theInput.equalsIgnoreCase("BALANCE")) {


				mySharedVariable = mySharedVariable + 20;
				mySharedVariable = mySharedVariable * 5;
				mySharedVariable = mySharedVariable / 3;

				System.out.println(myThreadName + " made the SharedVariable " + mySharedVariable);
				theOutput = account.amount+"";


			}

			else if (theInput.equalsIgnoreCase("SEND")) {

String recepitID=secondArg;
int amountToTransfer=Integer.parseInt(thirdArg);

				//perform the transection
				double remainingBalance=account.amount-amountToTransfer;


				if(recepitID.equals("1"))
				{

					if(account1!=null) {
						account1.amount += amountToTransfer;
						account.amount = remainingBalance;
					}
					else {
						theOutput = "No Such account Exist..";
					return theOutput;
					}
				}
				else if(recepitID.equals("2"))
				{
					if(account1!=null) {
						account2.amount += amountToTransfer;
						account.amount = remainingBalance;
					}
					else {
						theOutput = "No Such account Exist..";
						return theOutput;
					}
				}
				else if(recepitID.equals("3"))
				{
					if(account3!=null) {
						account3.amount += amountToTransfer;
						account.amount = remainingBalance;
					}
					else {
						theOutput = "No Such account Exist..";
						return theOutput;
					}
				}
				else if(recepitID.equals("4"))
				{
					if(account4!=null) {
						account4.amount += amountToTransfer;
						account.amount = remainingBalance;
					}
					else {
						theOutput = "No Such account Exist..";
						return theOutput;
					}
					}
				else {
					theOutput = "No Such account Exist..";
					return theOutput;
				}

				mySharedVariable = mySharedVariable + 20;
				mySharedVariable = mySharedVariable * 5;
				mySharedVariable = mySharedVariable / 3;

				System.out.println(myThreadName + " made the SharedVariable " + mySharedVariable);
				theOutput = account.amount+"";


			}



			else { //incorrect request
    			theOutput = myThreadName + " received incorrect request - only understand \"Do my action!\"";
		
    		}

//update account
		if (account.accountId.equals("1"))
			account1=account;
		else if(account.accountId.equals("2"))
			account2=account;
		else if(account.accountId.equals("3"))
			account3=account;
		else if(account.accountId.equals("4"))
			account4=account;

 
     		//Return the output message to the ActionServer
    		System.out.println(theOutput);
    		return theOutput;
    	}	
}

