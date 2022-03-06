import hello.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class AttributePanel extends JPanel {

    private ClientServer clientServer;
    private CreateTable createTable;

    private JTextField textField;
    private JComboBox type;
    private JCheckBox pk;
    private JCheckBox index;
    private JCheckBox unique;
    private JCheckBox fk;
    private JComboBox table;
    private JComboBox attribute;

    private JLabel nameLabel;
    private JLabel typeLabel;
    private JLabel pkLabel;
    private JLabel indexLabel;
    private JLabel uniqueLabel;
    private JLabel fkLabel;
    private JLabel tableLabel;
    private JLabel attrLabel;

    public AttributePanel(ClientServer cl, CreateTable cr) {
        clientServer = cl;
        createTable = cr;

        textField = new JTextField(10);
        type = new JComboBox();
        pk = new JCheckBox();
        index = new JCheckBox();
        unique = new JCheckBox();
        fk = new JCheckBox();
        table = new JComboBox();
        attribute = new JComboBox();
        nameLabel = new JLabel("nev:");
        typeLabel = new JLabel("tipus:");
        pkLabel = new JLabel("PK:");
        indexLabel = new JLabel("index:");
        uniqueLabel = new JLabel("unique:");
        fkLabel = new JLabel("FK:");
        tableLabel = new JLabel("tabla:");
        attrLabel = new JLabel("attr:");

        type.addItem("int");
        type.addItem("float");
        type.addItem("bit");
        type.addItem("date");
        type.addItem("datetime");
        type.addItem("string");

        updateTableCombo();
        updateAttrCombo();

        add(nameLabel);
        add(textField);
        add(typeLabel);
        add(type);
        add(pkLabel);
        add(pk);
        add(indexLabel);
        add(index);
        add(uniqueLabel);
        add(unique);
        add(fkLabel);
        add(fk);
        add(tableLabel);
        add(table);
        add(attrLabel);
        add(attribute);

        table.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAttrCombo();
            }
        });

        Arrays.stream(this.getComponents()).forEach(a-> {
            a.setFont(new Font("Serif",Font.PLAIN, 20));});
    }

    public void updateTableCombo() {
        table.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command","Get Tables");
        message.put("value",createTable.getDbCombo().getSelectedItem().toString());
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a->{table.addItem(a);});
    }


    public void updateAttrCombo() {
        attribute.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command","Get Attributes By Type");
        JSONObject value = new JSONObject();
        value.put("database",createTable.getDbCombo().getSelectedItem().toString());
        value.put("table", table.getSelectedItem());
        value.put("type",type.getSelectedItem());

        message.put("value",value);
        String answer = clientServer.send(message.toJSONString());

        Arrays.stream(answer.split(",")).forEach(a->{attribute.addItem(a);});
    }

    /*
    * command - create table
    * value -
    *   tableName -
    *   database -
    *   attributes - (array)
    *       name -
    *       type -
    *       pk -
    *       index -
    *       unique -
    *       fk -
    *       ftable -
    *       fattr -
    * */

    public JSONObject createMessage() {
        JSONObject message = new JSONObject();
        message.put("name", textField.getText());
        message.put("type",type.getSelectedItem());
        message.put("pk", pk.isSelected());
        message.put("index", index.isSelected());
        message.put("unique", unique.isSelected());
        message.put("fk", fk.isSelected());
        message.put("ftable", table.getSelectedItem());
        message.put("fattr", attribute.getSelectedItem());

        return message;
    }
}
