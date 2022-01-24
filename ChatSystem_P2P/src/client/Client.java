package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import model.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new Client();
    }

    private NodeInfo readConnectionInfo() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("properties.txt"));
            String[] parts = scanner.nextLine().split(" ");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            return new NodeInfo(ip, port, name);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
        return null;
    }

}
