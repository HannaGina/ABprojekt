import hello.JSONArray;
import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;

import javax.swing.*;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Select extends JPanel {
    private DBFrame dbFrame;
    private ClientServer clientServer;

    private JButton menuBtn;
    private JLabel dbLabel;
    private JLabel tableLabel;
    private JLabel selectLabel;
    private JComboBox dbCombo;
    private JComboBox tableCombo;
    private JLabel answerLabel;
    private JPanel mainPanel;
    private JPanel controlPanel;
    private DefaultListModel<String> selectedFieldsList;
    private DefaultListModel<String> unselectedFieldsList;
    private List<filterPanel> filterPanels;
    private JList<String> selectedFields;
    private JList<String> unselectedFields;
    private JButton left;
    private JButton right;
    private JButton addFilter;
    private JPanel database;
    private JPanel projection;
    private JPanel from;
    private JPanel filter;
    private JScrollPane filterScroll;
    private JButton selectBtn;
    private String order;
    private Map<String, Integer> index;
    private List<Boolean> show;
    private List<String>orderArray;
    private JFrame answerFrame;
    private JPanel answerPanel;
    private JScrollPane answerScroll;

    public Select(DBFrame dbFrame, ClientServer cl) {
        clientServer = cl;
        this.dbFrame = dbFrame;

        menuBtn = new JButton("Menu");
        menuBtn.setFont(new Font("Serif", Font.BOLD, 25));
        selectBtn = new JButton("Keres");
        selectBtn.setFont(new Font("Serif", Font.BOLD, 25));
        addFilter = new JButton("Feltétel hozzáadása");
        addFilter.setFont(new Font("Serif", Font.BOLD, 25));
        dbLabel = new JLabel("Adatbázis neve: ");
        dbLabel.setFont(new Font("Serif", Font.BOLD, 25));
        answerLabel = new JLabel();
        answerLabel.setFont(new Font("Serif", Font.PLAIN, 25));
        tableLabel = new JLabel("Tábla neve: ");
        tableLabel.setFont(new Font("Serif", Font.BOLD, 25));
        selectLabel = new JLabel("Mezők:");
        selectLabel.setFont(new Font("Serif", Font.BOLD, 25));
        dbCombo = new JComboBox();
        dbCombo.setFont(new Font("Serif", Font.BOLD, 25));
        tableCombo = new JComboBox();
        tableCombo.setFont(new Font("Serif", Font.BOLD, 25));
        filterPanels = new ArrayList<>();
        unselectedFieldsList = new DefaultListModel<>();
        selectedFieldsList = new DefaultListModel<>();
        index = new HashMap<>();
        show = new ArrayList<>();
        orderArray = new ArrayList<>();
        unselectedFields = new JList<>(unselectedFieldsList);
        selectedFields = new JList<>(selectedFieldsList);
        left = new JButton("<<");
        right = new JButton(">>");
        unselectedFields.setFont(new Font("Serif", Font.BOLD, 25));
        selectedFields.setFont(new Font("Serif", Font.BOLD, 25));
        unselectedFields.setFixedCellHeight(25);
        unselectedFields.setFixedCellWidth(250);
        selectedFields.setFixedCellHeight(25);
        selectedFields.setFixedCellWidth(250);

        selectedFieldsList.addElement("Kiválasztott  mezők");
        unselectedFieldsList.addElement("Kiválasztható  mezők");

        updateDbCombo();
        updateTableCombo();
        updateFields();
        initializeFilters();

        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("MenuPanel");
            }
        });

        dbCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateTableCombo();
            }
        });

        tableCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    updateFields();
                    mainPanel.remove(filterScroll);
                    initializeFilters();
                    mainPanel.add(filterScroll);
                }
            }
        });

        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer>torlendo = new ArrayList<>();
                int i = unselectedFields.getSelectedIndex();
                while (i!=-1){
                    if(i!=0) torlendo.add(i);
                    unselectedFields.removeSelectionInterval(i,i);
                    i = unselectedFields.getSelectedIndex();
                }

                while (!torlendo.isEmpty()){
                    int l = torlendo.size()-1;
                    selectedFieldsList.addElement(unselectedFieldsList.get(torlendo.get(l)));
                    show.set(index.get(unselectedFieldsList.get(torlendo.get(l))),Boolean.TRUE);
                    unselectedFieldsList.remove(torlendo.get(l));
                    torlendo.remove(l);
                }
            }
        });

        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer>torlendo = new ArrayList<>();
                int i = selectedFields.getSelectedIndex();
                while (i!=-1){
                    if(i!=0) torlendo.add(i);
                    selectedFields.removeSelectionInterval(i,i);
                    i = selectedFields.getSelectedIndex();
                }

                while (!torlendo.isEmpty()){
                    int l = torlendo.size()-1;
                    unselectedFieldsList.addElement(selectedFieldsList.get(torlendo.get(l)));
                    show.set(index.get(selectedFieldsList.get(torlendo.get(l))),Boolean.FALSE);
                    selectedFieldsList.remove(torlendo.get(l));
                    torlendo.remove(l);
                }
            }
        });


        addFilter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArrayList<String>tables = new ArrayList<>();
                tables.add(tableCombo.getSelectedItem().toString());
                filterPanel f = new filterPanel(dbCombo.getSelectedItem().toString(),tables, clientServer);
                filter.add(f);
                filter.add(Box.createRigidArea(new Dimension(0, 10)));
                filterPanels.add(f);
                filter.validate();
                filter.repaint();
                filterScroll.validate();
                filterScroll.repaint();
            }
        });

        selectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JSONObject message = toJSONMessage();

/*                JSONObject message2 = new JSONObject();
                message2.put("database", dbCombo.getSelectedItem().toString());
                message2.put("table", tableCombo.getSelectedItem().toString());
                JSONObject messageDatas = new JSONObject();
                messageDatas.put("command", "Get Documents From Table");
                messageDatas.put("value", message2);

                String answDatas = clientServer.send(messageDatas.toJSONString());
*/
                String answDatas = clientServer.send(message.toJSONString());

                if(answDatas.startsWith("HIBA")) {
                    answerLabel.setText(answDatas);
                } else {
                    answerLabel.setText("");
                    showAnswer(answDatas);
                }

            }
        });

        database = new JPanel();
        database.add(dbLabel);
        database.add(dbCombo);

        from = new JPanel();
        from.add(tableLabel);
        from.add(tableCombo);

        projection = new JPanel();
        projection.add(selectLabel);
        projection.add(selectedFields);
        projection.add(left);
        projection.add(right);
        projection.add(unselectedFields);


        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        mainPanel.add(database);
        mainPanel.add(from);
        mainPanel.add(projection);
        mainPanel.add(filterScroll);

        controlPanel = new JPanel();
        controlPanel.add(selectBtn);
        controlPanel.add(answerLabel);

        setLayout(new BorderLayout());
        add(menuBtn, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel,BorderLayout.SOUTH);
    }

    public void updateDbCombo() {
        dbCombo.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command", "Get Databases");
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a -> {
            dbCombo.addItem(a);
        });
    }

    public void updateTableCombo() {
        tableCombo.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command", "Get Tables");
        message.put("value", dbCombo.getSelectedItem().toString());
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a -> {
            tableCombo.addItem(a);
        });
    }

    public void updateFields() {
        JSONObject message = new JSONObject();
        message.put("command", "Get Table Values");
        JSONObject message2 = new JSONObject();
        message2.put("database", dbCombo.getSelectedItem().toString());
        message2.put("table", tableCombo.getSelectedItem().toString());
        message.put("value", message2);
        JSONParser jsonParser = new JSONParser();
        JSONObject ans = new JSONObject();
        try {
            ans = (JSONObject) jsonParser.parse(clientServer.send(message.toJSONString()));
        } catch (ParseException f) {
            //System.out.println(-1);
            f.printStackTrace();
        }

        selectedFieldsList.clear();
        unselectedFieldsList.clear();
        selectedFieldsList.addElement("Kiválasztott  mezők");
        unselectedFieldsList.addElement("Kiválasztható  mezők");

        /*JSONObject message2 = new JSONObject();
        message2.put("database", dbCombo.getSelectedItem().toString());
        message2.put("table", tableCombo.getSelectedItem().toString());*/

        JSONObject messageKey = new JSONObject();
        messageKey.put("command","Get Primary Key");
        messageKey.put("value", message2);
        String answKey = clientServer.send(messageKey.toJSONString());


        order=new String();
        index = new HashMap<>();
        show = new ArrayList<>();
        orderArray = new ArrayList<>();
        show.add(Boolean.FALSE);
        index.put(answKey,0);
        orderArray.add(answKey);
        unselectedFieldsList.addElement(answKey);
        int i=1;
        for (Object a : ans.keySet())
        if(!a.toString().equals(answKey)){
            unselectedFieldsList.addElement(a.toString());
            order+=a.toString()+",";
            orderArray.add(a.toString());
            index.put(a.toString(), i);
            show.add(Boolean.FALSE);
            ++i;
        }
    }

    public void initializeFilters(){
        filterPanels = new ArrayList<>();
        filter = new JPanel();
        filter.setLayout(new BoxLayout(filter,BoxLayout.Y_AXIS));
        addFilter.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        filter.add(addFilter);
        filter.add(Box.createRigidArea(new Dimension(0, 20)));
        //filter.setPreferredSize(new Dimension(400,20));

        filterScroll = new JScrollPane(filter);
        filterScroll.setPreferredSize(new Dimension(400,400));
        filterScroll.setAutoscrolls(true);

    }

    public JSONObject toJSONMessage() {
        JSONObject bigMessage = new JSONObject();
        bigMessage.put("command","Select");
        JSONObject message = new JSONObject();
        //System.out.println(dbCombo.getSelectedItem().toString());
        message.put("database", dbCombo.getSelectedItem().toString());
        //System.out.println(tableCombo.getSelectedItem().toString());
        message.put("table", tableCombo.getSelectedItem().toString());
        JSONArray messageArray = new JSONArray();
        for(filterPanel f : filterPanels) {
            messageArray.add(f.toJSONObject());
        //    System.out.println(f.toJSONObject().toString());
        }
        message.put("filters", messageArray);
        message.put("order", order);
        bigMessage.put("value", message);

        return bigMessage;
    }

    private void showAnswer(String ans) {
        answerFrame = new JFrame();
        answerPanel = new JPanel();
        answerScroll = new JScrollPane(answerPanel);
        answerScroll.setPreferredSize(new Dimension(400,400));
        answerScroll.setAutoscrolls(true);

        JSONObject message2 = new JSONObject();
        message2.put("database", dbCombo.getSelectedItem().toString());
        message2.put("table", tableCombo.getSelectedItem().toString());

        JSONObject messageKey = new JSONObject();
        messageKey.put("command","Get Primary Key");
        messageKey.put("value", message2);
        String answKey = clientServer.send(messageKey.toJSONString());

        ans+=",";
        String[] datas = ans.split(",,");
        Set<String>dataSet = new HashSet<>();
        Arrays.stream(datas).forEach(d->{
            String[] values = d.split(",");
            String keep = "";
            for (int i = 0; i < values.length; i++)
                if (show.get(i)) keep += values[i]+",";

            dataSet.add(keep);
        });

        int colNumber = selectedFieldsList.size()-1;
//        System.out.println(colNumber);
        int rowNumber = dataSet.size()+1;
//        System.out.println(rowNumber);

        answerPanel.setLayout(new GridLayout(rowNumber, colNumber));

        //System.out.println(orderArray);
        //System.out.println(show);
        //System.out.println(index);

        if(show.get(index.get(answKey))){
            JLabel label = new JLabel(answKey);
            label.setFont(new Font("Serif", Font.BOLD, 25));
            label.setBorder(BorderFactory.createLineBorder(Color.black));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            answerPanel.add(label);
        }
        for (int i = 0; i < orderArray.size(); i++)
        if(show.get(i) && !answKey.equals(orderArray.get(i))){
//            System.out.println(orderArray.get(i));
            JLabel label = new JLabel(orderArray.get(i));
            label.setFont(new Font("Serif", Font.BOLD, 25));
            label.setBorder(BorderFactory.createLineBorder(Color.black));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            answerPanel.add(label);
        }

        dataSet.forEach(d->{
            String[] values = d.split(",");
            for (int i = 0; i < values.length; i++) {
                JLabel label = new JLabel(values[i].toString());
                label.setFont(new Font("Serif", Font.BOLD, 25));
                label.setBorder(BorderFactory.createLineBorder(Color.black));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                answerPanel.add(label);
            }
        });

        answerFrame.add(answerScroll);
        answerFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        answerFrame.setBounds(1200,100,500,500);
        answerFrame.setVisible(true);
    }
}
