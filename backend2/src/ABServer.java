
import javafx.scene.control.Tab;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//Gyorgy Viktor gvim2021 522/1-es csoport
public class ABServer {

    ServerSocket server;
    Socket client;
    String ip = "127.0.0.1";
    int port = 2500;
    BufferedReader in;
    PrintWriter out;
    ObjectInputStream objectInputStream;

    public ABServer(){
        try {
            server = new ServerSocket(port);
            client = server.accept();
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            objectInputStream = new ObjectInputStream(client.getInputStream());
            //out.println("hello");

            Table table =(Table) objectInputStream.readObject();
            System.out.println(table);
            in.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ABServer a = new ABServer();
    }
}
