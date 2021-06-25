package net.mikc.derbyplus;

import org.aesh.readline.Readline;
import org.aesh.readline.tty.terminal.TerminalConnection;

import java.io.IOException;

public class Main {

    private static final DerbyReadline derbyReadline = new DerbyReadline();
    private static DatabaseConnector connector;
    private static IConsoleLogger console;

    public static void main(String[] args) throws IOException {
        TerminalConnection connection = new TerminalConnection();
        console = new ConsoleLogger(connection);
        connector = new DatabaseConnector(console);
        read(connection, derbyReadline.get() , "[derbyplus]$ ");
        connection.openBlocking();
    }

    private static void read(TerminalConnection connection, Readline readline, String prompt) {
        readline.readline(connection, prompt, input -> {
            if(input != null) {
                derbyReadline.append(input);
                if (input.equals("exit")) {
                    connector.exit();
                    connection.close();
                } else if (input.startsWith("get schema")) {
                    connector.getSchema();
                    read(connection, readline, prompt);
                } else if (input.startsWith("show tables")) {
                    connector.showTables();
                    read(connection, readline, prompt);
                } else if (input.startsWith("connect")) {
                    String []connectArgs = input.split("\\s+");
                    if(connectArgs.length>1) {
                        String jdbcUrl = connectArgs[1];
                        connector.connect(jdbcUrl);
                    }

                    read(connection, readline, prompt);
                } else {
//                    connection.write("=====> " + input + "\n");
                    connector.sqlCommand(input);
                    read(connection, readline, prompt);
                }
            }
        });
    }
}
