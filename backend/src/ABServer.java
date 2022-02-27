
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
        JsonObject value = Json.createObjectBuilder()
                .add("firstName", "John")
                .add("lastName", "Smith")
                .add("age", 25)
                .add("address", Json.createObjectBuilder()
                        .add("streetAddress", "21 2nd Street")
                        .add("city", "New York")
                        .add("state", "NY")
                        .add("postalCode", "10021"))
                .add("phoneNumber", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("type", "home")
                                .add("number", "212 555-1234"))
                        .add(Json.createObjectBuilder()
                                .add("type", "fax")
                                .add("number", "646 555-4567")))
                .build();

        try {
            server = new ServerSocket(port);
            client = server.accept();
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out.println("hello");
            System.out.println(in.readLine());
            in.close();
            js = newJSON
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ABServer a = new ABServer();
    }
}
