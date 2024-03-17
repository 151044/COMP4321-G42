package hk.ust.comp4321.db.visual;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;

public class Visualizer {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarculaLaf());

    }
}
