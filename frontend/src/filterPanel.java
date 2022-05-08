import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class filterPanel extends JPanel {
    private ClientServer clientServer;
    private String database;
    private List<String> tableNames;
    private JComboBox tableCombo;
    private JComboBox fields;
    private JComboBox operators;
    private JTextField value;
    private List<String> types;
    private String order;

    public filterPanel(String database, ArrayList<String> tableNames, ClientServer cl) {
        clientServer = cl;
        this.database = database;
        this.tableNames = new ArrayList<>(tableNames);
        fields = new JComboBox();
        fields.setFont(new Font("Serif", Font.BOLD, 25));
        operators = new JComboBox();
        operators.setFont(new Font("Serif", Font.BOLD, 25));
        tableCombo = new JComboBox();
        tableCombo.setFont(new Font("Serif", Font.BOLD, 25));
        value = new JTextField(20);
        value.setFont(new Font("Serif", Font.BOLD, 25));
        types = new ArrayList<>();

        for (String t : tableNames) tableCombo.addItem(t);
        updateFields();
        initializeOperators();

        tableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFields();
            }
        });

        value.setToolTipText(types.get(fields.getSelectedIndex()));
        fields.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fields.getItemCount()>0)
                    value.setToolTipText(types.get(fields.getSelectedIndex()));
            }
        });

        add(tableCombo);
        add(fields);
        add(operators);
        add(value);
    }

    public void updateFields() {
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

        order=new String();

        types = new ArrayList<>();
        types.add(ans.get(answKey).toString());

        fields.addItem(answKey);

        //System.out.println(ans);
        for (Object a : ans.keySet())
        if(!a.toString().equals(answKey)){
            order+=a.toString()+",";
            String type = ans.get(a).toString();
            if (type.equals("date")) type += "(yyyy/mm/dd)";
            if (type.equals("datetime")) type += "(yyyy/mm/dd:hh:mm)";
            types.add(type);

            fields.addItem(a.toString());
        }

    }

    public void initializeOperators() {
        operators.addItem("=");
        operators.addItem("<");
        operators.addItem(">");
        operators.addItem("<=");
        operators.addItem(">=");
    }

    public JSONObject toJSONObject() {
        JSONObject message = new JSONObject();
        message.put("table", tableCombo.getSelectedItem().toString());
        message.put("field", fields.getSelectedItem().toString());
        message.put("operator", operators.getSelectedItem().toString());
        message.put("value", value.getText());
        message.put("order",order);

        return message;
    }
}
