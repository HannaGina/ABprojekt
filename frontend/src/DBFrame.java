import javax.swing.*;
import java.awt.*;

public class DBFrame extends JFrame {
    private MenuPanel menuPanel;
    private CreateDatabase createDatabase;
    private ClientServer clientServer;
    private DropDatabase dropDatabase;
    private CreateTable createTable;
    private DropTable dropTable;

    public DBFrame(ClientServer cl) {
        clientServer = cl;
        menuPanel = new MenuPanel(this);
        createDatabase = new CreateDatabase(this,clientServer);
        dropDatabase = new DropDatabase(this,clientServer);
        createTable = new CreateTable(this,clientServer);
        dropTable = new DropTable(this,clientServer);

        add(menuPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100,100,1000,800);
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
                createDatabase.doEmpty();
                container.add(createDatabase);
                break;
            case "DropDatabase" :
                dropDatabase.updateCombo();
                container.add(dropDatabase);
                break;
            case "CreateTable" :
                createTable = new CreateTable(this,clientServer);
                createTable.updateCombo();
                container.add(createTable);
                break;
            case "DropTable" :
                dropTable = new DropTable(this,clientServer);
                container.add(dropTable);
                break;
        }
        container.validate();
        container.repaint();
    }


}
