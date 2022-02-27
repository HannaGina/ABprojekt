
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    public ABServer(){
        try {
            server = new ServerSocket(port);
            client = server.accept();
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out.println("hello");
            Table t;
            in.read(t);
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ABServer a = new ABServer();
    }
}
