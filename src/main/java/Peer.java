import javax.json.Json;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Socket;

public class Peer {
    private static String myAddress;
    public static BufferedReader bufferedReader;
    public static String[] peerList;
    public static ServerThread serverThread;
    public static void main(String[] args) throws Exception {
        peerList = new String[]{"localhost:4001", "localhost:4002"};
        myAddress = "Alice,localhost:4001";
//        myAddress = "Bob,localhost:4002";
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter username and port for this peer");
        String[] setupValues = myAddress.split(":");
        serverThread = new ServerThread(setupValues[1]);
        serverThread.start();
        updatePeers();
    }

    public static void updatePeers() throws Exception{
        new Peer().updateListenToPeers(bufferedReader,myAddress.split(",")[0],serverThread,peerList,myAddress.split(",")[1]);
    }

    public void updateListenToPeers(BufferedReader bufferedReader,String username,ServerThread serverThread,String[] peerList, String myAddress) throws Exception{
//        System.out.println("Enter space separated username:port#");
//        System.out.println("peers to recieve messages from (s to skip)");
//        String input = bufferedReader.readLine();

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
    public void communicate(BufferedReader bufferedReader,String username,ServerThread serverThread,String[] peerList,String myAddress){
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
}
