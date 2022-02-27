import com.sun.corba.se.impl.orbutil.ObjectWriter;
import javafx.scene.control.Tab;
import org.w3c.dom.Attr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientServer {
    private Socket clientSocket;
    private PrintWriter printWriter;
    private ObjectWriter objectWriter;
    private BufferedReader bufferedReader;
    private String ip;
    private int port;

    public ClientServer() {
        ip = "192.168.0.104";
        port = 2500;

        try {
            clientSocket = new Socket(ip,port);
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
            System.out.println(bufferedReader.readLine());
            printWriter.print(table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientServer clientServer = new ClientServer();
    }
}