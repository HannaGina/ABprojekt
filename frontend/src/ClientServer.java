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
        ip = "192.168.0.104";
        port = 2500;

        /*try {
            clientSocket = new Socket(ip,port);
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /*JSONObject ja1 = new JSONObject();
        ja1.put("name","cnp");
        ja1.put("type","int");
        ja1.put("primaryKey","true");

        JSONObject ja2 = new JSONObject();
        ja2.put("name","name");
        ja2.put("type","string");
        ja2.put("primaryKey","false");

        JSONArray jattribute = new JSONArray();
        jattribute.add(ja1);
        jattribute.add(ja2);

        JSONArray jfkey = new JSONArray();

        JSONObject jtable = new JSONObject();
        jtable.put("name","szemely");
        jtable.put("attributes",jattribute);
        jtable.put("fkeys",jfkey);

        Attribute attribute = new Attribute("cnp", "int", true, false, true, true);
        List<Attribute>attributes = new ArrayList<>();
        attributes.add(attribute);
        Table table = new Table("szemely",attributes);*/

        //printWriter.println("valami");

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