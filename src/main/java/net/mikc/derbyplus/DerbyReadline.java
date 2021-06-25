package net.mikc.derbyplus;

import org.aesh.readline.Readline;
import org.aesh.readline.ReadlineBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DerbyReadline extends Readline {
    private static final String HOME = System.getenv("HOME");
    private File historyFile;
    private Readline readline;

    public DerbyReadline() {
        this.historyFile = HOME == null ? new File(".derbyplus_history") : new File(HOME+"/.derbyplus_history");
        try {
            if (!historyFile.exists()) {
                historyFile.createNewFile();
            }
        }
        catch (IOException e) {
        }
        this.readline = ReadlineBuilder.builder()
                .historyFile(historyFile.getAbsolutePath())
                .enableHistory(true)
                .build();
    }

    public void append(String input) {
        try {
            FileWriter writer = new FileWriter(historyFile.getAbsolutePath(), true);
            writer.write(input+"\n");
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Readline get() {
        return readline;
    }
}
