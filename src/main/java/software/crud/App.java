package software.crud;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import software.crud.Models.CodeInput;
import software.crud.Models.CrudMessage;
import software.crud.Models.FinalQueryData;
import software.crud.MySQL.MySQLDBHelper;
import software.crud.MySQL.React_JAVAMySQL;

public class App extends JFrame {
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtDatabase;
    private JList<String> tableList;
    private DefaultListModel<String> tableListModel;
    private JTextField txtPackageName;
    private JButton btnConnect;
    private JTextArea logTextArea;

    public App() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        txtHost = new JTextField(20);
        txtHost.setText("localhost");
        txtPort = new JTextField(10);
        txtPort.setText("3306");
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtDatabase = new JTextField(20);
        tableListModel = new DefaultListModel<>();
        tableList = new JList<>(tableListModel);
        txtPackageName = new JTextField(20);
        txtPackageName.setText("com.packagename");
        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(e -> loadTables());
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.WEST);

        JPanel logPanel = createLogPanel();
        add(logPanel, BorderLayout.CENTER);

        JPanel southPanel = createSouthPanel();
        add(southPanel, BorderLayout.SOUTH);

        // Set background color
        getContentPane().setBackground(Color.WHITE);

        // Add padding to the content pane
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtHost, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPort, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Database:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtDatabase, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnConnect, gbc);

        formPanel.setBorder(BorderFactory.createTitledBorder("Database Connection"));

        return formPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.add(new JScrollPane(tableList));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Tables"));
        return centerPanel;
    }

    private JPanel createSouthPanel() {
        JButton btnSelectAll = new JButton("Select All");
        btnSelectAll.addActionListener(e -> selectAllTables());

        JButton btnUnselectAll = new JButton("Unselect All");
        btnUnselectAll.addActionListener(e -> unselectAllTables());

        JButton btnGenerate = new JButton("Generate API");
        btnGenerate.addActionListener(e -> generateAPI());

        JPanel packagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        packagePanel.add(new JLabel("Package Name:"));
        packagePanel.add(txtPackageName);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnSelectAll);
        buttonPanel.add(btnUnselectAll);
        buttonPanel.add(btnGenerate);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(packagePanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);

        return southPanel;
    }

    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        return logPanel;
    }

    private void loadTables() {
        MySQLDBHelper dbHelper = createDBHelper();

        try {
            dbHelper.connect();
            List<String> tables = dbHelper.getListOfTables();
            populateTableList(tables);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private MySQLDBHelper createDBHelper() {
        return new MySQLDBHelper(
                txtHost.getText(),
                txtPort.getText(),
                txtUsername.getText(),
                new String(txtPassword.getPassword()),
                txtDatabase.getText());
    }

    private void populateTableList(List<String> tables) {
        tableListModel.clear();
        tables.forEach(tableListModel::addElement);
    }

    private void handleException(Exception ex) {
        ex.printStackTrace();
        // You can display an error message to the user here
    }

    private void selectAllTables() {
        tableList.setSelectionInterval(0, tableListModel.size() - 1);
    }

    private void unselectAllTables() {
        tableList.clearSelection();
    }

    private void generateAPI() {
        List<String> selectedTables = tableList.getSelectedValuesList();
        React_JAVAMySQL generator = new React_JAVAMySQL(txtPackageName.getText());
        MySQLDBHelper dbHelper = createDBHelper();

        try {
            CodeInput<FinalQueryData> codeInput = generator.automator(txtPackageName.getText(), selectedTables,
                    dbHelper);
            displayMessages(generator.getMessages());
            generator.createReactApp(codeInput);
            displayMessages(generator.getMessages());
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private void displayMessages(ArrayList<CrudMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            StringBuilder messageText = new StringBuilder();
            messages.forEach(message -> messageText.append(message.getMessage()).append("\n"));
            logTextArea.append(messageText.toString());
            messages.clear();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App gui = new App();
            gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gui.setTitle("CRUD Software - Created by www.lance.name");
            gui.setPreferredSize(new Dimension(800, 600));
            gui.pack();
            gui.setVisible(true);
        });
    }
}