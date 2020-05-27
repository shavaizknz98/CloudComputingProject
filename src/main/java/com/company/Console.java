
package com.company;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Console extends WindowAdapter implements WindowListener, Runnable {
    private JFrame frame;

    private JTextPane textArea;

    private Thread stdOutReader;

    private Thread stdErrReader;

    private boolean stopThreads;

    private final PipedInputStream stdOutPin = new PipedInputStream();

    private final PipedInputStream stdErrPin = new PipedInputStream();

    //Used to print error messages in red
    private StyledDocument doc;
    private Style style;

    /** Initializes a new console */
    public Console() {

        // The area to which the output will be send to
        textArea = new JTextPane();
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        doc = (StyledDocument) textArea.getDocument();
        style = doc.addStyle("ConsoleStyle", null);
        StyleConstants.setFontFamily(style, "MonoSpaced");
        StyleConstants.setFontSize(style, 12);

        // Main frame to which the text area will be added to, along with scroll bars
        frame = new JFrame("Console");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = new Dimension(screenSize.width / 3, screenSize.height / 4);
        int x = frameSize.width / 20;
        int y = frameSize.height / 20;
        frame.setBounds(x, y, frameSize.width, frameSize.height);

        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setVisible(true);

        frame.addWindowListener(this);

        try {
            PipedOutputStream stdOutPos = new PipedOutputStream(this.stdOutPin);
            System.setOut(new PrintStream(stdOutPos, true));
        } catch (IOException io) {
            textArea.setText("Couldn't redirect STDOUT to this console\n" + io.getMessage());
        } catch (SecurityException se) {
            textArea.setText("Couldn't redirect STDOUT to this console\n" + se.getMessage());
        }

        try {
            PipedOutputStream stdErrPos = new PipedOutputStream(this.stdErrPin);
            System.setErr(new PrintStream(stdErrPos, true));
        } catch (IOException io) {
            textArea.setText("Couldn't redirect STDERR to this console\n" + io.getMessage());
        } catch (SecurityException se) {
            textArea.setText("Couldn't redirect STDERR to this console\n" + se.getMessage());
        }

        stopThreads = false; // Will be set to true at closing time. This will stop the threads

        // Starting two threads to read the PipedInputStreams
        stdOutReader = new Thread(this);
        stdOutReader.setDaemon(true);
        stdOutReader.start();

        stdErrReader = new Thread(this);
        stdErrReader.setDaemon(true);
        stdErrReader.start();
    }

    /**
     * Closes the window and stops the "stdOutReader" threads
     *
     * @param evt WindowEvent
     */
    public synchronized void windowClosed(WindowEvent evt) {

        // Notify the threads that they must stop
        stopThreads = true;
        this.notifyAll();

        try {
            stdOutReader.join(1000);
            stdOutPin.close();
        } catch (Exception e) {
        }
        try {
            stdErrReader.join(1000);
            stdErrPin.close();
        } catch (Exception e) {
        }
    }

    /** Close de window */
    public synchronized void windowClosing(WindowEvent evt) {
        frame.setVisible(false);
        frame.dispose();
    }

    /** The real work... */
    public synchronized void run() {
        try {
            while (Thread.currentThread() == stdOutReader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (stdOutPin.available() != 0) {
                    String input = this.readLine(stdOutPin);
                    StyleConstants.setForeground(style, Color.black);
                    doc.insertString(doc.getLength(), input, style);
                    // Make sure the last line is always visible
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                }
                if (stopThreads) {
                    return;
                }
            }

            while (Thread.currentThread() == stdErrReader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (stdErrPin.available() != 0) {
                    String input = this.readLine(stdErrPin);
                    StyleConstants.setForeground(style, Color.red);
                    doc.insertString(doc.getLength(), input, style);
                    // Make sure the last line is always visible
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                }
                if (stopThreads) {
                    return;
                }
            }
        } catch (Exception e) {
            textArea.setText("\nConsole reports an Internal error.");
            textArea.setText("The error is: " + e);
        }
    }

    private synchronized String readLine(PipedInputStream in) throws IOException {
        String input = "";
        do {
            int available = in.available();
            if (available == 0) {
                break;
            }
            byte b[] = new byte[available];
            in.read(b);
            input += new String(b, 0, b.length);
        } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !stopThreads);
        return input;
    }

    public static void main(String[] args) {
        new Console();
        System.out.println("Example message");
        System.err.println("Example error message");
    }
}
