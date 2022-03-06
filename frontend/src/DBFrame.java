import javax.swing.*;
import java.awt.*;

public class DBFrame extends JFrame {
    private MenuPanel menuPanel;
    private CreateDatabase createDatabase;
    private ClientServer clientServer;
    private DropDatabase dropDatabase;

    public DBFrame(ClientServer cl) {
        clientServer = cl;
        menuPanel = new MenuPanel(this);
        createDatabase = new CreateDatabase(this,clientServer);
        dropDatabase = new DropDatabase(this,clientServer);

        add(menuPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100,100,800,800);
        setVisible(true);
    }

    public void JumpTo(String panelName) {
        Container container = getContentPane();
        container.removeAll();
        switch (panelName) {
            case "MenuPanel" :
                container.add(menuPanel);
                break;
            case "CreateDatabase" :
                container.add(createDatabase);
                break;
            case "DropDatabase" :
                dropDatabase.updateCombo();
                container.add(dropDatabase);
                break;
        }
        container.validate();
        container.repaint();
    }
}
