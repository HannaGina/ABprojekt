import hello.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CreateDatabase extends JPanel {
    private JLabel jLabel;
    private JTextField jTextField;
    private JButton createDbBtn;
    private JButton menuBtn;
    private JPanel mainPanel;
    private DBFrame dbFrame;
    private JLabel answerLabel;
    private ClientServer clientServer;

    public CreateDatabase(DBFrame dbFrame,ClientServer cl) {
        clientServer = cl;
        this.dbFrame = dbFrame;

        answerLabel = new JLabel();
        answerLabel.setFont(new Font("Serif",Font.PLAIN, 20));
        jLabel = new JLabel("Adatbazis neve: ");
        jLabel.setFont(new Font("Serif",Font.BOLD, 30));
        jTextField = new JTextField(30);
        jTextField.setFont(new Font("Serif",Font.BOLD, 30));
        createDbBtn = new JButton("Adatbazis letrehozasa");
        createDbBtn.setFont(new Font("Serif",Font.BOLD, 30));
        menuBtn = new JButton("Menu");
        menuBtn.setFont(new Font("Serif",Font.BOLD, 30));

        setLayout(new BorderLayout());

        menuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dbFrame.JumpTo("MenuPanel");
            }
        });

        createDbBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(jTextField.getText().equals("")) {
                    answerLabel.setText("Empty field!");
                } else {
                    JSONObject message = new JSONObject();
                    message.put("command","Create Database");
                    message.put("value",jTextField.getText());
                    answerLabel.setText(clientServer.send(message.toJSONString()));
                }
            }
        });

        add(menuBtn,BorderLayout.NORTH);

        mainPanel = new JPanel();
        mainPanel.add(jLabel);
        mainPanel.add(jTextField);
        mainPanel.add(createDbBtn);
        mainPanel.add(answerLabel);

        add(mainPanel,BorderLayout.CENTER);
    }

    public void doEmpty() {
        jTextField.setText("");
        answerLabel.setText("");
    }
}
