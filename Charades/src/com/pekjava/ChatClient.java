package com.pekjava;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class ChatClient {

    static class ChatAccess extends Observable {
        private Socket socket;
        private OutputStream output;
        private String name;
        private String key = "######";
        private boolean active = false;

        public boolean isActive() {
            return active;
        }

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        public void initSocket(String server, int port, ChatFrame chatFrame) throws IOException {

            socket = new Socket(server, port);
            output = socket.getOutputStream();

            Thread receivingThread = new Thread(() -> {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = input.readLine()) != null) {
                        if (line.contains("Welcome")) {
                            name = line.split(" ")[1];
                        }
                        if (line.toLowerCase().endsWith(key.toLowerCase()) && line.contains("<") && line.split("> ")[1].equalsIgnoreCase(key)) {
                            send("#correct#-" + line.split(">")[0].replace("<", ""));
                        } else if (line.contains("#correct#-")) {
                            chatFrame.getDrawArea().clear();
                        } else if (line.contains("#")) {
                            systemCodes(chatFrame, line);
                        } else {
                            notifyObservers(line);
                        }
                    }
                } catch (IOException ex) {
                    notifyObservers(ex);
                }
            });
            receivingThread.start();
        }

        private void systemCodes(ChatFrame chatFrame, String line) {

            if (line.contains("#tura#-")) {

                if (line.contains("#tura#-" + name)) {
                    active = true;
                    chatFrame.getUserInputText().setVisible(false);
                } else {
                    active = false;
                    chatFrame.getUserInputText().setVisible(true);
                }

            } else if (line.contains("#key#-")) {

                key = line.split("#key#-")[1];
                chatFrame.getTextArea().append("Your turn. Key is : " + key);

            } else if (line.contains("#draw#")) {

                String[] x = line.split("#draw#")[1].split(",");
                chatFrame.getDrawArea().getG2().drawLine(Integer.valueOf(x[0]), Integer.valueOf(x[1]), Integer.valueOf(x[2]), Integer.valueOf(x[3]));
                chatFrame.getDrawArea().repaint();

            } else if (line.contains("#color#-")) {

                if (line.contains("blue")) {
                    chatFrame.getDrawArea().blue();
                } else if (line.contains("red")) {
                    chatFrame.getDrawArea().red();
                } else if (line.contains("green")) {
                    chatFrame.getDrawArea().green();
                } else if (line.contains("clear")) {
                    chatFrame.getDrawArea().clear();
                }
            }
        }

        private static final String CRLF = "\r\n";

        public void send(String text) {
            try {
                output.write((text + CRLF).getBytes());
                output.flush();
            } catch (IOException e) {
                notifyObservers(e);
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                notifyObservers(e);
            }
        }
    }

    static class ChatFrame extends JFrame implements Observer {

        private JTextArea textArea;
        private JTextField userInputText;
        private DrawArea drawArea;
        private Toolbar toolbar;
        private ChatAccess chatAccess;

        public ChatFrame(ChatAccess chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            buildGui();
        }

        public DrawArea getDrawArea() {
            return drawArea;
        }

        public JTextArea getTextArea() {
            return textArea;
        }

        public JTextField getUserInputText() {
            return userInputText;
        }

        private void buildGui() {

            Box box = Box.createVerticalBox();
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            userInputText = new JTextField();
            userInputText.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String msg = userInputText.getText();

                    if (msg != null && msg.trim().length() > 0) {
                        chatAccess.send(msg);
                    }

                    userInputText.selectAll();
                    userInputText.requestFocus();
                    userInputText.setText("");
                }
            });

            box.add(new JScrollPane(textArea), BorderLayout.CENTER);
            box.add(userInputText, BorderLayout.SOUTH);

            Box box2 = Box.createVerticalBox();
            drawArea = new DrawArea(chatAccess);
            Dimension dim = drawArea.getPreferredSize();
            dim.width = 480;
            drawArea.setPreferredSize(dim);

            toolbar = new Toolbar();

            toolbar.setColorListener(new ColorListener() {

                public void colorChosen(String color) {

                    if (color == "red" && chatAccess.isActive()) {
                        drawArea.red();
                        chatAccess.send("#color#-red");
                    } else if (color == "green" && chatAccess.isActive()) {
                        drawArea.green();
                        chatAccess.send("#color#-green");
                    } else if (color == "blue" && chatAccess.isActive()) {
                        drawArea.blue();
                        chatAccess.send("#color#-blue");
                    } else if (color == "clear" && chatAccess.isActive()) {
                        drawArea.clear();
                        chatAccess.send("#color#-clear");
                    }
                }
            });
            box2.add(drawArea, BorderLayout.CENTER);
            box2.add(toolbar, BorderLayout.NORTH);

            add(box, BorderLayout.EAST);
            add(box2, BorderLayout.WEST);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.send("/quit");
                    chatAccess.close();
                }
            });
        }

        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(() -> {
                textArea.append("\n");
                textArea.append(finalArg.toString());
                textArea.append("\n");
            });
        }
    }

    public static void main(String[] args) {
        String server = "localhost";
        int port = 2222;

        ChatAccess chatAccess = new ChatAccess();
        ChatFrame chatFrame = new ChatFrame(chatAccess);
        chatFrame.setTitle("My App - connected to " + server + ": " + port);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.pack();
        chatFrame.setLocationRelativeTo(null);
        chatFrame.setResizable(false);
        chatFrame.setSize(1050, 400);
        chatFrame.setVisible(true);

        try {
            chatAccess.initSocket(server, port, chatFrame);
        } catch (IOException e) {
            System.out.println("Cannot connect to " + server + ": " + port);
            e.printStackTrace();
            System.exit(0);
        }
    }
}
