import com.sun.corba.se.impl.orbutil.ObjectWriter;
import javafx.scene.control.Tab;
import org.w3c.dom.Attr;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientServer {
    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String ip;
    private int port;

    public ClientServer() {
        //ip = "192.168.0.104";
        ip = "127.0.0.1";
        port = 2500;

        try {
            clientSocket = new Socket(ip,port);
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Attribute attribute = new Attribute("cnp", "int", true, false, true, true);
        List<Attribute>attributes = new ArrayList<>();
        attributes.add(attribute);
        Table table = new Table("szemely",attributes);

        //printWriter.println("valami");
        try {
            //System.out.println(bufferedReader.readLine());
            objectOutputStream.writeObject(table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientServer clientServer = new ClientServer();
    }
}