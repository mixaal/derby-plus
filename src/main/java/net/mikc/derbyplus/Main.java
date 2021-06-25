package net.mikc.derbyplus;

import net.mikc.derbyplus.commands.DatabaseCommand;
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
                    connector.execute(DatabaseCommand.EXIT);
                    connection.close();
                } else if (input.startsWith("get schema")) {
                    connector.execute(DatabaseCommand.GET_SCHEMA);
                    read(connection, readline, prompt);
                } else if (input.startsWith("show tables")) {
                    connector.execute(DatabaseCommand.SHOW_TABLES);
                    read(connection, readline, prompt);
                } else if (input.startsWith("connect")) {
                    String jdbcUrl = fetchArgument(input);
                    if(jdbcUrl!=null) {
                        connector.execute(DatabaseCommand.CONNECT, jdbcUrl);
                    }
                    read(connection, readline, prompt);
                } else if (input.startsWith("select")) {
                    connector.execute(DatabaseCommand.SELECT, input);
                    read(connection, readline, prompt);
                } else if (input.startsWith("create")) {
                    connector.execute(DatabaseCommand.CREATE_TABLE, input);
                    read(connection, readline, prompt);
                } else if (input.startsWith("desc")) {
                    String tableName = fetchArgument(input);
                    if(tableName!=null) {
                        connector.execute(DatabaseCommand.DESC_TABLE, tableName);
                    }
                    read(connection, readline, prompt);
                } else {
                    connector.execute(DatabaseCommand.UPDATE_INSERT, input);
                    read(connection, readline, prompt);
                }
            }
        });
    }

    private static String fetchArgument(String input) {
        String []connectArgs = input.split("\\s+");
        if(connectArgs.length>1) {
            String jdbcUrl = connectArgs[1];
            return jdbcUrl;
        }
        console.log("Need argument");
        return null;
    }
}
