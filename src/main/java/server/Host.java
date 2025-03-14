package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import server.jacksonclasses.Database;
import server.jacksonclasses.Databases;
import server.jacksonclasses.Table;
import server.mongobongo.DataTable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Host {
    private final JSONObject catalog = new JSONObject();
    private String currentDatabase = "";
    private String error;
    private final List<String> elvSzavak;
    private final String acc;

    private final String answer = "";

    private final ArrayList<Message> messages = new ArrayList<>();

    public Host() throws IOException {

        elvSzavak = new ArrayList<>();
        elvSzavak.add("USE");
        elvSzavak.add("CREATE");
        elvSzavak.add("DROP");
        elvSzavak.add("use");
        elvSzavak.add("create");
        elvSzavak.add("drop");
        elvSzavak.add("INSERT");
        elvSzavak.add("insert");
        elvSzavak.add("DELETE");
        elvSzavak.add("select");
        error = "";
        acc = "";

        // Create a file to hold the log
//        File logFile = new File("console.log");
//        FileOutputStream fos = new FileOutputStream(logFile);
//
//        // Redirect System.out to the log file
//        PrintStream ps = new PrintStream(fos);
//        System.setOut(ps);


        Create_load_catalog();
        Create_load_lastCurrentDatabase();
        Create_socket_communication();
    }
    private void Write_lastCurrentDatabase(){
        try {
            Writer writer = new FileWriter("currentdatabase.txt");
            writer.write(currentDatabase);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void Create_load_lastCurrentDatabase(){
        // currentdatabase.txt contains the last used database name
        try {
            BufferedReader br = new BufferedReader(new FileReader("currentdatabase.txt"));
            String line = br.readLine();
            if (line != null) {
                currentDatabase = line;
            }
        } catch (FileNotFoundException e) {
            Writer writer = null;
            try {
                writer = new FileWriter("currentdatabase.txt");
                writer.write("");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void Create_load_catalog() {
        ObjectMapper mapper = new ObjectMapper();

        File file = new File("Catalog.json");
        if (file.length() == 0){
            System.out.println("Catalog is empty, initializing...");
            Databases dbs = new Databases();
            try {
                mapper.writeValue(file, dbs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Starting server...");

    }

    private void handleClient(int portNumber) throws IOException {

            try (
                    ServerSocket serverSocket = new ServerSocket(portNumber);
                    Socket clientSocket = serverSocket.accept();
                    OutputStream outputStream = clientSocket.getOutputStream();
                    ObjectOutputStream outS = new ObjectOutputStream(outputStream);
                    InputStream inputStream = clientSocket.getInputStream();
                    ObjectInputStream inS = new ObjectInputStream(inputStream)
            ) {

                Message message = new Message();
                message.setMessageUser("Welcome to the server!");
                message.setDatabases(new DataBaseNames().getDatabaseNames());
                DataBaseNames dbn = new DataBaseNames();

                ArrayList<Database> databaseArrayList = new ArrayList<>();
                ArrayList<Table> tableArrayList = new ArrayList<>();

                for (String databaseName : dbn.getDatabaseNames()) {
                    System.out.println(databaseName);
                    databaseArrayList.add(dbn.getDatabase(databaseName));
                }
                ArrayList<DataTable> dataTables = new ArrayList<>();
                for (Database db : databaseArrayList) {
                    tableArrayList.addAll(db.getTables());
//                    for (Table table : db.getTables()) {
//                        System.out.println(table.get_tableName());
//                        System.out.println("db: " + db.get_dataBaseName() + " tabla: " + table.get_tableName());
//                        dataTables.add(new DataTable(db.get_dataBaseName(), table.get_tableName()));
                }
//                }
//                message.setDataTables(dataTables);
                message.setTables(tableArrayList);
                message.setDatabases(dbn.getDatabaseNames());
                message.setDatabaseObjects(databaseArrayList);

//                if outs is not connected to the client

                try {
                    outS.writeObject(message);
                    outS.flush();
                } catch (IOException e) {
                    System.out.println("Disconnected from client");
                    System.out.println(e.getMessage());
                    System.out.println(Arrays.toString(e.getStackTrace()));
                    serverSocket.close();
                    return;
                }
                System.out.println("message sent to client: " + message.getMessageUser());
                while (true) {
                    try {
                        if (serverSocket.isClosed()) {
                            System.out.println("Server socket is closed");
                            break;
                        }

                        message = null;
                        try {
                            message = (Message) inS.readObject();
                        } catch (ClassNotFoundException e1) {
                            System.out.println("Disconnected from client");
                            e1.printStackTrace();
                            serverSocket.close();
                            return;
                        }
//                        } catch (IOException | ClassNotFoundException e1) {
//                            System.out.println("Disconnected from client");
//                            System.out.println(e1.getMessage());
//                            System.out.println(Arrays.toString(e1.getStackTrace()));
//                            e1.printStackTrace();
//                            serverSocket.close();
//                            return;
//                        }

                        System.out.println("message received from client: " + message.getMessageUser());
                        darabol(message.getMessageUser());

                        if (!messages.isEmpty()) {
                            for (Message m : messages) {

                                System.out.println("message sent to client: " + m.getMessageUser());
                                System.out.println("message size: " + getObjectSize(m) + " bytes");
                                outS.writeObject(m);
                                outS.flush();
                            }
                            for (int i = 0; i < messages.size(); i++) {
                                messages.remove(i);
                            }
                        }


                        Thread.sleep(1000);
                        Write_lastCurrentDatabase();
                    } catch (InterruptedException e3) {
                        e3.printStackTrace();
                        return;
                    }
                }


            } catch (IOException e) {

                System.out.println("Disconnected from client");
                System.out.println(e.getMessage());
                System.out.println(e.getStackTrace());
            }

    }

    private void Create_socket_communication() throws IOException {
        int portNumber = 1234; // replace with your port number

        while (true) {
            handleClient(portNumber);
        }

    }

    public void addAnswerToClient(Message mgs) {
        messages.add(mgs);
    }

    String reformatParserInput(String fullInput) {
        fullInput = fullInput.trim();
//                    remove extra spaces
        fullInput = fullInput.replaceAll("\\s+", " ");
//                    replace tabs with spaces
        fullInput = fullInput.replaceAll("\\t", " ");
//                    if ( has no space  after it, add one
        fullInput = fullInput.replaceAll("([^\\s])\\(", "$1 (");
//                     if ( has no space before it, add one
        fullInput = fullInput.replaceAll("\\)([^\\s])", ") $1");
//                    if ) has no space after it, add one
        fullInput = fullInput.replaceAll("\\)\\(", ") (");
//                    if ) has no space before it, add one
        fullInput = fullInput.replaceAll("\\)\\(", ") (");
        return fullInput;
    }
    public void darabol(String input) {


        StringBuilder command = new StringBuilder();
//        newline character to space
        input = reformatParserInput(input);

        String[] words = input.split(" ");


        ArrayList<String> commandList = new ArrayList<>();
        String fullInput = "";
        for (int i = 0; i < words.length; i++) {
            if (elvSzavak.contains(words[i].toUpperCase())) {
                if (i == 0) {
                    fullInput = words[i] + " ";
                } else {
                    fullInput = fullInput.trim();
                    System.out.println("|=> parsed command: " + fullInput + "|");
                    Parser parser = new Parser(fullInput, this);
//                    if (error.length() > 0) {
//                        answer = "ERROR: " + error;
//                        error = "";
//                        return;
//                    } else {
//                        answer = "ok";
//                    }S
                    Message message = parser.getAnswer();

                    this.addAnswerToClient(message);

                    fullInput = words[i] + " ";
                }
            } else {
                fullInput += words[i] + " ";
            }
        }
        fullInput = fullInput.trim();
        System.out.println("|=> parsed command: " + fullInput + "| ");
//        new Parser(fullInput, this);
//        if (error.length() > 0) {
//            answer = "ERROR: " + error;
//            error = "";
//        } else {
//            answer = "ok";
//        }
        Parser parser = new Parser(fullInput, this);
        Message message = parser.getAnswer();

        this.addAnswerToClient(message);

    }

    public void setError(String error) {
        this.error = error;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    public static void main(String[] args) throws IOException {
        Host host = new Host();
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
