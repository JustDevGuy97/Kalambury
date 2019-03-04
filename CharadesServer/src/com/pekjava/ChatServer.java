package com.pekjava;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static ServerSocket server;
    private static Socket clientSocket;

    private static final int maxClientsCount = 5;
    private static final ClientThread[] threads = new ClientThread[maxClientsCount];

    public static void main(String[] args) {
        int portNumber = 2222;
        System.out.println("Usage: java Server\n" + "Now using portNumber: " + portNumber);
        try {
            server = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                clientSocket = server.accept();
                int i;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintWriter output = new PrintWriter(clientSocket.getOutputStream());
                    output.println("Server too busy. Try later.");
                    output.flush();
                    output.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
