package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import static com.company.Main.conn;
import static com.company.Main.vidid;

public class Upload {

    Double time;
    Double fps;
    int totx;
    int toty;

    public Upload(){

    }

    public void uploadlog(){
        System.out.println("Uploading analysis data...");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("log"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                if(line.startsWith("elapsed time")){
                    time = Double.parseDouble(line.split(": ", 2)[1]);
                }
                else if(line.startsWith("approximate FPS")){
                    fps = Double.parseDouble(line.split(": ", 2)[1]);
                }
                else if(line.startsWith("total x")){
                    totx = (int) Double.parseDouble(line.split(": ", 2)[1]);
                }
                else if(line.startsWith("total y")){
                    toty = (int) Double.parseDouble(line.split(": ", 2)[1]);
                }
                line = reader.readLine();
            }

            Statement stmtObj = conn.createStatement();
            stmtObj.execute("INSERT INTO VIDEOANALYSIS (VIDID, NUMOFPEOPLEX, NUMOFPEOPLEY, ANALYSISTIME, FPS) " +
                    "VALUES ("+vidid+", "+totx+", "+toty+", "+time+", "+fps+")");

            time = -1.0;
            fps = -1.0;
            totx = -1;
            toty = -1;

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void update(){
        System.out.println("Updating the queue...");
        try {
            Statement stmtObj = null;
            stmtObj = conn.createStatement();
            stmtObj.execute("UPDATE VIDEOINFO SET analyzed = 1 WHERE VIDID = " + vidid);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
