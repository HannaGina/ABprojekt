import hello.JSONArray;
import hello.JSONObject;
import hello.parser.JSONParser;
import hello.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

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

    //projection
    private DefaultListModel<String> selectedFieldsList;
    private DefaultListModel<String> unselectedFieldsList;
    private JList<String> selectedFields;
    private JList<String> unselectedFields;
    private JScrollPane selectedScroll;
    private JScrollPane unselectedScroll;
    private JButton left;
    private JButton right;

    //select
    private JButton addFilter;
    private JPanel filter;
    private JScrollPane filterScroll;
    private List<filterPanel> filterPanels;

    //join
    private JButton addJoin;
    private JButton finishJoin;
    private JPanel join;
    private JScrollPane joinScroll;
    private ArrayList<String> notJoinedTables;
    private ArrayList<String> joinedTables;
    private List<joinPanel> joinPanels;

    //group by
    private JButton addGroupBy;
    private JButton finishGroupBy;
    private JPanel groupBy;
    private JScrollPane groupByScroll;
    private List<GroupByPanel> groupByPanels;

    //group by projection
    private JButton addGroupByProjection;
    private JPanel groupByProjection;
    private JScrollPane groupByProjectionScroll;
    private List<GroupProjectionPanel> groupByProjectionPanels;

    private JPanel database;
    private JPanel projection;
    private JPanel from;
    private JButton selectBtn;
    private String order;
    private Map<String, Integer> index; //the attributes index in the orderArray
    private List<Boolean> show; //which attributes we have to show - orderArray order
    private List<String> orderArray; //initial array of values
    private List<String> orderGroupBy;

    //answer
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
        addJoin = new JButton("Join hozzáadása");
        addJoin.setFont(new Font("Serif", Font.BOLD, 25));
        finishJoin = new JButton("Joinok lezarasa");
        finishJoin.setFont(new Font("Serif", Font.BOLD, 25));
        addGroupBy = new JButton("Group By hozzáadása");
        addGroupBy.setFont(new Font("Serif", Font.BOLD, 25));
        finishGroupBy = new JButton("Group By lezarasa");
        finishGroupBy.setFont(new Font("Serif", Font.BOLD, 25));
        addGroupByProjection = new JButton("Group By projekció hozzáadása");
        addGroupByProjection.setFont(new Font("Serif", Font.BOLD, 25));

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
        groupByPanels = new ArrayList<>();
        groupByProjectionPanels = new ArrayList<>();

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
        selectedScroll = new JScrollPane(selectedFields);
        unselectedScroll = new JScrollPane(unselectedFields);
        selectedScroll.setPreferredSize(new Dimension(300, 110));
        unselectedScroll.setPreferredSize(new Dimension(300, 110));
        selectedScroll.setAutoscrolls(true);
        unselectedScroll.setAutoscrolls(true);
        notJoinedTables = new ArrayList<>();
        joinedTables = new ArrayList<>();

        selectedFieldsList.addElement("Kiválasztott  mezők");
        unselectedFieldsList.addElement("Kiválasztható  mezők");

        selectBtn.setEnabled(false);
        addFilter.setEnabled(false);
        addGroupBy.setEnabled(false);
        finishGroupBy.setEnabled(false);

        updateDbCombo();
        updateTableCombo();
        updateFields();
        initializeJoins();
        initializeFilters();
        initializeGroupBy();
        initializeGroupByProjection();

        //joinedTables.add(tableCombo.getSelectedItem().toString());
        //notJoinedTables.remove(tableCombo.getSelectedItem().toString());

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
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    joinedTables.forEach(j -> notJoinedTables.add(j));
                    joinedTables.clear();
                    updateFields();
                    mainPanel.remove(groupByScroll);
                    mainPanel.remove(filterScroll);
                    mainPanel.remove(joinScroll);
                    mainPanel.remove(groupByProjectionScroll);
                    initializeJoins();
                    initializeFilters();
                    initializeGroupBy();
                    initializeGroupByProjection();
                    mainPanel.add(groupByProjectionScroll);
                    mainPanel.add(joinScroll);
                    mainPanel.add(filterScroll);
                    mainPanel.add(groupByScroll);
                    selectBtn.setEnabled(false);
                    validate();
                    repaint();
                }
            }
        });

        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer> torlendo = new ArrayList<>();
                int i = unselectedFields.getSelectedIndex();
                while (i != -1) {
                    if (i != 0) torlendo.add(i);
                    unselectedFields.removeSelectionInterval(i, i);
                    i = unselectedFields.getSelectedIndex();
                }

                while (!torlendo.isEmpty()) {
                    int l = torlendo.size() - 1;
                    selectedFieldsList.addElement(unselectedFieldsList.get(torlendo.get(l)));
                    show.set(index.get(unselectedFieldsList.get(torlendo.get(l))), Boolean.TRUE);
                    unselectedFieldsList.remove(torlendo.get(l));
                    torlendo.remove(l);
                }
            }
        });

        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer> torlendo = new ArrayList<>();
                int i = selectedFields.getSelectedIndex();
                while (i != -1) {
                    if (i != 0) torlendo.add(i);
                    selectedFields.removeSelectionInterval(i, i);
                    i = selectedFields.getSelectedIndex();
                }

                while (!torlendo.isEmpty()) {
                    int l = torlendo.size() - 1;
                    unselectedFieldsList.addElement(selectedFieldsList.get(torlendo.get(l)));
                    show.set(index.get(selectedFieldsList.get(torlendo.get(l))), Boolean.FALSE);
                    selectedFieldsList.remove(torlendo.get(l));
                    torlendo.remove(l);
                }
            }
        });


        addFilter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArrayList<String> tables = new ArrayList<>();
                tables.add(tableCombo.getSelectedItem().toString());
                filterPanel f = new filterPanel(dbCombo.getSelectedItem().toString(), joinedTables, clientServer);
                filter.add(f);
                filter.add(Box.createRigidArea(new Dimension(0, 10)));
                filterPanels.add(f);
                filter.validate();
                filter.repaint();
                filterScroll.validate();
                filterScroll.repaint();
            }
        });

        addJoin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (notJoinedTables.size() > 1) {
                    ArrayList<String> tables = new ArrayList<>();
                    String lastJoined;
                    if (!joinPanels.isEmpty()) {
                        lastJoined = joinPanels.get(joinPanels.size() - 1).getJoined();
                        joinPanels.get(joinPanels.size() - 1).blockateThis();
                    } else lastJoined = tableCombo.getSelectedItem().toString();
                    joinedTables.add(lastJoined);
                    notJoinedTables.remove(lastJoined);

                    joinPanel j = new joinPanel(dbCombo.getSelectedItem().toString(), notJoinedTables, joinedTables, clientServer);
                    join.add(j);
                    join.add(Box.createRigidArea(new Dimension(0, 10)));
                    joinPanels.add(j);
                    join.validate();
                    join.repaint();
                    joinScroll.validate();
                    joinScroll.repaint();

                    updateFields();
                }
            }
        });

        finishJoin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String lastJoined;
                if (!joinPanels.isEmpty()) {
                    lastJoined = joinPanels.get(joinPanels.size() - 1).getJoined();
                    joinPanels.get(joinPanels.size() - 1).blockateThis();
                } else lastJoined = tableCombo.getSelectedItem().toString();
                joinedTables.add(lastJoined);
                notJoinedTables.remove(lastJoined);

                addJoin.setEnabled(false);
                finishJoin.setEnabled(false);
                addGroupBy.setEnabled(true);
                finishGroupBy.setEnabled(true);
                addFilter.setEnabled(true);
                updateFields();
            }
        });

        addGroupBy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArrayList<String> tables = new ArrayList<>();
                tables.add(tableCombo.getSelectedItem().toString());
                GroupByPanel f = new GroupByPanel(dbCombo.getSelectedItem().toString(), joinedTables, clientServer);
                groupBy.add(f);
                groupBy.add(Box.createRigidArea(new Dimension(0, 10)));
                groupByPanels.add(f);
                groupBy.validate();
                groupBy.repaint();
                groupByScroll.validate();
                groupByScroll.repaint();
            }
        });

        finishGroupBy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (GroupByPanel g : groupByPanels) {
                    g.blockateThis();
                }

                addGroupBy.setEnabled(false);
                finishGroupBy.setEnabled(false);
                selectBtn.setEnabled(true);
                addGroupByProjection.setEnabled(true);
                updateFields();
            }
        });

        addGroupByProjection.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArrayList<String> tables = new ArrayList<>();
                tables.add(tableCombo.getSelectedItem().toString());
                GroupProjectionPanel f = new GroupProjectionPanel(dbCombo.getSelectedItem().toString(), joinedTables, clientServer);
                groupByProjection.add(f);
                groupByProjection.add(Box.createRigidArea(new Dimension(0, 10)));
                groupByProjectionPanels.add(f);
                groupByProjection.validate();
                groupByProjection.repaint();
                groupByProjectionScroll.validate();
                groupByProjectionScroll.repaint();
            }
        });

        selectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JSONObject message = toJSONMessage();

                JSONParser jsonParser = new JSONParser();
                JSONObject answDatas = null;
                try {
                    String s = clientServer.send(message.toJSONString());
//                    System.out.println(s);
                    answDatas = (JSONObject) jsonParser.parse(s);
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }

                if (answDatas.get("array").toString().startsWith("H,I,B,A")) {
                    String msg = String.join("", answDatas.get("array").toString().split(","));
                    answerLabel.setText(msg);
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
        projection.add(selectedScroll);
        projection.add(left);
        projection.add(right);
        projection.add(unselectedScroll);


        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(database);
        mainPanel.add(from);
        mainPanel.add(projection);
        mainPanel.add(groupByProjectionScroll);
        mainPanel.add(joinScroll);
        mainPanel.add(filterScroll);
        mainPanel.add(groupByScroll);

        controlPanel = new JPanel();
        controlPanel.add(selectBtn);
        controlPanel.add(answerLabel);

        setLayout(new BorderLayout());
        add(menuBtn, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    public void updateDbCombo() {
        dbCombo.removeAllItems();
        JSONObject message = new JSONObject();
        message.put("command", "Get Databases");
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a -> {
            dbCombo.addItem(a);
        });
        selectBtn.setEnabled(false);
    }

    public void updateTableCombo() {
        tableCombo.removeAllItems();
        notJoinedTables.clear();
        joinedTables.clear();
        JSONObject message = new JSONObject();
        message.put("command", "Get Tables");
        message.put("value", dbCombo.getSelectedItem().toString());
        String answer = clientServer.send(message.toJSONString());
        Arrays.stream(answer.split(",")).forEach(a -> {
            tableCombo.addItem(a);
            notJoinedTables.add(a);
        });
        selectBtn.setEnabled(false);
    }

    public void updateFields() {

        selectedFieldsList.clear();
        unselectedFieldsList.clear();
        selectedFieldsList.addElement("Kiválasztott  mezők");
        unselectedFieldsList.addElement("Kiválasztható  mezők");

        order = new String();
        index = new HashMap<>();
        show = new ArrayList<>();
        orderArray = new ArrayList<>();
        orderGroupBy = new ArrayList<>();

        for (String table : joinedTables) {
            JSONObject message = new JSONObject();
            message.put("command", "Get Table Values");
            JSONObject message2 = new JSONObject();
            message2.put("database", dbCombo.getSelectedItem().toString());
            message2.put("table", table);
            message.put("value", message2);
            JSONParser jsonParser = new JSONParser();
            JSONObject ans = new JSONObject();
            try {
                ans = (JSONObject) jsonParser.parse(clientServer.send(message.toJSONString()));
            } catch (ParseException f) {
                //System.out.println(-1);
                f.printStackTrace();
            }

        /*JSONObject message2 = new JSONObject();
        message2.put("database", dbCombo.getSelectedItem().toString());
        message2.put("table", tableCombo.getSelectedItem().toString());*/

            JSONObject messageKey = new JSONObject();
            messageKey.put("command", "Get Primary Key");
            messageKey.put("value", message2);
            String answKey = clientServer.send(messageKey.toJSONString());

            String ak = table + "." + answKey;
            show.add(Boolean.FALSE);
            index.put(ak, orderArray.size());
            orderArray.add(ak);
            orderGroupBy.add(ak);
            unselectedFieldsList.addElement(ak);
            int i = orderArray.size();
            for (Object a : ans.keySet()) {
                String attr = table + "." + a.toString();
                if (!attr.equals(ak)) {
                    unselectedFieldsList.addElement(attr);
                    order += attr + ",";
                    orderArray.add(attr);
                    orderGroupBy.add(attr);
                    index.put(attr, i);
                    show.add(Boolean.FALSE);
                    ++i;
                }
            }
        }
        if (!groupByPanels.isEmpty()) {
            order = new String();
            index = new HashMap<>();
            show = new ArrayList<>();
            orderArray = new ArrayList<>();
            unselectedFieldsList.clear();
            unselectedFieldsList.addElement("Kiválasztható  mezők");

            int i = 0;
            for (GroupByPanel g : groupByPanels) {
                String attr = g.toString();
                unselectedFieldsList.addElement(attr);
                order += attr + ",";
                orderArray.add(attr);
                index.put(attr, i);
                show.add(Boolean.FALSE);
                ++i;
            }
        }

        //System.out.println(order);
    }

    public void initializeFilters() {
        filterPanels = new ArrayList<>();
        filter = new JPanel();
        filter.setLayout(new BoxLayout(filter, BoxLayout.Y_AXIS));
        addFilter.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        filter.add(addFilter);
        filter.add(Box.createRigidArea(new Dimension(0, 20)));
        //filter.setPreferredSize(new Dimension(400,20));

        filterScroll = new JScrollPane(filter);
        filterScroll.setPreferredSize(new Dimension(400, 200));
        filterScroll.setAutoscrolls(true);
    }

    public void initializeJoins() {
        joinPanels = new ArrayList<>();
        join = new JPanel();
        join.setLayout(new BoxLayout(join, BoxLayout.Y_AXIS));

        addJoin.setEnabled(true);
        addJoin.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        join.add(addJoin);
        join.add(Box.createRigidArea(new Dimension(0, 10)));

        finishJoin.setEnabled(true);
        finishJoin.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        join.add(finishJoin);
        join.add(Box.createRigidArea(new Dimension(0, 20)));
        //join.setPreferredSize(new Dimension(400,20));

        joinScroll = new JScrollPane(join);
        joinScroll.setPreferredSize(new Dimension(400, 200));
        joinScroll.setAutoscrolls(true);
    }

    public void initializeGroupBy() {
        groupByPanels = new ArrayList<>();
        groupBy = new JPanel();
        groupBy.setLayout(new BoxLayout(groupBy, BoxLayout.Y_AXIS));
        addGroupBy.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        addGroupBy.setEnabled(false);
        finishGroupBy.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        finishGroupBy.setEnabled(false);
        groupBy.add(addGroupBy);
        groupBy.add(Box.createRigidArea(new Dimension(0, 10)));
        groupBy.add(finishGroupBy);
        groupBy.add(Box.createRigidArea(new Dimension(0, 20)));
        //filter.setPreferredSize(new Dimension(400,20));

        groupByScroll = new JScrollPane(groupBy);
        groupByScroll.setPreferredSize(new Dimension(400, 200));
        groupByScroll.setAutoscrolls(true);
    }

    public void initializeGroupByProjection() {
        groupByProjectionPanels = new ArrayList<>();
        groupByProjection = new JPanel();
        groupByProjection.setLayout(new BoxLayout(groupByProjection, BoxLayout.Y_AXIS));
        addGroupByProjection.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        addGroupByProjection.setEnabled(false);
        groupByProjection.add(addGroupByProjection);
        groupByProjection.add(Box.createRigidArea(new Dimension(0, 10)));

        groupByProjectionScroll = new JScrollPane(groupByProjection);
        groupByProjectionScroll.setPreferredSize(new Dimension(400, 200));
        groupByProjectionScroll.setAutoscrolls(true);
    }

    public JSONObject toJSONMessage() {
        JSONObject bigMessage = new JSONObject();
        bigMessage.put("command", "Select");
        JSONObject message = new JSONObject();
        //System.out.println(dbCombo.getSelectedItem().toString());
        message.put("database", dbCombo.getSelectedItem().toString());
        //System.out.println(tableCombo.getSelectedItem().toString());
        message.put("table", tableCombo.getSelectedItem().toString());
        JSONArray messageArray = new JSONArray();
        JSONArray messageArrayJoin = new JSONArray();
        JSONArray messageArrayProjection = new JSONArray();
        JSONArray messageArrayJoinTables = new JSONArray();
        JSONArray messageArrayGroupBy = new JSONArray();
        JSONArray messageArrayGroupByProjection = new JSONArray();
        for (filterPanel f : filterPanels) {
            messageArray.add(f.toJSONObject());
            //    System.out.println(f.toJSONObject().toString());
        }
        for (joinPanel j : joinPanels) {
            messageArrayJoin.add(j.toJSONObject());
        }

        for (String t : joinedTables) {
            messageArrayJoinTables.add(t);
        }

        for (GroupByPanel g : groupByPanels) {
            messageArrayGroupBy.add(g.toJSONObject());
        }
        
        for (GroupProjectionPanel g : groupByProjectionPanels) {
            messageArrayGroupByProjection.add(g.toJSONObject());
        }

        for (int i = 0; i < selectedFieldsList.size(); i++) {
            messageArrayProjection.add(selectedFieldsList.get(i));
        }
        message.put("projections", messageArrayProjection);
        message.put("filters", messageArray);
        message.put("joins", messageArrayJoin);
        message.put("joinTables", messageArrayJoinTables);
        message.put("groupBy", messageArrayGroupBy);
        message.put("groupByOrder", orderGroupBy.toString());
        message.put("groupByProjection", messageArrayGroupByProjection);
        bigMessage.put("value", message);

        return bigMessage;
    }

    private void showAnswer(JSONObject answ) {
        answerFrame = new JFrame();
        answerPanel = new JPanel();
        answerScroll = new JScrollPane(answerPanel);
        answerScroll.setPreferredSize(new Dimension(400, 400));
        answerScroll.setAutoscrolls(true);
        System.out.println(answ);

        if (answ.get("onlyIndex").toString().equals("1")) //just an index in the projection, and no selection
        {
            String ans = answ.get("array").toString();
            //        System.out.println(ans);
            //        ans = ans.substring(1, ans.length() - 1);
            String[] datas = ans.split(",");

            int colNumber = 1;
            int rowNumber = datas.length + 1;
            answerPanel.setLayout(new GridLayout(rowNumber, colNumber));
            JLabel label = new JLabel(selectedFieldsList.get(1));
            label.setFont(new Font("Serif", Font.BOLD, 25));
            label.setBorder(BorderFactory.createLineBorder(Color.black));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            answerPanel.add(label);

            for (String d : datas) {
                label = new JLabel(d);
                label.setFont(new Font("Serif", Font.BOLD, 25));
                label.setBorder(BorderFactory.createLineBorder(Color.black));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                answerPanel.add(label);
            }
        } else {
            String ans = answ.get("array").toString();

            List<String>ordAr = new ArrayList<>(orderArray);
            List<Boolean>shw = new ArrayList<>(show);
            for (GroupProjectionPanel g : groupByProjectionPanels) {
                ordAr.add(g.toString());
                shw.add(Boolean.TRUE);   
            }
            
            ans += ",";
            String[] datas = ans.split(",,");
            Set<String> dataSet = new HashSet<>();
            Arrays.stream(datas).forEach(d -> {
                String[] values = d.split(",");
                String keep = "";
                for (int i = 0; i < values.length; i++)
                    if (shw.get(i)) keep += values[i] + ",";

                dataSet.add(keep);
            });
            

            int colNumber = selectedFieldsList.size() - 1 + groupByProjectionPanels.size();
//        System.out.println(colNumber);
            int rowNumber = dataSet.size() + 1;
//        System.out.println(rowNumber);

            answerPanel.setLayout(new GridLayout(rowNumber, colNumber));

            //System.out.println(ordAr);
            
            for (int i = 0; i < ordAr.size(); i++)
                if (shw.get(i)) {
//            System.out.println(ordAr.get(i));
                    JLabel label = new JLabel(ordAr.get(i));
                    label.setFont(new Font("Serif", Font.BOLD, 25));
                    label.setBorder(BorderFactory.createLineBorder(Color.black));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    answerPanel.add(label);
                }

            dataSet.forEach(d -> {
                String[] values = d.split(",");
                for (int i = 0; i < values.length; i++) {
                    JLabel label = new JLabel(values[i].toString());
                    label.setFont(new Font("Serif", Font.BOLD, 25));
                    label.setBorder(BorderFactory.createLineBorder(Color.black));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    answerPanel.add(label);
                }
            });
        }


        answerFrame.add(answerScroll);
        answerFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        answerFrame.setBounds(1200, 100, 500, 500);
        answerFrame.setVisible(true);
    }
}
