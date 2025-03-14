package client;

import server.Message;
import server.jacksonclasses.Database;
import server.jacksonclasses.Table;
import server.mongobongo.DataTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static java.lang.System.exit;

public class KliensNew extends JFrame implements Runnable {

    private final ObjectExplorer leftPanel;
    private final SidePanel rightPanel;
    private final SidePanel topPanel;
    private final JTabbedPane tabbedPane;
    private final JTabbedPane rightPanelTabs;
    private final JPanel queryPanelOptions;
    private final JPanel visualQueryDesignerOptions;
    private final JComponent QueryPanel;
    private JComponent VisualQueryDesigner;
    private final JScrollPane scrollTextResp = new JScrollPane();
    private JTextArea textArea;

    private Message clientMessage;
    private final JTextArea textAreas = new JTextArea();
    private JTextArea outText = new JTextArea();
    private boolean connected = false;
    private boolean send = false;
    private JButton connectionButton;
    private int currentTabId = -1;
    private QueryPanel currentQueryPanel;
    private final Syntax syntax;
    private int tabsCounter;

    private final ArrayList<String> databases;
    private final ArrayList<Table> tableObjects;
    private final ArrayList<Database> databaseObjects;

    private ArrayList<DataTable> dataTables;

    public KliensNew() {
//        InitQueryPanel();
//        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setTitle("AB: Client");
        this.setSize(1000, 700);
        this.setLocationRelativeTo(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setIconImage(new ImageIcon("src/main/resources/icons/ablogo512.jpg").getImage());

        clientMessage = new Message();
        dataTables = new ArrayList<>();
        tableObjects = new ArrayList<>();
        databaseObjects = new ArrayList<>();
        databases = new ArrayList<>();
        syntax = new Syntax(this);
        tabsCounter = 0;
        leftPanel = new ObjectExplorer(this);
        rightPanel = new SidePanel(this);
        topPanel = new SidePanel(this);
        tabbedPane = new JTabbedPane();
        rightPanelTabs = new JTabbedPane();
        QueryPanel = new JPanel();
        VisualQueryDesigner = new JPanel();
        queryPanelOptions = new JPanel();
        visualQueryDesignerOptions = new JPanel();

        rightPanelTabs.setPreferredSize(new Dimension(300, 700));
        rightPanel.setLayout(new GridLayout(1, 10));
        rightPanelTabs.setEnabled(false);
        rightPanel.add(rightPanelTabs);
        rightPanelTabs.setVisible(false);


        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        textArea.setBorder(BorderFactory.createLineBorder(Color.black));

        JScrollPane scrollText = new JScrollPane(textArea);

        outText = new JTextArea();
        outText.setEditable(false);
        outText.setText("welcome friend!");
        outText.setBorder(BorderFactory.createLineBorder(Color.black));

        JScrollPane scrollTextResp = new JScrollPane(outText);
        QueryPanel.setLayout(new BoxLayout(QueryPanel, BoxLayout.Y_AXIS));


        QueryPanel.add(scrollText);

        JButton connectionButton = new JButton("Connect") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (connected) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                g.fillOval(getWidth() / 10, getHeight() / 2 - 1, 3, 3);
            }
        };

        JButton saveDocument = new JButton("Save Document");
        JButton loadDocument = new JButton("Load Document");
        saveDocument.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Document");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write(textArea.getText());
                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        loadDocument.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Document");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }
                    textArea.setText(stringBuilder.toString());
                    reader.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        leftPanel.addButtons(saveDocument);
        leftPanel.add(loadDocument);

        JButton execButton = new JButton("Execute");
        JButton closeTab = new JButton("Close this Tab");
        JButton clear = new JButton("Clear");
        JButton exit = new JButton("Exit");
        JButton newQuery = new JButton("New Query");

        JButton newVisualQueryDesigner = new JButton("Visual Query Designer");
        topPanel.add(connectionButton);
        topPanel.add(newQuery);
        topPanel.add(newVisualQueryDesigner);
        topPanel.add(exit);



        queryPanelOptions.add(execButton);
        queryPanelOptions.add(clear);
        queryPanelOptions.add(closeTab);
        EventsAndActions();
        ButtonEventsAndActions(connectionButton, execButton, closeTab, clear, exit, newVisualQueryDesigner, newQuery);
        resizeWindowLayout();

        this.add(leftPanel);
        this.add(rightPanel);
        this.add(topPanel);
        this.add(tabbedPane);

        this.setVisible(true);
    }


    private void configVisualQueryDesignerOptions() {

        VisualQueryDesigner = new JPanel();

        VisualQueryDesigner.setBackground(new Color(115, 34, 34));
        int width = visualQueryDesignerOptions.getWidth();
        int height = visualQueryDesignerOptions.getHeight();

        VisualQueryDesigner.setLayout(new BoxLayout(VisualQueryDesigner, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setBounds(50, 50, width - 50, height - 50);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton createTable = new JButton("Insert / Delete");
        JButton selectTable = new JButton("Select");
        JButton closeTab = new JButton("Close Tab");

        createTable.setEnabled(false);
        selectTable.setEnabled(false);

        final int[] Selected = {0};

        JComboBox<String> comboBoxDatabase = new JComboBox<>();
        JComboBox<String> comboBoxTables = new JComboBox<>();

//        if comboBoxDatabase is selected the selected parameter increase by 1
        comboBoxDatabase.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Selected[0]++;
                System.out.println("SELECTED: " + Selected[0]);

                if (Selected[0] == 2) {
                    createTable.setEnabled(true);
                    selectTable.setEnabled(true);
                }
            }
        });

        comboBoxTables.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (comboBoxTables.getSelectedIndex() != -1) {
                    Selected[0]++;
                }
                System.out.println("SELECTED: " + Selected[0]);

                if (Selected[0] == 2) {
                    createTable.setEnabled(true);
                    selectTable.setEnabled(true);
                }
            }
        });


        panel.add(createTable, BorderLayout.NORTH);
        panel.add(selectTable, BorderLayout.NORTH);
        panel.add(closeTab, BorderLayout.SOUTH);

        panel.add(comboBoxDatabase, BorderLayout.WEST);
        panel.add(comboBoxTables, BorderLayout.EAST);

        for (Database database : databaseObjects) {
            System.out.println(database.get_dataBaseName());
            comboBoxDatabase.addItem(database.get_dataBaseName());
        }

        if (comboBoxDatabase.getItemCount() > 0) {
            comboBoxDatabase.setSelectedIndex(0);
        }

        comboBoxDatabase.addActionListener(e -> {
            comboBoxTables.removeAllItems();
            Database database = databaseObjects.get(comboBoxDatabase.getSelectedIndex());
            for (Table table : database.getTables()) {
                comboBoxTables.addItem(table.get_tableName());
            }
            comboBoxTables.setVisible(comboBoxTables.getItemCount() > 0);
        });


        if (comboBoxTables.getItemCount() > 0) {
            comboBoxTables.setSelectedIndex(0);
        }


        selectTable.addActionListener(e -> {

//            VisualQueryDesigner visualQueryDesigner = tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner ? (VisualQueryDesigner) tabbedPane.getSelectedComponent() : null;

            if (tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner) {
                String databaseName = (String) comboBoxDatabase.getSelectedItem();
                String tableName = (String) comboBoxTables.getSelectedItem();
                clientMessage.setVisualQueryDesignerMessage("getSceleton\r\n" + databaseName + "\r\n" + tableName + "\r\n");
                send();
            }

        });

        createTable.addActionListener(e -> {

            VisualQueryDesigner visualQueryDesigner = tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner ? (VisualQueryDesigner) tabbedPane.getSelectedComponent() : null;


            String query = "use " + comboBoxDatabase.getSelectedItem() + "\n";
            query += "select * from " + comboBoxTables.getSelectedItem() + "\n";
            textArea.setText(query);
            send();

        });

        closeTab.addActionListener(e -> {
            VisualQueryDesigner visualQueryDesigner = tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner ? (VisualQueryDesigner) tabbedPane.getSelectedComponent() : null;
            tabbedPane.remove(visualQueryDesigner);
            revalidate();
        });


//        visualQueryDesignerOptions.add(comboBoxDatabase);
//        visualQueryDesignerOptions.add(comboBoxTables);
//        visualQueryDesignerOptions.add(button);
        visualQueryDesignerOptions.add(panel);
        visualQueryDesignerOptions.setVisible(true);
        validate();

    }

    private void processMessage(Message mess) {

        System.out.println("processMessage");
        System.out.println("mess.isMessageUserEmpy(): " + mess.isMessageUserEmpy());
        System.out.println("mess.isMessageServerEmpy(): " + mess.isMessageServerEmpy());
        System.out.println("mess.isDatabasesEmpty(): " + mess.isDatabasesEmpty());
        System.out.println("mess Message: " + mess.getMessageUser() + " \n" + mess.getMessageServer() + " \n" + mess.getDatabases() + " ");

        this.dataTables = mess.getDataTables();

        if (dataTables != null) {
            for (DataTable dataTable : dataTables) {
                System.out.println("dataTable: " + dataTable.getTableName());
            }
        }

        if (!mess.isMessageUserEmpy()) {
            System.out.println("mess.getMessageUser(): " + mess.getMessageUser());
            outText.setText(outText.getText() + "\n" + mess.getMessageUser());
            if (currentQueryPanel != null)
                currentQueryPanel.setOutText(mess.getMessageUser());
//            QueryPanel
        }

        DataTable vqdDataTable = mess.getVqdTableSkeleton();

        if (vqdDataTable != null) {
            System.out.println("vqdDataTable: " + vqdDataTable.getTableName());
            if (tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner visualQueryDesigner) {
                System.out.println("VisualQueryDesigner SELECTED SKELTON");
                visualQueryDesigner.selectTable(vqdDataTable);
            }
        }

        DataTable selectedDataTable = mess.getSelectedDataTable();

        if (selectedDataTable != null) {
            System.out.println("selectedDataTable: " + selectedDataTable.getTableName());
//            if (currentQueryPanel != null)
//                currentQueryPanel.setDataTableToOut(selectedDataTable);
//            tabbedPane.getSelectedComponent()
            if (tabbedPane.getSelectedComponent() instanceof QueryPanel queryPanel) {
                queryPanel.setDataTableToOut(selectedDataTable);
                revalidate();

            }

            if (tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner visualQueryDesigner) {

                visualQueryDesigner.createTable(selectedDataTable);
            }

        }


        if (!mess.isMessageServerEmpy()) {

        }


        if (!mess.isDatabasesEmpty()) {
            System.out.println("mess.getDatabases(): " + mess.getDatabases());
            databases.clear();
            databases.addAll(mess.getDatabases());
            databaseObjects.clear();
            databaseObjects.addAll(mess.getDatabaseObjects());

            leftPanel.repaint();
            resizeWindowLayout();
            visualQueryDesignerOptions.removeAll();
            visualQueryDesignerOptions.validate();
            configVisualQueryDesignerOptions();
            validate();

        }
        for (Table s : mess.getTables()) {
            System.out.println("mess.getTables(): " + s.get_tableName());

        }
        if (!mess.isTablesEmpty()) {
            System.out.println("mess.getTables(): " + mess.getTables());
            tableObjects.clear();
            tableObjects.addAll(mess.getTables());
        }

    }

    private void resizeWindowLayout() {

        leftPanel.resizePanel(0, getHeight() / 8, getWidth() / 4, getHeight());
        rightPanel.resizePanel(getWidth() - getWidth() / 4, getHeight() / 8, getWidth() / 4, getHeight());
        topPanel.resizePanel(0, 0, getWidth(), getHeight() / 8);
        tabbedPane.setBounds(getWidth() / 4, getHeight() / 8, getWidth() - getWidth() / 2, getHeight() - getHeight() / 6);
        QueryPanel.setBounds(getWidth() / 4, getHeight() / 8, getWidth() - getWidth() / 2, getHeight() - getHeight() / 6);
        VisualQueryDesigner.setBounds(getWidth() / 4, getHeight() / 8, getWidth() - getWidth() / 2, getHeight());
        scrollTextResp.setBounds(getWidth() / 4, getHeight() / 8, getWidth() - getWidth() / 2, getHeight() - getHeight() / 4);
    }

    private void ButtonEventsAndActions(JButton connectionButton, JButton execButton, JButton closeTab, JButton clear, JButton exit, JButton newVisualQueryDesigner, JButton newQuery) {

        tabbedPane.addChangeListener(e1 -> {

            if (tabbedPane.getSelectedComponent() instanceof QueryPanel) {
                rightPanelTabs.removeAll();
                rightPanelTabs.addTab("Query Opt", queryPanelOptions);
                rightPanelTabs.validate();
                rightPanelTabs.setVisible(true);
                validate();

                outText = ((QueryPanel) tabbedPane.getSelectedComponent()).getOutText();
            } else if (tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner) {
                rightPanelTabs.removeAll();
                rightPanelTabs.addTab("VQD Opt", visualQueryDesignerOptions);
                rightPanelTabs.setVisible(true);
                rightPanelTabs.validate();
                validate();

            } else {
                rightPanelTabs.setVisible(false);
            }
        });

        newQuery.addActionListener(e -> {
            String tabName = "Query " + tabsCounter;
            JComponent queryPanel = new QueryPanel(this, tabbedPane);
            tabbedPane.addTab(tabName, queryPanel);
            tabsCounter++;
//            rightPanelTabs.setSelectedIndex(0);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

        });


        newVisualQueryDesigner.addActionListener(e -> {
            String tabName = "VQD " + tabsCounter;
            JComponent VisualQueryDesigner = new VisualQueryDesigner(this);
            tabbedPane.addTab(tabName, VisualQueryDesigner);
            tabsCounter++;
//            rightPanelTabs.setSelectedIndex(1);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

        });

        clear.addActionListener(e -> {
            textArea.setText("");
        });

        closeTab.addActionListener(e -> {
//                remove QueryPanel
            QueryPanel queryPanel = tabbedPane.getSelectedComponent() instanceof QueryPanel ? (QueryPanel) tabbedPane.getSelectedComponent() : null;
            if (queryPanel != null) {
                tabbedPane.remove(queryPanel);
                tabsCounter--;
                revalidate();
            }

        });

        connectionButton.addActionListener(e -> {
            System.out.println("Connect");
            if (connectionButton.getText().equals("Connect")) {
                connectionButton.setText("Disconnect");
                rightPanelTabs.setVisible(true);
                if (tabbedPane.getTabCount() != 0) {
                    if (tabbedPane.getSelectedComponent() instanceof QueryPanel) {

                        outText = ((QueryPanel) tabbedPane.getSelectedComponent()).getOutText();
                    } else if (tabbedPane.getSelectedComponent() instanceof VisualQueryDesigner) {

                    }
                } else {
                    rightPanelTabs.setVisible(false);
                }
                connected = true;
                new Thread(this).start();

            } else {
                System.out.println("Disconected");
                rightPanelTabs.setVisible(false);
                databases.clear();
                databaseObjects.clear();
                tableObjects.clear();

                connected = false;
                connectionButton.setText("Connect");
                visualQueryDesignerOptions.removeAll();
                configVisualQueryDesignerOptions();

            }
//            resizeWindowLayout();
        });

        exit.addActionListener(e -> {
            System.out.println("Exit");

            exit(0);

        });

        textArea.addKeyListener(new KeyAdapter() {
                                    @Override
                                    public void keyReleased(KeyEvent e) {

                                        super.keyReleased(e);

                                        if (e.getKeyCode() == KeyEvent.VK_SPACE)
                                            syntax.syntaxHighlighting();

                                    }
                                }
        );
        execButton.setSize(100, 50);
        execButton.addActionListener(e -> {

            int id = tabbedPane.getSelectedIndex();
            QueryPanel q = tabbedPane.getComponentAt(id) instanceof QueryPanel ? (QueryPanel) tabbedPane.getComponentAt(id) : null;
            if (q != null) {
                q.getTextArea().setText(textArea.getText());

            }
            send = true;
        });

        JButton popOut = new JButton("PopOut");
        popOut.setSize(100, 50);
        popOut.addActionListener(e -> {
            QueryPanel queryPanel = tabbedPane.getSelectedComponent() instanceof QueryPanel ? (QueryPanel) tabbedPane.getSelectedComponent() : null;
            if (queryPanel != null) {
               queryPanel.pop();
            }
        });
        queryPanelOptions.add(popOut);


        tabbedPane.addChangeListener(e -> {
            tabbedPane.getSelectedComponent();
            if (tabbedPane.getSelectedComponent() instanceof QueryPanel) {
                currentQueryPanel = (QueryPanel) tabbedPane.getSelectedComponent();
                textArea = currentQueryPanel.getTextArea();
            }
        });


    }

    private void print(String execute, int id) {

    }

    void print(String execute) {
        outText.setText(outText.getText() + "\n" + execute);
    }

    private void EventsAndActions() {
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeWindowLayout();
            }
        });


    }

    public void send() {
        send = true;
    }

    public void setCurrentQueryPanel(QueryPanel queryPanel) {
        this.currentQueryPanel = queryPanel;
    }

    @Override
    public void run() {
//        if (connectToServer() != 0) {
//            connectionButton.setText("Connect");
//        } else {
//            connectionButton.setText("Disconnect");
//
//        }
        try {
            connectSendReceive();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void connectSendReceive() throws IOException {
        String hostName = "localhost";
        int portNumber = 1234;
        System.out.println(hostName + " " + portNumber);
        try (
                Socket clientSocket = new Socket(hostName, portNumber);
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();
                ObjectInputStream in = new ObjectInputStream(inputStream);
                ObjectOutputStream oot = new ObjectOutputStream(outputStream)
        ) {
            System.out.println("Connected to server");
            Message message = new Message();

            while (connected) {

                if (clientSocket.isClosed()) {
                    System.out.println("Socket closed");
                    break;
                }

                if (send) {
                    message = clientMessage;
                    message.setMessageUser(textArea.getText());
                    message.setKlientID(tabbedPane.getSelectedIndex());
                    oot.writeObject(message);
                    oot.flush();
                    clientMessage = new Message();

//                    textArea.setText("");
                    send = false;

                }
                if (inputStream.available() > 0) {
                    System.out.println("Waiting for message");
                    if (clientSocket.isClosed()) {
                        System.out.println("Socket closed");
                        break;
                    }
                    message = null;
                    try {
                        message = (Message) in.readObject();
                        System.out.println("Message received");
                        System.out.println("Message size:"+ getObjectSize(message)+" bytes");
                    } catch (EOFException e) {
                        System.out.println("EOFException");
                        break;
                    }
                    if (message != null) {
                        System.out.println("New message");
                        System.out.println(message.getMessageUser());
                        processMessage(message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    public void setCurrentTabId(int id) {
        currentTabId = id;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(String text) {
        this.textArea.setText(text);
    }

    public void setOptionTabbedPane(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    public static void main(String[] args) {
        new KliensNew();
    }

    public ArrayList<Database> getDatabases() {
        return this.databaseObjects;
    }

    public ArrayList<Table> getTables() {
        return this.tableObjects;
    }

    public Message getCilentMessage() {
        return this.clientMessage;
    }
    public int getObjectSize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        return baos.size();
    }
}