import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class Insert extends JPanel {
    private DBFrame dbFrame;
    private ClientServer clientServer;

    private JButton menuBtn;
    private JLabel dbLabel;
    private JLabel tableLabel;
    private JComboBox dbCombo;
    private JComboBox tableCombo;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JPanel attrPanel;
    private JButton insertBtn;
    private JLabel answerLabel;
    private JPanel controlPanel;

    public Insert(DBFrame dbFrame,ClientServer cl) {
        clientServer = cl;
        this.dbFrame = dbFrame;

        answerLabel = new JLabel();
        answerLabel.setFont(new Font("Serif",Font.PLAIN, 20));
        dbLabel = new JLabel("Adatbazis neve: ");
        dbLabel.setFont(new Font("Serif",Font.BOLD, 30));
        tableLabel = new JLabel("Tabla neve: ");
        tableLabel.setFont(new Font("Serif",Font.BOLD, 30));
        dbCombo = new JComboBox();
        dbCombo.setFont(new Font("Serif",Font.BOLD, 30));
        tableCombo = new JComboBox();
        tableCombo.setFont(new Font("Serif",Font.BOLD, 30));
        menuBtn = new JButton("Menu");
        menuBtn.setFont(new Font("Serif",Font.BOLD, 30));
        insertBtn = new JButton("Adatok beszurasa");
        insertBtn.setFont(new Font("Serif",Font.BOLD, 30));
        attrPanel = new JPanel();
        attrPanel.setLayout(new GridLayout());
        scrollPane = new JScrollPane(attrPanel);
        scrollPane.setPreferredSize(new Dimension(800,500));
        scrollPane.setAutoscrolls(true);
        //scrollPane.setMaximumSize(new Dimension(800,500));

        updateDbCombo();
        updateTableCombo();
        updateAttributes();

        dbCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateTableCombo();
            }
        });

        tableCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED)
                    updateAttributes();
            }
        });

        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("MenuPanel");
            }
        });

        insertBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                /*
                command
                value
                    database
                    table
                    cells
                         attr - ertek
                */

                JSONObject bigMessage = new JSONObject();
                bigMessage.put("command","Insert Into Table");
                JSONObject message = new JSONObject();
                message.put("database", dbCombo.getSelectedItem());
                message.put("table", tableCombo.getSelectedItem());
                JSONObject minmessage = new JSONObject();
                Object[] objects = Arrays.stream(attrPanel.getComponents()).toArray();
                for (int i = 0; i < objects.length; i+=2) {
                    minmessage.put(((JLabel)objects[i]).getText(),((JTextField)objects[i+1]).getText());
                }
                message.put("cells", minmessage);
                bigMessage.put("value",message);

                answerLabel.setText(clientServer.send(bigMessage.toJSONString()));
            }
        });

        setLayout(new BorderLayout());
        add(menuBtn,BorderLayout.NORTH);

        //scrollPane.add(attrPanel);
       // scrollPane.setViewportView(attrPanel);

        mainPanel = new JPanel();
        //mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        mainPanel.add(dbLabel);
        mainPanel.add(dbCombo);
        mainPanel.add(tableLabel);
        mainPanel.add(tableCombo);
        mainPanel.add(scrollPane);

        controlPanel = new JPanel();
        controlPanel.add(insertBtn);
        controlPanel.add(answerLabel);
        add(controlPanel, BorderLayout.SOUTH);
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

    public void updateAttributes() {
        attrPanel.removeAll();
        JSONObject message = new JSONObject();
        message.put("command","Get Table Values");
        JSONObject message2 = new JSONObject();
        message2.put("database", dbCombo.getSelectedItem().toString());
        message2.put("table", tableCombo.getSelectedItem().toString());
        message.put("value",message2);
        JSONParser jsonParser = new JSONParser();
        JSONObject ans = new JSONObject();
        try {
            ans =  (JSONObject) jsonParser.parse(clientServer.send(message.toJSONString()));
        } catch (ParseException e) {
            System.out.println(-1);
            e.printStackTrace();
        }

        String[] s = ans.toString().split(",");
        //Arrays.stream(s).forEach(a-> System.out.println(a));
        //System.out.println(ans);
        attrPanel.setLayout(new GridLayout(s.length,2));
        attrPanel.setPreferredSize(new Dimension(750, s.length*50));
        attrPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        for(Object a : ans.keySet()) {
            JLabel jLabel = new JLabel(a.toString());
            jLabel.setFont(new Font("Serif",Font.BOLD, 30));
            attrPanel.add(jLabel);
            JTextField jTextField = new JTextField(20);
            jTextField.setFont(new Font("Serif",Font.BOLD, 30));
            String type = ans.get(a).toString();
            if(type.equals("date")) type += "(yyyy/mm/dd)";
            if(type.equals("datetime")) type += "(yyyy/mm/dd:hh:mm)";
            jTextField.setToolTipText(type);

            attrPanel.add(jTextField);
        }
        //System.out.println(s.length);
        //scrollPane.setPreferredSize(new Dimension(750, s.length*50));

        attrPanel.validate();
        attrPanel.repaint();
        scrollPane.validate();
        scrollPane.repaint();
    }
}
