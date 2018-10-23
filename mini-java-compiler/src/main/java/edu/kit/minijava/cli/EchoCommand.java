package edu.kit.minijava.cli;

import java.io.*;

class EchoCommand {
    int execute(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            InputStreamReader reader = new InputStreamReader(stream, "US-ASCII");

            StringBuilder builder = new StringBuilder();
            int symbol = reader.read();

            while (symbol != -1) {
                builder.append((char) symbol);
                symbol = reader.read();
            }

            System.out.print(builder.toString());

            return 0;
        }
        catch (FileNotFoundException exception) {
            System.err.println("error: File '" + path + "' was not found!");

            return 1;
        }
        catch (IOException exception) {
            System.err.println("error: File '" + path + "' could not be read!");

            return 1;
        }
    }
}
