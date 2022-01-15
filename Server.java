import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
private ServerSocket serverSocket;

public Server(ServerSocket serverSocket) {
this.serverSocket=serverSocket;
}

public void startServer(){
	try {
		while(!serverSocket.isClosed()){
			Socket socket = serverSocket.accept();
			System.out.println("New Client has joined that chat!");
			
			ClientHandler clientHandler = new ClientHandler(socket);
			
			Thread thread1 = new Thread(clientHandler);
			
			thread1.start();
			
		}
	}
	catch(IOException e) {
		
	}
}
public void closeServerSocket() {
	try {
		if(serverSocket!=null) {
			serverSocket.close();
		}
	}catch(IOException e) {
		e.printStackTrace();
	}
}

public static void main () throws IOException{
	ServerSocket serverSocket = new ServerSocket(1234);
	Server server = new  Server(serverSocket);
	
	server.startServer();
	
}
}
