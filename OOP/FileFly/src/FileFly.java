import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class FileFly extends JFrame {

    private JTextArea logArea;
    private JButton btnSelectFile, btnSendFile, btnReceiveFile, btnGenerateQR, btnReceiveQR;
    private JLabel lblQRCode;
    private JTextField txtIP, txtPort;
    private File selectedFile;

    private final int PORT = 9090;

    public FileFly() {
        setTitle("File Fly - Share files with QR code");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(230, 240, 250));

        // Top panel with buttons
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBackground(new Color(200, 220, 240));

        // First row - File operations
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filePanel.setBackground(new Color(200, 220, 240));

        // Second row - Network operations
        JPanel networkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        networkPanel.setBackground(new Color(200, 220, 240));

        Font customFont = new Font("Comic Sans MS", Font.PLAIN, 14);
        Color darkBlue = new Color(0, 0, 128);
        Font buttonFont = new Font("Comic Sans MS", Font.BOLD, 12);
        Color buttonBgColor = new Color(70, 130, 180);
        Color buttonFgColor = darkBlue;

        btnSelectFile = new JButton("Select File to Send");
        btnSendFile = new JButton("Send File");
        btnSendFile.setEnabled(false);
        btnReceiveFile = new JButton("Manual Receive");
        btnGenerateQR = new JButton("Generate QR Code");
        btnReceiveQR = new JButton("Receive via QR Code");

        JButton[] buttons = {btnSelectFile, btnSendFile, btnReceiveFile, btnGenerateQR, btnReceiveQR};
        for (JButton btn : buttons) {
            btn.setFont(buttonFont);
            btn.setBackground(buttonBgColor);
            btn.setForeground(buttonFgColor);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 90, 120), 1),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
        }

        txtIP = new JTextField(15);
        txtPort = new JTextField(String.valueOf(PORT), 5);

        txtIP.setFont(customFont);
        txtIP.setBackground(Color.WHITE);
        txtIP.setForeground(darkBlue);
        txtIP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        txtPort.setFont(customFont);
        txtPort.setBackground(Color.WHITE);
        txtPort.setForeground(darkBlue);
        txtPort.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JLabel lblReceiverIP = new JLabel("Receiver IP:");
        lblReceiverIP.setFont(buttonFont);
        lblReceiverIP.setForeground(darkBlue);

        JLabel lblPort = new JLabel("Port:");
        lblPort.setFont(buttonFont);
        lblPort.setForeground(darkBlue);

        // Add components to panels
        filePanel.add(btnSelectFile);
        filePanel.add(btnSendFile);
        filePanel.add(btnGenerateQR);

        networkPanel.add(lblReceiverIP);
        networkPanel.add(txtIP);
        networkPanel.add(lblPort);
        networkPanel.add(txtPort);
        networkPanel.add(btnReceiveFile);
        networkPanel.add(btnReceiveQR);

        topPanel.add(filePanel);
        topPanel.add(networkPanel);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBackground(new Color(230, 240, 250));

        lblQRCode = new JLabel("QR Code will appear here", SwingConstants.CENTER);
        lblQRCode.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        lblQRCode.setFont(new Font("Comic Sans MS", Font.ITALIC, 12));
        lblQRCode.setForeground(darkBlue);
        lblQRCode.setBackground(Color.WHITE);
        lblQRCode.setOpaque(true);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        logArea.setForeground(darkBlue);
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));

        centerPanel.add(lblQRCode);
        centerPanel.add(scrollPane);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        add(panel);

        // Event listeners
        btnSelectFile.addActionListener(e -> selectFile());
        btnSendFile.addActionListener(e -> sendFile());
        btnReceiveFile.addActionListener(e -> receiveFile());
        btnGenerateQR.addActionListener(e -> generateQRCode());
        btnReceiveQR.addActionListener(e -> receiveViaQRCode());
        
        // Check for ZXing libraries
        try {
            Class.forName("com.google.zxing.MultiFormatWriter");
            log("ZXing core library found.");
            Class.forName("com.google.zxing.client.j2se.MatrixToImageWriter");
            log("ZXing javase library found.");
            Class.forName("com.google.zxing.MultiFormatReader");
            log("ZXing QR reader found.");
        } catch (ClassNotFoundException e) {
            log("ZXing library missing: " + e.getMessage());
            btnReceiveQR.setEnabled(false);
        }
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File to Share");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            log("Selected file: " + selectedFile.getAbsolutePath());
            btnSendFile.setEnabled(true);
        }
    }

    private void sendFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
            return;
        }

        // Check if port is available
        if (!isPortAvailable(PORT)) {
            log("Error: Port " + PORT + " is already in use");
            JOptionPane.showMessageDialog(this, "Port " + PORT + " is busy. Please try again later.");
            return;
        }

        // Automatically generate QR code when starting to send file
        generateQRCode(false); // Don't show dialog when auto-generating

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log("Waiting for receiver to connect on port " + PORT + "...");
                log("Server started. Waiting for connection...");
                
                try (Socket socket = serverSocket.accept();
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     FileInputStream fis = new FileInputStream(selectedFile)) {

                    log("Receiver connected: " + socket.getInetAddress());

                    dos.writeUTF(selectedFile.getName());
                    dos.writeLong(selectedFile.length());
                    log("Sending file: " + selectedFile.getName() + " (" + selectedFile.length() + " bytes)");

                    byte[] buffer = new byte[4096];
                    int read;
                    long totalSent = 0;
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                        totalSent += read;
                        // Optional: Show progress
                        if (totalSent % (1024 * 1024) == 0) { // Log every MB
                            log("Sent: " + (totalSent / (1024 * 1024)) + " MB");
                        }
                    }
                    dos.flush();
                    log("File sent successfully. Total bytes: " + totalSent);
                }
            } catch (IOException ex) {
                log("Error sending file: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Error sending file: " + ex.getMessage()));
            }
        }).start();
    }

    private void receiveFile() {
        String ip = txtIP.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number.");
            return;
        }

        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter sender IP address.");
            return;
        }

        connectAndReceiveFile(ip, port);
    }

    private void receiveViaQRCode() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select QR Code Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        chooser.setFileFilter(filter);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File qrImageFile = chooser.getSelectedFile();
            log("Selected QR code image: " + qrImageFile.getAbsolutePath());
            
            new Thread(() -> {
                try {
                    String qrData = decodeQRCode(qrImageFile);
                    if (qrData != null && !qrData.isEmpty()) {
                        log("QR Code decoded: " + qrData);
                        
                        // Parse IP and port from QR data
                        String[] parts = qrData.split(":");
                        if (parts.length == 2) {
                            String ip = parts[0];
                            int port = Integer.parseInt(parts[1]);
                            
                            SwingUtilities.invokeLater(() -> {
                                txtIP.setText(ip);
                                txtPort.setText(String.valueOf(port));
                                log("Auto-filled connection details from QR code");
                                log("Connecting to: " + ip + ":" + port);
                            });
                            
                            // Automatically connect after decoding
                            connectAndReceiveFile(ip, port);
                        } else {
                            log("Invalid QR code format. Expected format: IP:PORT");
                            SwingUtilities.invokeLater(() -> 
                                JOptionPane.showMessageDialog(this, 
                                    "Invalid QR code format.\nExpected format: IP:PORT"));
                        }
                    } else {
                        log("Failed to decode QR code");
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(this, 
                                "Failed to decode QR code.\nPlease ensure the image contains a valid QR code."));
                    }
                } catch (Exception e) {
                    log("Error processing QR code: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(this, 
                            "Error processing QR code: " + e.getMessage()));
                }
            }).start();
        }
    }

    private String decodeQRCode(File qrImageFile) throws Exception {
        BufferedImage image = ImageIO.read(qrImageFile);
        if (image == null) {
            throw new Exception("Could not read image file");
        }

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
            new BufferedImageLuminanceSource(image)));
        
        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(binaryBitmap);
        
        return result.getText();
    }

    private void connectAndReceiveFile(String ip, int port) {
        new Thread(() -> {
            try (Socket socket = new Socket()) {
                // Set timeout for connection
                socket.connect(new InetSocketAddress(ip, port), 10000); // 10 second timeout
                
                try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                    log("Connected to sender at " + ip + ":" + port);
                    String fileName = dis.readUTF();
                    long length = dis.readLong();
                    log("Receiving file: " + fileName + " (" + length + " bytes)");

                    // Show save dialog on EDT
                    final String finalFileName = fileName;
                    final long finalLength = length;
                    
                    SwingUtilities.invokeAndWait(() -> {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Save received file as");
                        chooser.setSelectedFile(new File(finalFileName));
                        int res = chooser.showSaveDialog(this);
                        if (res == JFileChooser.APPROVE_OPTION) {
                            File saveFile = chooser.getSelectedFile();
                            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                                byte[] buffer = new byte[4096];
                                int read;
                                long totalRead = 0;
                                while (totalRead < finalLength && (read = dis.read(buffer, 0, 
                                       (int) Math.min(buffer.length, finalLength - totalRead))) > 0) {
                                    fos.write(buffer, 0, read);
                                    totalRead += read;
                                    // Optional: Show progress
                                    if (totalRead % (1024 * 1024) == 0) { // Log every MB
                                        log("Received: " + (totalRead / (1024 * 1024)) + " MB");
                                    }
                                }
                                fos.flush();
                                log("File received and saved to: " + saveFile.getAbsolutePath());
                                SwingUtilities.invokeLater(() -> 
                                    JOptionPane.showMessageDialog(this, 
                                        "File received successfully!\nSaved to: " + saveFile.getAbsolutePath()));
                            } catch (IOException e) {
                                log("Error saving file: " + e.getMessage());
                            }
                        } else {
                            log("Save canceled by user.");
                        }
                    });
                }
            } catch (Exception ex) {
                log("Error receiving file: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Error connecting to sender: " + ex.getMessage()));
            }
        }).start();
    }

    private void generateQRCode() {
        generateQRCode(true); // Show dialog by default
    }
    
    private void generateQRCode(boolean showDialog) {
        try {
            // First, debug network info
            debugNetworkInfo();
            
            String ip = getLocalIPAddress();
            log("Detected Local IP Address: " + (ip != null ? ip : "null"));
            
            if (ip == null || ip.isEmpty() || ip.equals("127.0.0.1")) {
                if (showDialog) {
                    JOptionPane.showMessageDialog(this, 
                        "Unable to determine local IP address.\n" +
                        "Please ensure you are connected to a network.\n" +
                        "Using localhost for testing...");
                }
                ip = "127.0.0.1"; // Use localhost for testing
            }
            
            String data = ip + ":" + PORT;
            log("Generating QR code for: " + data);
            
            try {
                BufferedImage qrImage = createQRImage(data, 250, 250);
                if (qrImage == null) {
                    throw new Exception("QR code image is null");
                }
                
                // Scale the image to fit the label properly
                ImageIcon icon = new ImageIcon(qrImage);
                Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                lblQRCode.setIcon(new ImageIcon(scaledImage));
                lblQRCode.setText("");
                lblQRCode.revalidate();
                lblQRCode.repaint();
                
                log("QR code generated successfully for: " + data);
                
                // Only show dialog if explicitly requested (not when auto-generated from sendFile)
                if (showDialog) {
                    JOptionPane.showMessageDialog(this, 
                        "QR Code generated!\n\n" +
                        "Manual connection info:\n" +
                        "IP: " + ip + "\n" +
                        "Port: " + PORT + "\n\n" +
                        "Share this QR code with the receiving device.\n" +
                        "The receiver can scan this code to auto-connect.",
                        "QR Code Generated", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                    
            } catch (Exception e) {
                log("QR Generation Error: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            log("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            if (showDialog) {
                JOptionPane.showMessageDialog(this, 
                    "QR Code Generation Failed:\n" + e.getMessage() + 
                    "\nPlease check your ZXing libraries and network connection.");
            }
        }
    }

    private String getLocalIPAddress() {
        try {
            // Method 1: Try to connect to a remote address to determine local IP
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("8.8.8.8", 80), 3000);
                String ip = socket.getLocalAddress().getHostAddress();
                if (!ip.equals("127.0.0.1")) {
                    return ip;
                }
            } catch (Exception e) {
                log("Method 1 failed: " + e.getMessage());
            }
            
            // Method 2: Network interface enumeration
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        // Prefer private network addresses
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || 
                            (ip.startsWith("172.") && 
                             Integer.parseInt(ip.split("\\.")[1]) >= 16 && 
                             Integer.parseInt(ip.split("\\.")[1]) <= 31)) {
                            return ip;
                        }
                    }
                }
            }
            
            // Method 3: Try localhost
            return InetAddress.getLocalHost().getHostAddress();
            
        } catch (Exception e) {
            log("All IP detection methods failed: " + e.getMessage());
            return "127.0.0.1";
        }
    }

    private BufferedImage createQRImage(String data, int width, int height) throws WriterException {
        log("Creating QR image for data: " + data + " with size: " + width + "x" + height);
        
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            
            log("QR BitMatrix created successfully");
            
            // Try to use MatrixToImageWriter first
            try {
                BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
                log("MatrixToImageWriter succeeded");
                return image;
            } catch (Exception e) {
                log("MatrixToImageWriter failed, using manual method: " + e.getMessage());
            }
            
            // Manual creation as fallback
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            
            int matrixWidth = bitMatrix.getWidth();
            int matrixHeight = bitMatrix.getHeight();
            int cellWidth = width / matrixWidth;
            int cellHeight = height / matrixHeight;
            
            log("Manual QR creation: matrix=" + matrixWidth + "x" + matrixHeight + 
                ", cell=" + cellWidth + "x" + cellHeight);
            
            for (int x = 0; x < matrixWidth; x++) {
                for (int y = 0; y < matrixHeight; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                    }
                }
            }
            graphics.dispose();
            log("Manual QR creation completed successfully");
            return image;
            
        } catch (Exception e) {
            log("ERROR in createQRImage: " + e.getMessage());
            e.printStackTrace();
            throw new WriterException("Failed to create QR code: " + e.getMessage());
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void debugNetworkInfo() {
        try {
            log("=== Network Debug Info ===");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                log("Interface: " + ni.getName() + " - " + ni.getDisplayName());
                log("  Up: " + ni.isUp() + ", Loopback: " + ni.isLoopback());
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    log("  Address: " + addr.getHostAddress() + " (IPv4: " + (addr instanceof Inet4Address) + ")");
                }
            }
            log("=== End Debug Info ===");
        } catch (Exception e) {
            log("Debug error: " + e.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WelcomeScreen welcome = new WelcomeScreen();
            welcome.setVisible(true);
        });
    }
}

// WelcomeScreen class
class WelcomeScreen extends JFrame {
    public WelcomeScreen() {
        setTitle("Welcome to File-Fly");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setBackground(new Color(135, 206, 250));
        add(backgroundPanel);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(135, 206, 250));
        
        JLabel title = new JLabel("✈️ File-Fly", SwingConstants.CENTER);
        title.setFont(new Font("Comic Sans MS", Font.BOLD, 48));
        title.setForeground(new Color(0, 102, 204));
        titlePanel.add(title, BorderLayout.CENTER);

        JLabel subtitle = new JLabel("Share files instantly with QR codes", SwingConstants.CENTER);
        subtitle.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        subtitle.setForeground(new Color(0, 102, 204));
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        backgroundPanel.add(titlePanel, BorderLayout.CENTER);

        JPanel featurePanel = new JPanel(new GridLayout(4, 1, 5, 5));
        featurePanel.setBackground(new Color(135, 206, 250));
        featurePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        String[] features = {
            "📁 Select and send files instantly",
            "📱 Generate QR codes for easy sharing",
            "🔍 Scan QR codes to auto-connect",
            "🌐 Works on local networks"
        };

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature, SwingConstants.CENTER);
            featureLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
            featureLabel.setForeground(new Color(0, 102, 204));
            featurePanel.add(featureLabel);
        }

        backgroundPanel.add(featurePanel, BorderLayout.NORTH);

        JButton startButton = new JButton("Start File-Fly");
        startButton.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        startButton.setBackground(new Color(173, 216, 230));
        startButton.setForeground(new Color(0, 102, 204));
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(135, 206, 250));
        buttonPanel.add(startButton);
        backgroundPanel.add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> {
            dispose();
            new FileFly().setVisible(true);
        });
    }
}