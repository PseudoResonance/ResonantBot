package com.github.pseudoresonance.resonantbot.log;

import java.io.File;

import com.github.pseudoresonance.resonantbot.ResonantBot;

public class FileAppender<E> extends ch.qos.logback.core.FileAppender<E> {

    /**
     * The name of the active log file.
     */
    protected String fileName = null;

    /**
     * The <b>File</b> property takes a string value which should be the name of
     * the file to append to.
     */
    public void setFile(String file) {
        if (file == null) {
            fileName = file;
            super.fileName = file;
        } else {
        	File f = new File(ResonantBot.getBot().getDirectory(), file);
            // Trim spaces from both ends. The users probably does not want
            // trailing spaces in file names.
            fileName = f.getAbsolutePath();
            super.fileName = f.getAbsolutePath();
        }
    }

    /**
     * Returns the value of the <b>File</b> property.
     * 
     * <p>
     * This method may be overridden by derived classes.
     * 
     */
    public String getFile() {
        return fileName;
    }
}