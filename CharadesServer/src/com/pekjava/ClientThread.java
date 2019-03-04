package com.pekjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ClientThread extends Thread {
    private static boolean activityInitial = false;
    private String clientName;
    private BufferedReader input;
    private PrintStream output;
    private Socket clientSocket;
    private final ClientThread[] threads;
    private int maxClientsCount;
    private boolean active;

    private String name;
    private List<String> key = Arrays.asList("Bike", "House", "Tree");


    public ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;

        if (!activityInitial) {
            this.active = true;
            activityInitial = true;
        } else {
            this.active = false;
        }

        maxClientsCount = threads.length;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientThread[] threads = this.threads;

        try {

            // Creating input and output for this client

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintStream(clientSocket.getOutputStream());


            while (true) {
                output.println("Enter your name.");
                name = input.readLine().trim();

                if (name.indexOf('@') == -1) {
                    break;
                } else {
                    output.println("The name should not contain '@' character.");
                }
            }

            // New client joins the room

            output.println("Welcome " + name + " to our game room.\nTo leave press the \"X\" button in the upper right corner of the game window.");
            sendToActive();

            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = "@" + name;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].output.println("*** A new user " + name + " entered the game room !!! ***");
                    }
                }
            }

            // Starting conversation

            while (true) {

                String line = input.readLine();

                synchronized (this) {
                    if (line.startsWith("/quit")) {
                        if (active && currentThreadsCount() > 1) {

                            for (ClientThread thread : threads) {
                                if (thread != null && !thread.isActive()) {
                                    thread.setActive();
                                    thread.sendToActive();
                                    break;
                                }
                            }

                        }
                        active = false;
                        if (currentThreadsCount() == 1) {
                            setActivityInitial(false);
                        }
                        break;
                    }
                }

                synchronized (this) {
                    if (line.startsWith("#correct#-")) {
                        active = false;

                        for (ClientThread thread : threads) {
                            if (thread != null && thread.getClientName().equalsIgnoreCase(line.split("-")[1])) {
                                thread.setActive();
                                thread.sendToActive();
                            }
                        }
                        sendNotActive(line.split("-")[1]);
                    }
                }

                // Broadcasting message to all other clients

                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i].clientName != null) {
                            threads[i].output.println("<" + name + "> " + line);
                        }
                    }
                }
            }

            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this && threads[i].clientName != null) {
                        threads[i].output.println("*** The user " + name + " is leaving the game room !!! ***");
                    }
                }
            }
            output.println("*** Bye " + name + " ***");

            // Cleaning up. Setting current thread variable to null, so that a new client could be accepted by the server
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }

            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public boolean isActive() {
        return active;
    }

    public String getClientName() {
        return name;
    }

    private void setActive() {
        active = true;
        output.println("#tura#-" + name);
    }

    private static void setActivityInitial(boolean activityInitial) {
        ClientThread.activityInitial = activityInitial;
    }

    private void sendNotActive(String name) {
        output.println("#tura#-" + name);
    }

    private void sendToActive() {
        if (active) {
            Random generator = new Random();
            output.println("#tura#-" + name);
            output.println("#key#-" + key.get(generator.nextInt(3)));
        }
    }

    private int currentThreadsCount() {
        int count = 0;
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null) {
                count++;
            }
        }
        return count;
    }
}
