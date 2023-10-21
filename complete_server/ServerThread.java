package complete_server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ServerThread implements Runnable {
    private static final String DOCUMENT_ROOT = "/opt/homebrew/var/www";
    private Socket socket;

    // InputStream으로 부터 바이트열을 행단위로 읽어들이는 유틸리티 메서드
    private static String readLine(InputStream input) throws Exception {
        int ch;
        String ret = "";
        while ((ch = input.read()) != -1) {
            // 개행문자가 아닌 동안 반복 (\r\n : CR+LF 커서를 가장 앞으로 이동 후 다음 줄로 이동)
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

    // 한 행의 문자열을 바이트열로서 OutputStream으로 쓰는 유틸리티 메서드
    private static void writeLine(OutputStream output, String str) throws Exception {
        for (char ch : str.toCharArray()) {
            output.write((int) ch);
        }
        output.write((int) '\r');
        output.write((int) '\n');
    }

    // 현재시각 부터 HTTTP 표준 포맷에 맞게 날짜 문자열을 반환
    private static String getDateStringUtc() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        df.setTimeZone(cal.getTimeZone());
        return df.format(cal.getTime()) + " GMT";
    }

    // 확장자와 Content-Type 매핑
    private static HashMap<String, String> contentTypeMap =
            new HashMap<String, String>() {
                {
                    put("html", "text/html");
                    put("htm", "text/html");
                    put("txt", "text/plain");
                    put("css", "text/css");
                    put("png", "image/png");
                    put("jpg", "image/jpeg");
                    put("jpeg", "image/jpeg");
                    put("gif", "image/gif");
                }

            };
    //파일 확장자에 따른 ContentType을 반환
    private static String getContentType(String ext){
        String ret = contentTypeMap.get(ext.toLowerCase());
        if(ret == null){
            return"application/octet-stream";
        }else{
            return ret;
        }
    }

    @Override
    public void run() {
        OutputStream output;
        try {
            InputStream input = socket.getInputStream();
            String line;
            String path = null;
            String ext = null;
            while ((line = readLine(input)) != null) {
                if (line.equals("")) {
                    break;
                }
                if (line.startsWith("GET")) {
                    path = line.split(" ")[1];
                    String[] tmp = path.split("\\.");
                    /** regex 보충설명
                     * path = "example\\file.txt"
                     * parts = path.split("\\.")  # Split the string using a backslash followed by a period as the delimiter
                     * print(parts)  # Output: ['example\\file', 'txt']
                     */
                    ext = tmp[tmp.length - 1];    // 확장자를 추출
                }
            }
            output = socket.getOutputStream();
            // 리스폰스 헤더를 반환
            writeLine(output, "HTTP/1.1 200 OK");
            writeLine(output, "Date: " + getDateStringUtc());
            writeLine(output, "Server: SmallCat/0.1");
            writeLine(output, "Connection: close");
            writeLine(output, "Content-type: " + getContentType(ext));
            writeLine(output, "");

            // 리스폰스 바디를 반환
            try (FileInputStream fis1 = new FileInputStream(DOCUMENT_ROOT + path)) {
                int ch;
                while ((ch = fis1.read()) != -1) {
                    output.write(ch);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    ServerThread(Socket socket) {
        this.socket = socket;
    }
}
