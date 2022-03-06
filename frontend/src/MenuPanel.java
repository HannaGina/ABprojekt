import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuPanel extends JPanel {
    private JButton createDatabaseBtn;
    private DBFrame dbFrame;
    private JButton dropDbBtn;

    public MenuPanel(DBFrame dbFrame) {
        this.dbFrame = dbFrame;
        createDatabaseBtn = new JButton("Adatbazis letrehozasa");
        createDatabaseBtn.setFont(new Font("Serif",Font.BOLD, 30));
        dropDbBtn = new JButton("Adatbazis torlese");
        dropDbBtn.setFont(new Font("Serif",Font.BOLD, 30));

        createDatabaseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("CreateDatabase");
            }
        });


        dropDbBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("DropDatabase");
            }
        });

        this.add(createDatabaseBtn);
        this.add(dropDbBtn);
    }
}
