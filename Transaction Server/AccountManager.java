import java.util.ArrayList;

public class AccountManager {
    private Account account1;
    private Account account2;
    private Account account3;
    private Account account4;
    private ArrayList<Account> accountArrayList=new ArrayList<>();

    public AccountManager(Account account1, Account account2, Account account3, Account account4) {
        this.account1 = account1;
        this.account2 = account2;
        this.account3 = account3;
        this.account4 = account4;
    }

    public AccountManager(ArrayList<Account> accountArrayList) {
        this.accountArrayList = accountArrayList;
    }
    public void addAccount(Account account)
    {
        accountArrayList.add(account);
    }

    public synchronized String processInput(String myThreadName, String theInput, String secondArg, String thirdArg, Account account) {
        System.out.println(myThreadName + " received "+ theInput);
        String theOutput = null;
        // Check what the client said
        if (theInput.equalsIgnoreCase("ADD")) {

            account.amount+=Integer.parseInt(secondArg);

            accountArrayList.remove((Integer.parseInt(account.accountId)-1));
            accountArrayList.add((Integer.parseInt(account.accountId)-1),account);
            System.out.println(myThreadName + " New Balance" + account.amount);

        }

        else if (theInput.equalsIgnoreCase("SUB")) {
            //Correct request

    				/*  Add 20 to the variable
    					multiply it by 5
    					divide by 3.
    				 */

            account.amount-=Integer.parseInt(secondArg);
accountArrayList.remove((Integer.parseInt(account.accountId)-1));
accountArrayList.add((Integer.parseInt(account.accountId)-1),account);
            System.out.println(myThreadName + " New Balance" + account.amount);


        }

        else if (theInput.equalsIgnoreCase("BALANCE")) {

            theOutput = account.amount+"";


        }

        else if (theInput.equalsIgnoreCase("SEND")) {

            String recepitID=secondArg;
            int amountToTransfer=Integer.parseInt(thirdArg);

            //perform the transection
            double remainingBalance=account.amount-amountToTransfer;

            Account ac=accountArrayList.get(Integer.parseInt(recepitID)-1);
            ac.amount+=amountToTransfer;
            account.amount = remainingBalance;
            accountArrayList.remove(Integer.parseInt(recepitID)-1);
            accountArrayList.add(Integer.parseInt(recepitID)-1,ac);

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
