import hello.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class DropTable extends JPanel {
    private JLabel dbLabel;
    private JLabel tableLabel;
    private JComboBox dbCombo;
    private JComboBox tableCombo;
    private JButton dropTableBtn;
    private JButton menuBtn;
    private JPanel mainPanel;
    private DBFrame dbFrame;
    private JLabel answerLabel;
    private ClientServer clientServer;

    public DropTable(DBFrame dbFrame,ClientServer cl) {
        clientServer = cl;
        this.dbFrame = dbFrame;

        answerLabel = new JLabel();
        answerLabel.setFont(new Font("Serif",Font.PLAIN, 20));
        dbLabel = new JLabel("Adatbazis neve: ");
        dbLabel.setFont(new Font("Serif",Font.BOLD, 30));
        tableLabel = new JLabel("Tabla neve: ");
        tableLabel.setFont(new Font("Serif",Font.BOLD, 30));
        dropTableBtn = new JButton("Tabla torlese");
        dropTableBtn.setFont(new Font("Serif",Font.BOLD, 30));
        menuBtn = new JButton("Menu");
        menuBtn.setFont(new Font("Serif",Font.BOLD, 30));
        dbCombo = new JComboBox();
        dbCombo.setFont(new Font("Serif",Font.BOLD, 30));
        tableCombo = new JComboBox();
        tableCombo.setFont(new Font("Serif",Font.BOLD, 30));

        updateDbCombo();
        updateTableCombo();

        dbCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTableCombo();
            }
        });

        dropTableBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JSONObject message = new JSONObject();
                message.put("command","Drop Table");
                JSONObject miniMessage = new JSONObject();
                miniMessage.put("database",dbCombo.getSelectedItem().toString());
                miniMessage.put("table",tableCombo.getSelectedItem().toString());
                message.put("value", miniMessage);
                answerLabel.setText(clientServer.send(message.toJSONString()));
                updateTableCombo();
            }
        });

        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("MenuPanel");
            }
        });

        setLayout(new BorderLayout());
        add(menuBtn,BorderLayout.NORTH);

        mainPanel = new JPanel();
        mainPanel.add(dbLabel);
        mainPanel.add(dbCombo);
        mainPanel.add(tableLabel);
        mainPanel.add(tableCombo);
        mainPanel.add(dropTableBtn);
        mainPanel.add(answerLabel);

        add(mainPanel,BorderLayout.CENTER);
    }

    public void updateDbCombo() {
        dbCombo.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command","Get Databases");
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a->{dbCombo.addItem(a);});
    }

    public void updateTableCombo() {
        tableCombo.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command","Get Tables");
        message.put("value",dbCombo.getSelectedItem().toString());
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a->{tableCombo.addItem(a);});
    }

    public void setAnswerLabel(String string) {
        answerLabel.setText(string);
    }
}
