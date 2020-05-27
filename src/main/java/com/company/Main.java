package com.company;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Main {
    public static Connection conn;
    public static String vidname = "";
    public static String vidpath = "";
    public static int vidid = -1;
    public static ArrayList<String> vidnames = new ArrayList<>();
    public static ArrayList<String> vidpaths = new ArrayList<>();
    public static ArrayList<Integer> vidids = new ArrayList<>();
    public static ArrayList<Integer> analys = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            conn = SQLConnector.connect();

            try {
                Statement stmtObj = conn.createStatement();
                ResultSet resObj = stmtObj.executeQuery("SELECT * FROM VIDEOINFO where analyzed = 0");
                while (resObj.next()) {
                    vidnames.add(resObj.getString("VIDNAME"));
                    vidids.add(resObj.getInt("VIDID"));
                    vidpaths.add(resObj.getString("VIDPATH"));
                    analys.add(resObj.getInt("analyzed"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            try {
                Statement stmtObj = conn.createStatement();
                ResultSet resObj = stmtObj.executeQuery("SELECT TOP 1 * FROM VIDEOINFO WHERE analyzed = 0 ORDER BY VIDID ASC");
                while (resObj.next()) {
                    vidname = resObj.getString("VIDNAME");
                    vidid = resObj.getInt("VIDID");
                    vidpath = resObj.getString("VIDPATH");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }



            if(!vidname.equals("") && !vidpath.equals("")  && vidid != -1){
                System.out.println("videos in the que:");
                for (int i = 0; i < vidids.size(); i++) {
                    System.out.println(vidids.get(i) + "  " + vidnames.get(i) + "  " + analys.get(i) + "  " + vidpaths.get(i) + "\n");
                }
                System.out.println("video next for analysis:" + vidid + "  " + vidname + "  "+ vidpath+ "\n");

                Upload upl = new Upload();
                upl.update();

                Download dec = new Download();
                dec.downloadfile();
                dec.decompress();

                Execute exec = new Execute();
                exec.executescript();

                upl.uploadlog();

                try {
                    System.out.println("Emptying the logs...");
                    PrintWriter writer = null;
                    writer = new PrintWriter("log");
                    writer.print("");
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Delete del = new Delete();
                del.deleteFiles();
            }
            else{
                System.out.println("No videos in the queue!\n");
            }

            vidname = "";
            vidpath = "";
            vidid = -1;
            vidnames = new ArrayList<>();
            vidpaths = new ArrayList<>();
            vidids = new ArrayList<>();
            analys = new ArrayList<>();

            try {
                System.out.println("Sleeping for 5 seconds...");
                System.out.println("You can now hit ctrl+c to stop the execution");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
