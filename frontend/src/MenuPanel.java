import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class MenuPanel extends JPanel {
    private JButton createDatabaseBtn;
    private DBFrame dbFrame;
    private JButton dropDbBtn;
    private JButton createTableBtn;
    private JButton dropTableBtn;
    private JButton insertBtn;
    private JButton deleteBtn;
    private JButton selectBtn;

    public MenuPanel(DBFrame dbFrame) {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        this.dbFrame = dbFrame;
        createDatabaseBtn = new JButton("Adatbazis letrehozasa");
        createDatabaseBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        dropDbBtn = new JButton("Adatbazis torlese");
        dropDbBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        createTableBtn = new JButton("Tabla letrehozasa");
        createTableBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        dropTableBtn = new JButton("Tabla torlese");
        dropTableBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        insertBtn = new JButton("Adatok beszurasa");
        insertBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        deleteBtn = new JButton("Adatok torlese");
        deleteBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        selectBtn = new JButton("Adatok lekerese");
        selectBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);

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

        createTableBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("CreateTable");
            }
        });

        dropTableBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("DropTable");
            }
        });

        insertBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("Insert");
            }
        });

        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("Delete");
            }
        });

        selectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("Select");
            }
        });


        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(createDatabaseBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(dropDbBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(createTableBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(dropTableBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(insertBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(deleteBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(selectBtn);
        this.add(Box.createRigidArea(new Dimension(0, 10)));

        Arrays.stream(this.getComponents()).forEach(a-> {
            a.setFont(new Font("Serif",Font.PLAIN, 30));});
    }

}
