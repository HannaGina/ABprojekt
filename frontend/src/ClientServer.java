import com.sun.corba.se.impl.orbutil.ObjectWriter;
import hello.JSONArray;
import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;
import javafx.scene.control.Tab;
import org.w3c.dom.Attr;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientServer {
    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private OutputStreamWriter out;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String ip;
    private int port;

    public ClientServer() {
        //ip = "192.168.0.106";
        ip = "192.168.43.158";
        port = 2500;
    }

    public String send(String message) {
        try {
            clientSocket = new Socket(ip,port);
            out = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.write(message);
            out.flush();

            String answer = bufferedReader.readLine();

            out.close();
            bufferedReader.close();
            return answer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        //ClientServer clientServer = new ClientServer();
        DBFrame dbFrame = new DBFrame(new ClientServer());
    }
}