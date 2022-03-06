import hello.JSONArray;
import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class DropDatabase extends JPanel {
    private JLabel jLabel;
    private JComboBox jComboBox;
    private JButton dropDbBtn;
    private JButton menuBtn;
    private JPanel mainPanel;
    private DBFrame dbFrame;
    private JLabel answerLabel;
    private ClientServer clientServer;

    public DropDatabase(DBFrame dbFrame,ClientServer cl) {
        clientServer = cl;
        this.dbFrame = dbFrame;

        answerLabel = new JLabel();
        answerLabel.setFont(new Font("Serif",Font.PLAIN, 20));
        jLabel = new JLabel("Adatbazis neve: ");
        jLabel.setFont(new Font("Serif",Font.BOLD, 30));
        dropDbBtn = new JButton("Adatbazis torlese");
        dropDbBtn.setFont(new Font("Serif",Font.BOLD, 30));
        menuBtn = new JButton("Menu");
        menuBtn.setFont(new Font("Serif",Font.BOLD, 30));
        jComboBox = new JComboBox();
        jComboBox.setFont(new Font("Serif",Font.BOLD, 30));

        updateCombo();

        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("MenuPanel");
            }
        });

        dropDbBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JSONObject message = new JSONObject();
                message.put("command","Drop Database");
                message.put("value",jComboBox.getSelectedItem().toString());
                answerLabel.setText(clientServer.send(message.toJSONString()));
                updateCombo();
            }
        });

        this.setLayout(new BorderLayout());
        add(menuBtn,BorderLayout.NORTH);

        mainPanel = new JPanel();
        mainPanel.add(jLabel);
        mainPanel.add(jComboBox);
        mainPanel.add(dropDbBtn);
        mainPanel.add(answerLabel);

        add(mainPanel,BorderLayout.CENTER);
    }

    public void updateCombo() {
        jComboBox.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command","Get Databases");
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a->{jComboBox.addItem(a);});

    }
}
