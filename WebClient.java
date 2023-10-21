import java.io.*;
import java.net.Socket;

public class WebClient {
    public static void main(String[] args) {
        try{
            Socket socket = new Socket("localhost", 8001);
            File recvFile = new File("client_recv.txt");
            File sendFile = new File("client_send.txt");
            FileInputStream fis = new FileInputStream(sendFile);
            FileOutputStream fos = new FileOutputStream(recvFile);

            int ch;
            // client_send.txt에 저장된 내용을 서버에 송신
            OutputStream output = socket.getOutputStream();
            while((ch=fis.read())!=-1){
                output.write(ch);
            }

//            output.write(0); // 전송의 끝을 알리기 위해 0을 송신
            InputStream input = socket.getInputStream();
            while ((ch = input.read()) != -1) {
                fos.write(ch);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
