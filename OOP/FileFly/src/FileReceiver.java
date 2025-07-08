/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.*;
import java.net.*;
import javax.swing.*;

public class FileReceiver {
    public static void main(String[] args) {
        try {
            // Change this IP to match sender's IP from QR or console
            String senderIP = "127.0.0.1"; // e.g. 192.168.0.105
            int senderPort = 9090;

            Socket socket = new Socket(senderIP, senderPort);
            InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            FileOutputStream fos = new FileOutputStream("received_" + fileName);

            byte[] buffer = new byte[4096];
            int read;
            long totalRead = 0;
            while ((read = dis.read(buffer)) > 0) {
                totalRead += read;
                fos.write(buffer, 0, read);
                if (totalRead >= fileSize) break;
            }

            fos.close();
            dis.close();
            socket.close();

            JOptionPane.showMessageDialog(null, "File received: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
