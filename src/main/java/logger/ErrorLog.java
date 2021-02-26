package logger;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ErrorLog extends File {

    public ErrorLog(@NotNull String pathname) {
        super(pathname);
        this.setReadOnly();
    }

}
