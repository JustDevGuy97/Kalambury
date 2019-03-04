package com.pekjava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JPanel implements ActionListener {

    private JButton clearBtn, redBtn, blueBtn, greenBtn;
    private ColorListener listener;

    public Toolbar() {
        setLayout(new FlowLayout());

        clearBtn = new JButton("Clear");
        redBtn = new JButton("Red");
        blueBtn = new JButton("Blue");
        greenBtn = new JButton("Green");


        add(clearBtn);
        clearBtn.addActionListener(this);
        add(redBtn);
        redBtn.addActionListener(this);
        add(blueBtn);
        blueBtn.addActionListener(this);
        add(greenBtn);
        greenBtn.addActionListener(this);
    }

    public void setColorListener(ColorListener listener) {
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearBtn) {
            listener.colorChosen("clear");
        } else if (e.getSource() == redBtn) {
            listener.colorChosen("red");
        } else if (e.getSource() == blueBtn) {
            listener.colorChosen("blue");
        } else if (e.getSource() == greenBtn) {
            listener.colorChosen("green");
        }
    }
}
