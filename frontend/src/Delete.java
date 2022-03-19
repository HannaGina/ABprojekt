import hello.JSONArray;
import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class Delete extends JPanel {
    private DBFrame dbFrame;
    private ClientServer clientServer;

    private JButton menuBtn;
    private JLabel dbLabel;
    private JLabel tableLabel;
    private JComboBox dbCombo;
    private JComboBox tableCombo;
    private JLabel answerLabel;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JPanel attrPanel;
    private JPanel controlPanel;
    private JButton deleteBtn;
    private int colNumber;
    private int rowNumber;

    public Delete(DBFrame dbFrame,ClientServer cl) {
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
        deleteBtn = new JButton("Adatok torlese");
        deleteBtn.setFont(new Font("Serif",Font.BOLD, 30));
        attrPanel = new JPanel();
        attrPanel.setLayout(new GridLayout());
        attrPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        scrollPane = new JScrollPane(attrPanel);
        scrollPane.setPreferredSize(new Dimension(800,500));
        scrollPane.setAutoscrolls(true);

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

        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JSONObject bigMessage = new JSONObject();
                bigMessage.put("command","Delete Documents From Table");
                JSONObject message = new JSONObject();
                message.put("database", dbCombo.getSelectedItem());
                message.put("table", tableCombo.getSelectedItem());
                JSONArray array = new JSONArray();

                int j=2;

                while(j<=rowNumber) {
                    //System.out.println(attrPanel.getComponent((j-1)*colNumber).getClass());
                    if(((JCheckBox)attrPanel.getComponent((j-1)*colNumber)).isSelected()) {
                        array.add(((JLabel)attrPanel.getComponent((j-1)*colNumber+1)).getText());
                    }
                    ++j;
                }
                message.put("cells", array);
                bigMessage.put("value",message);

                answerLabel.setText(clientServer.send(bigMessage.toJSONString()));
                updateAttributes();
            }
        });

        setLayout(new BorderLayout());
        add(menuBtn,BorderLayout.NORTH);
        mainPanel = new JPanel();
        mainPanel.add(dbLabel);
        mainPanel.add(dbCombo);
        mainPanel.add(tableLabel);
        mainPanel.add(tableCombo);
        mainPanel.add(scrollPane);

        controlPanel = new JPanel();
        controlPanel.add(deleteBtn);
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
            //System.out.println(-1);
            e.printStackTrace();
        }
        String[] s = ans.toString().split(",");

        JSONObject messageKey = new JSONObject();
        messageKey.put("command","Get Primary Key");
        messageKey.put("value", message2);
        String answKey = clientServer.send(messageKey.toJSONString());

        JSONObject messageDatas = new JSONObject();
        messageDatas.put("command", "Get Documents From Table");
        messageDatas.put("value", message2);
        String answDatas = clientServer.send(messageDatas.toJSONString());
        //System.out.println(answDatas);
        String[] datas = answDatas.split(",");

        //System.out.println(datas.length);
        //System.out.println(s.length);

        colNumber = s.length+1;
        rowNumber = (datas.length+1)/(s.length+1)+1;
        if(answDatas.isEmpty()) rowNumber=1;
        attrPanel.setLayout(new GridLayout(rowNumber,colNumber));

        //attrPanel.setPreferredSize(new Dimension(750, s.length*50));

        JLabel label = new JLabel("Torlendo");
        label.setFont(new Font("Serif",Font.BOLD, 30));
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        attrPanel.add(label);

        JLabel keyLabel = new JLabel(answKey);
        keyLabel.setFont(new Font("Serif",Font.BOLD, 30));
        keyLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        keyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        attrPanel.add(keyLabel);

        for(Object a : ans.keySet()) {
            if(!a.equals(answKey)) {
                JLabel jLabel = new JLabel(a.toString());
                jLabel.setFont(new Font("Serif", Font.BOLD, 30));
                jLabel.setBorder(BorderFactory.createLineBorder(Color.black));
                jLabel.setHorizontalAlignment(SwingConstants.CENTER);
                attrPanel.add(jLabel);
            }
        }

        if(!answDatas.isEmpty()) {
            JCheckBox checkbox = new JCheckBox();
            Icon normal = new ImageIcon(new ImageIcon("deselected.png").getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
            Icon selected = new ImageIcon(new ImageIcon("selected.png").getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
            checkbox.setIcon(normal);
            checkbox.setSelectedIcon(selected);
            checkbox.setBorder(BorderFactory.createLineBorder(Color.black));
            attrPanel.add(checkbox);

            Arrays.stream(datas).forEach(a -> {
                if (!a.equals("")) {
                    JLabel jLabel = new JLabel(a.toString());
                    jLabel.setFont(new Font("Serif", Font.BOLD, 30));
                    jLabel.setBorder(BorderFactory.createLineBorder(Color.black));
                    jLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    attrPanel.add(jLabel);
                } else {
                    JCheckBox chkbox = new JCheckBox();
                    chkbox.setIcon(normal);
                    chkbox.setSelectedIcon(selected);
                    checkbox.setBorder(BorderFactory.createLineBorder(Color.black));
                    attrPanel.add(chkbox);
                }
            });
        }

        //System.out.println(s.length);
        //scrollPane.setPreferredSize(new Dimension(750, s.length*50));

        attrPanel.validate();
        attrPanel.repaint();
        scrollPane.validate();
        scrollPane.repaint();
    }
}
