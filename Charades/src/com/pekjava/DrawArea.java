package com.pekjava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

@SuppressWarnings("serial")
public class DrawArea extends JComponent {

    private ChatClient.ChatAccess playerChat;
    private Image image;

    private Graphics2D g2;
    private int oldX, oldY, currentX, currentY;


    public Graphics2D getG2() {
        return g2;
    }

    public DrawArea(ChatClient.ChatAccess playerChat) {
        this.playerChat = playerChat;

        setDoubleBuffered(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (playerChat.isActive()) {

                    currentX = e.getX();
                    currentY = e.getY();

                    if (g2 != null) {
                        g2.drawLine(oldX, oldY, currentX, currentY);
                        repaint();

                        playerChat.send("#draw#" + String.valueOf(oldX) + "," + String.valueOf(oldY) + "," + String.valueOf(currentX) + "," + String.valueOf(currentY) + ",");

                        oldX = currentX;
                        oldY = currentY;
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) {
            image = createImage(getSize().width, getSize().height);
            g2 = (Graphics2D) image.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            clear();
        }
        g.drawImage(image, 0, 0, null);
    }

    public void clear() {
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, getSize().width, getSize().height);
        g2.setPaint(Color.black);
        repaint();
    }

    public void red() {
        g2.setPaint(Color.red);
    }

    public void green() {
        g2.setPaint(Color.green);
    }

    public void blue() {
        g2.setPaint(Color.blue);
    }
}
