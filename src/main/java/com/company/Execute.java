package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.company.Download.fstream1;


public class Execute {
    Process p = null;
    boolean pyRunning = false;

    public Execute() {

    }

    public void executescript() {
        System.out.println("Analysing...");
        try {
            ProcessBuilder pb =
                    new ProcessBuilder("python", "people_counter.py", "--prototxt",
                            "mobilenet_ssd/MobileNetSSD_deploy.prototxt",
                            "--model",
                            "mobilenet_ssd/MobileNetSSD_deploy.caffemodel",
                            "--input",
                            fstream1.getAbsolutePath(),
                            //"videos/" + vidname.substring(0, vidname.length() -3),
                            "--skip-frames",
                            "60");
            pb.directory(new File("./people-counting-opencv"));
            File log = new File("log");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            p = pb.start();
            assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
            assert p.getInputStream().read() == -1;

            pyRunning = true;
            System.out.println("Thread Running");
            try {
                p.waitFor();

                pyRunning = false;

                StringBuilder sb = new StringBuilder();

                try (BufferedReader br = Files.newBufferedReader(Paths.get("log"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }

                } catch (IOException e) {
                    System.err.format("IOException: %s%n", e);
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

}
