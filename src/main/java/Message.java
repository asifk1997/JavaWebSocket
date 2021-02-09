import javax.json.Json;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Message {
    private static String myAddress;
    public static BufferedReader bufferedReader;
    public static String[] peerList;
    public static ServerThread serverThread;
    public static void main(String[] args) throws Exception {
        peerList = new String[]{"localhost:4001", "localhost:4002","localhost:4003"};
//        myAddress = "Alice,localhost:4001";
        myAddress = "Bob,localhost:4002";
//        myAddress = "Carla,localhost:4003";
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter username and port for this peer");
        System.out.println(myAddress);
        String[] setupValues = myAddress.split(":");
        serverThread = new ServerThread(setupValues[1]);
        serverThread.start();
        updatePeers();
    }
    public static void updatePeers() throws Exception{
        new Message().updateListenToPeers(bufferedReader,myAddress.split(",")[0],serverThread,peerList,myAddress.split(",")[1]);
    }

    public void updateListenToPeers(BufferedReader bufferedReader, String username, ServerThread serverThread, String[] peerList, String myAddress) throws Exception{
//        System.out.println("Enter space separated username:port#");
//        System.out.println("peers to recieve messages from (s to skip)");
//        String input = bufferedReader.readLine();
        System.out.println("updateListenToPeers");
        String[] inputValues = peerList;
        //if (!input.equals("s"))
        for (int i=0;i<inputValues.length;i++){
//                System.out.println(inputValues[i]);
            if (inputValues[i].equals(myAddress)) continue;
            String[] address = inputValues[i].split(":");
            Socket socket = null;
            try {
                socket = new Socket(address[0],Integer.valueOf(address[1]));
                new PeerThread(socket).start();
            }catch (Exception e){
                if (socket!=null) socket.close();
                else System.out.println("invalid input. skipping to next step");
            }
        }
        communicate(bufferedReader,username,serverThread,peerList,myAddress);
    }
    public void communicate(BufferedReader bufferedReader, String username, ServerThread serverThread, String[] peerList, String myAddress){
        System.out.println("communicate");
        try {
            System.out.println("> you can communicate (e to exit, c to change)");
            boolean flag = true;
            while (flag){
                String message = bufferedReader.readLine();
                if (message.equals("e")){
                    flag = false;
                    break;
                }else if (message.equals("c")){
                    updateListenToPeers(bufferedReader,username,serverThread,peerList,myAddress);
                }else {
                    StringWriter stringWriter = new StringWriter();
                    Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                            .add("username",username)
                            .add("message",message)
                            .add("address",myAddress)
                            .build());
                    serverThread.sendMessage(stringWriter.toString());
                }
            }
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ServerThreadThread serverThreadThread;
    public static class ServerThread extends Thread{
        private ServerSocket serverSocket;
        private Set<ServerThreadThread> serverThreadThreads = new HashSet<ServerThreadThread>();
        public ServerThread(String portNumb) throws IOException {
            System.out.println("ServerThread");
            serverSocket = new ServerSocket(Integer.parseInt(portNumb));
        }

        public  void run(){
            try {
                System.out.println("ServerThread run");
                while (true){
                    System.out.println("server thread running");
                    //System.out.println("serverSocket address"+serverSocket.getLocalPort()+" "+serverSocket.getLocalSocketAddress().toString());
                    Socket clientSocket = null;
                    try {
                        clientSocket = serverSocket.accept();
                        System.out.println("Connected from " + clientSocket .getInetAddress() + " on port "
                                + clientSocket .getPort() + " to port " + clientSocket .getLocalPort() + " of "
                                + clientSocket .getLocalAddress());
                    } catch (IOException e) {
                        System.exit(1);
                    }
                    serverThreadThread = new ServerThreadThread(clientSocket, serverThread);
//                    updatePeers();
                    //System.out.println(serverSocket.toString());
                    serverThreadThreads.add(serverThreadThread);
                    serverThreadThread.start();
                    System.out.println("server thread running");
                }
            }catch (Exception e) { e.printStackTrace(); }
        }
        void sendMessage(String message){
            try {
                System.out.println("ServerThread sendMessage");
                serverThreadThreads.forEach(t-> t.getPrintWriter().println(message));
            }catch (Exception e){ e.printStackTrace(); }
        }
        public Set<ServerThreadThread> getServerThreadThreads(){ return  serverThreadThreads; }
    }


    public static class ServerThreadThread extends Thread{
        private ServerThread serverThread;
        private Socket socket;
        private PrintWriter printWriter;

        public ServerThreadThread(Socket socket, ServerThread serverThread){
            System.out.println("ServerThreadThread ");
            this.serverThread = serverThread;
            this.socket = socket;
        }

        public void run(){
            try {
                System.out.println("ServerThreadThread run");
                String ip=(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
                System.out.println("IP"+ip);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.printWriter = new PrintWriter(socket.getOutputStream(),true);
                while (true) serverThread.sendMessage(bufferedReader.readLine());

            }catch (Exception e) { serverThread.getServerThreadThreads().remove(this); }
        }

        public PrintWriter getPrintWriter() { return printWriter ;}
    }
}

