import hello.JSONArray;
import hello.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateTable extends JPanel {
    private JButton menuBtn;
    private JButton createTableBtn;
    private JPanel mainPanel;
    private JLabel jLabel;
    private JTextField jTextField;
    private DBFrame dbFrame;
    private JLabel answerLabel;
    private ClientServer clientServer;
    private JButton addAttribute;
    private ArrayList<AttributePanel> attributePanels;
    private JLabel dbLabel;
    private JComboBox dbCombo;
    private JPanel controlPanel;

    public CreateTable(DBFrame dbFrame,ClientServer cl) {
        clientServer = cl;
        this.dbFrame = dbFrame;

        answerLabel = new JLabel();
        answerLabel.setFont(new Font("Serif",Font.PLAIN, 17));
        jLabel = new JLabel("Tabla neve: ");
        jLabel.setFont(new Font("Serif",Font.BOLD, 25));
        jTextField = new JTextField(20);
        jTextField.setFont(new Font("Serif",Font.BOLD, 20));
        createTableBtn = new JButton("Tabla letrehozasa");
        createTableBtn.setFont(new Font("Serif",Font.BOLD, 25));
        menuBtn = new JButton("Menu");
        menuBtn.setFont(new Font("Serif",Font.BOLD, 25));
        addAttribute = new JButton("Attributum hozzaadasa");
        addAttribute.setFont(new Font("Serif",Font.BOLD, 25));
        dbLabel = new JLabel("Adatbazis:");
        dbLabel.setFont(new Font("Serif",Font.BOLD, 25));
        dbCombo = new JComboBox();
        dbCombo.setFont(new Font("Serif",Font.BOLD, 25));
        updateCombo();
        attributePanels = new ArrayList<>();
        controlPanel = new JPanel();

        mainPanel = new JPanel();
        mainPanel.add(dbLabel);
        mainPanel.add(dbCombo);
        mainPanel.add(jLabel);
        mainPanel.add(jTextField);
        mainPanel.add(addAttribute);

        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("MenuPanel");
            }
        });

        dbCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attributePanels.forEach(a->{a.updateTableCombo();});
            }
        });

        createTableBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JSONObject bigMessage = new JSONObject();
                bigMessage.put("command","Create Table");
                JSONObject message = new JSONObject();
                message.put("database",dbCombo.getSelectedItem());
                JSONArray mesArray = new JSONArray();

                attributePanels.forEach(a->{mesArray.add(a.createMessage());});
                message.put("attributes",mesArray);
                message.put("table", jTextField.getText());
                bigMessage.put("value",message);

                answerLabel.setText(clientServer.send(bigMessage.toJSONString()));
            }
        });

        CreateTable cr = this;
        addAttribute.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AttributePanel attributePanel = new AttributePanel(clientServer,cr);
                mainPanel.add(attributePanel);
                attributePanels.add(attributePanel);
                mainPanel.validate();
                mainPanel.repaint();
            }
        });

        controlPanel.add(createTableBtn);
        controlPanel.add(answerLabel);

        setLayout(new BorderLayout());
        add(menuBtn,BorderLayout.NORTH);
        add(mainPanel,BorderLayout.CENTER);
        add(controlPanel,BorderLayout.SOUTH);
    }

    public void updateCombo() {
        dbCombo.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command","Get Databases");
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a->{dbCombo.addItem(a);});

    }

    public JComboBox getDbCombo() {
        return dbCombo;
    }
}
