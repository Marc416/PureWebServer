import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8081);
            File recvFile = new File("server_recv.txt");
            File sendFile = new File("server_send.txt");
            FileOutputStream fos = new FileOutputStream(recvFile);      // File Write
            FileInputStream fis = new FileInputStream(sendFile);        // File Read

            System.out.println("클라이언트로부터 접속을기다리고 있습니다");
            Socket socket = server.accept();

            System.out.println("클라이언트 접속");
            int ch;
            // 클라이언트로부터 수신한 내용을 server_recv.txt에 저장
            InputStream input= socket.getInputStream();
            while((ch = input.read()) !=0){
                fos.write(ch);
            }

            // server_send.txt에 저장된 내용을 클라이언트로 전송
            OutputStream output = socket.getOutputStream();
            while ((ch = fis.read()) != -1) {
                output.write(ch);
            }
            socket.close();
            System.out.println("클라이언트 접속 종료");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
