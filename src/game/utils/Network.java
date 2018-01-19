package game.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;

public class Network {

    private static ArrayList<String> ipAddresses = new ArrayList<>();

    private static boolean isPortAvailable(int port) {
        boolean portAvailable = true;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            portAvailable = false;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return portAvailable;
    }

    public static int getFreePort() {
        int port = 3000;
        while (true) {
            if (isPortAvailable(port)) break;
            else port++;
        }
        return port;
    }

    public static String ipAddresses() {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                Enumeration ee = ((NetworkInterface) e.nextElement()).getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String temp = i.getHostAddress();
                    if ((temp.charAt(1) == '7' || temp.charAt(1) == '9') && (temp.charAt(2) == '2')) {
                        ipAddresses.add(temp);
                        System.out.println(temp);
                    } else if (temp.charAt(0) == '1' && temp.charAt(1) == '0') {
                        ipAddresses.add(temp);
                        System.out.println(temp);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (ipAddresses.size() < 1) ipAddresses.add("127.0.0.1");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ipAddresses.size(); i++) sb.append(ipAddresses.get(i)).append(i == ipAddresses.size() - 1 ? "" : " | ");
        ipAddresses.clear();
        return sb.toString();
    }
}
