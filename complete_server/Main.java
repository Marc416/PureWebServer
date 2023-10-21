package complete_server;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        try(ServerSocket server = new ServerSocket(8001)){
            // 소켓이 무한루프에 있기 때문에 꺼지지않음.
            for(;;){
                Socket socket = server.accept();
                ServerThread serverThread = new ServerThread(socket);
                Thread thread = new Thread(serverThread);
                thread.start();
            }
        }
    }
}
