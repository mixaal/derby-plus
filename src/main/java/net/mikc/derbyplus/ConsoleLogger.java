package net.mikc.derbyplus;

import org.aesh.readline.tty.terminal.TerminalConnection;

public class ConsoleLogger implements IConsoleLogger {
    private final TerminalConnection connection;
    public ConsoleLogger(TerminalConnection connection) {
        this.connection = connection;
    }

    public void log(String message) {
        connection.write(message+"\n");
    }
}
