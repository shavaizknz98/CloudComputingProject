package com.company;

import sun.security.action.GetPropertyAction;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import static com.company.Main.vidname;
import static com.company.Main.vidpath;

public class Download {

    String path = System.getProperty("java.io.tmpdir");
    public static File fstream;
    public static File fstream1;
    public static String tempPath;

    public Download(){

    }

    public void downloadfile(){
        System.out.println("Downloading...");
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            fstream = File.createTempFile(vidname.substring(0, vidname.length() -4), ".mp4.gz");
            try (BufferedInputStream inputStream =
                         new BufferedInputStream(new URL(vidpath).openStream());
                 FileOutputStream fileOS =
                         new FileOutputStream(fstream)) {
                byte data[] = new byte[4096];
                int byteContent;
                while ((byteContent = inputStream.read(data, 0, 4096)) != -1) {
                    fileOS.write(data, 0, byteContent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decompress(){
        System.out.println("Decompressing...");
        String gzip_filepath = "./people-counting-opencv/videos/" + vidname;
        String decopressed_filepath = "./people-counting-opencv/videos/" + vidname.substring(0, vidname.length() -3);

        byte[] buffer = new byte[4096];

        try {
            Path outerpath = Paths.get(fstream.getPath());
            tempPath = outerpath.getParent().toString();
            System.out.println(tempPath);
            System.out.println(fstream.getAbsolutePath());
            //FileInputStream fileIn = new FileInputStream(gzip_filepath);
            FileInputStream fileIn = new FileInputStream(fstream.getAbsolutePath());

            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);

            fstream1 = File.createTempFile(vidname.substring(0, vidname.length() -7), ".mp4");
            //FileOutputStream fileOutputStream = new FileOutputStream(decopressed_filepath);
            System.out.println(fstream1.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(fstream1.getAbsolutePath());

            int bytes_read;

            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }

            gZIPInputStream.close();
            fileOutputStream.close();

            System.out.println("The file was decompressed successfully!");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
