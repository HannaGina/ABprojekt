import hello.JSONArray;
import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class joinPanel extends JPanel {
    private ClientServer clientServer;
    private String database;
    private List<String> tableNames1;
    private List<String> tableNames2;
    private JComboBox tableCombo1;
    private JComboBox fields1;
    private JComboBox tableCombo2;
    private JComboBox fields2;
    private JSONArray order1;
    private JSONArray order2;
    private final JLabel label = new JLabel("=");

    public joinPanel(String database, ArrayList<String> tableNames1, ArrayList<String> tableNames2, ClientServer cl) {
        clientServer = cl;
        this.database = database;
        this.tableNames1 = new ArrayList<>(tableNames1);
        this.tableNames2 = new ArrayList<>(tableNames2);
        fields1 = new JComboBox();
        fields1.setFont(new Font("Serif", Font.BOLD, 25));
        fields2 = new JComboBox();
        fields2.setFont(new Font("Serif", Font.BOLD, 25));
        tableCombo1 = new JComboBox();
        tableCombo1.setFont(new Font("Serif", Font.BOLD, 25));
        tableCombo2 = new JComboBox();
        tableCombo2.setFont(new Font("Serif", Font.BOLD, 25));
        label.setFont(new Font("Serif", Font.BOLD, 25));
        order1 = new JSONArray();
        order2 = new JSONArray();

//        System.out.println(tableNames2);
        for (String t : tableNames1) tableCombo1.addItem(t);
        for (String t : tableNames2) tableCombo2.addItem(t);

        tableCombo1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFields(fields1, tableCombo1, order1);
            }
        });

        tableCombo2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFields(fields2, tableCombo2, order2);
            }
        });

        updateFields(fields1, tableCombo1, order1);
        updateFields(fields2, tableCombo2, order2);

        add(tableCombo1);
        add(fields1);
        add(label);
        add(tableCombo2);
        add(fields2);
    }


    public void updateFields(JComboBox fields, JComboBox tableCombo, JSONArray order) {
        fields.removeAllItems();

        String table = tableCombo.getSelectedItem().toString();

        JSONObject message = new JSONObject();
        message.put("command", "Get Table Values");
        JSONObject message2 = new JSONObject();
        message2.put("database", database);
        message2.put("table", table);
        message.put("value", message2);
        JSONParser jsonParser = new JSONParser();
        JSONObject ans = new JSONObject();
        try {
            ans = (JSONObject) jsonParser.parse(clientServer.send(message.toJSONString()));
        } catch (ParseException f) {
            f.printStackTrace();
        }

        JSONObject messageKey = new JSONObject();
        messageKey.put("command","Get Primary Key");
        messageKey.put("value", message2);
        String answKey = clientServer.send(messageKey.toJSONString());

        order.clear();

        fields.addItem(answKey);
        order.add(answKey);

        for (Object a : ans.keySet())
            if(!a.toString().equals(answKey)){
                order.add(a.toString());
                fields.addItem(a.toString());
            }
    }

    public void blockateThis() {
//        setBackground(Color.gray);
        tableCombo1.setEnabled(false);
        tableCombo2.setEnabled(false);
        fields1.setEnabled(false);
        fields2.setEnabled(false);
    }

    public String getJoined() {
        return tableCombo1.getSelectedItem().toString();
    }

    public JSONObject toJSONObject() {

        //System.out.println(order1);
        //System.out.println(order2);

        JSONObject message = new JSONObject();
        message.put("table1", tableCombo1.getSelectedItem().toString());
        message.put("field1", fields1.getSelectedItem().toString());
        message.put("table2", tableCombo2.getSelectedItem().toString());
        message.put("field2", fields2.getSelectedItem().toString());
        message.put("order1",order1);
        message.put("order2",order2);

        return message;
    }
}
