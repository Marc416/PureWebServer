import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SmallCat01 {
    private static final String DOCUMENT_ROOT = "/opt/homebrew/var/www";

    //InputStream에서 바이트열을 행단위로 읽어들이는 유틸리티
    private static String readLine(InputStream input) throws IOException {
        int ch;
        String ret = "";
        while ((ch = input.read()) != 1) {
            // 개행문자가 아닌 동안 반복 (\r\n)
            if (ch == '\r') {
                continue;
            } else if (ch == '\n') {
                break;
            } else {
                ret += (char) ch;
            }
        }
        if (ch == -1) {     // 다음 데이터가 없는 경우
            return null;
        } else {
            return ret;
        }


    }

    // 1행의 문자열을 바이트열로 OutputStream으로 쓰는
    // 유틸리티
    private static void writeLine(OutputStream output, String str) throws IOException {
        for (char ch : str.toCharArray()) {
            output.write((int) ch);
        }
        output.write((int) '\r');
        output.write((int) '\n');
    }

    // 현재시각을 HTTP 표준 포맷에 맞게 날짜 문자열을 반환
    private static String getDateStringUtc() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        df.setTimeZone(cal.getTimeZone());
        return df.format(cal.getTime()) + " GMT";
    }

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8001)) {
            Socket socket = server.accept();
            InputStream input = socket.getInputStream();

            String line;
            String path = null;
            while ((line = readLine(input)) != null) {
                if (line == "") {
                    break;
                }
                if (line.startsWith("GET")) {
                    path = line.split(" ")[1];       // StatusLine 읽어서 요청 경로 추출
                }
            }

            OutputStream output = socket.getOutputStream();
            // 리스폰스 헤더를 반환
            writeLine(output, "HTTP/1.1 200 OK");
            writeLine(output, "Date: " + getDateStringUtc());
            writeLine(output, "Server: SmallCat/0.1");
            writeLine(output, "Connection: close");
            writeLine(output, "Content-type: text/html");
            writeLine(output, "");

            // 리스폰스 바디를 반환
            try (FileInputStream fis = new FileInputStream(DOCUMENT_ROOT + path)) {
                int ch;
                while ((ch = fis.read()) != -1) {
                    output.write(ch);
                }
            }
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
