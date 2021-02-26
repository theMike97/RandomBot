package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorLogger {

    File log;

    public ErrorLogger(File log) throws IOException {
        this.log = log;
    }

    public void log(String message) throws IOException {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(message + "\n");
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
