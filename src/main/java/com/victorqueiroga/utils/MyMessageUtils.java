package com.victorqueiroga.utils;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author victorqueiroga
 */
public class MyMessageUtils {
    public static void showError(String message, Component parent) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    public static void showInformation(String message, Component parent) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
