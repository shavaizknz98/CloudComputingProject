package com.company;

import java.io.File;

import static com.company.Download.*;

public class Delete {
    public Delete() {
    }

    public void deleteFiles() {
        System.out.println("Deleting...");
        File f1 = new File(fstream1.getAbsolutePath());
        File f2 = new File(fstream.getAbsolutePath());
        // Get all files in directory

        if (!f1.delete()) {
            // Failed to delete file
            System.out.println("Failed to delete " + f1.getAbsolutePath());
        }
        if (!f2.delete()) {
            // Failed to delete file
            System.out.println("Failed to delete " + f2.getAbsolutePath());
        }
    }
}
