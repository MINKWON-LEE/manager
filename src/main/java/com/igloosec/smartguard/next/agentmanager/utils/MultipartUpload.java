package com.igloosec.smartguard.next.agentmanager.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MultipartUpload {

    private final String boundary;
    private final String tail;
    private static final String LINE_END = "\r\n";
    private static final String TWOHYPEN = "--";
    private HttpURLConnection httpConn;
    private String charset;
    private static final String TAG = "MultipartUtility";
    private int maxBufferSize = 1024;

    public MultipartUpload(String requestURL, String charset) throws IOException {
        this.charset = charset;

        boundary = "===" + System.currentTimeMillis() + "===";
        tail = LINE_END + TWOHYPEN + boundary + TWOHYPEN + LINE_END;
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    }

    public String upload(HashMap<String, String> params, HashMap<String, String> files) throws IOException {
        String paramsPart = "";
        String fileHeader = "";
        String filePart = "";
        long fileLength = 0;

        ArrayList<String> paramHeaders = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {

            String param = TWOHYPEN + boundary + LINE_END
                    + "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END
                    + "Content-Type: text/plain; charset=" + charset + LINE_END
                    + LINE_END
                    + entry.getValue() + LINE_END;
            paramsPart += param;
            paramHeaders.add(param);
        }

        ArrayList<File> filesAL = new ArrayList<>();
        ArrayList<String> fileHeaders = new ArrayList<>();

        for (Map.Entry<String, String> entry : files.entrySet()) {

            File file = new File(entry.getValue());
            fileHeader = TWOHYPEN + boundary + LINE_END
                    + "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + file.getName() + "\"" + LINE_END
                    + "Content-Type: " + URLConnection.guessContentTypeFromName(file.getAbsolutePath()) + LINE_END
                    + "Content-Transfer-Encoding: binary" + LINE_END
                    + LINE_END;
            fileLength += file.length() + LINE_END.getBytes(charset).length;
            filePart += fileHeader;

            fileHeaders.add(fileHeader);
            filesAL.add(file);
        }
        String partData = paramsPart + filePart;

        long requestLength = partData.getBytes(charset).length + fileLength + tail.getBytes(charset).length;
        httpConn.setRequestProperty("Content-length", "" + requestLength);
        httpConn.setFixedLengthStreamingMode((int) requestLength);
        httpConn.connect();

        OutputStream outputStream = new BufferedOutputStream(httpConn.getOutputStream());
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

        for (int i = 0; i < paramHeaders.size(); i++) {
            writer.append(paramHeaders.get(i));
            writer.flush();
        }

        int totalRead = 0;
        int bytesRead;
        byte buf[] = new byte[maxBufferSize];
        for (int i = 0; i < filesAL.size(); i++) {
            writer.append(fileHeaders.get(i));
            writer.flush();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filesAL.get(i)));
            while ((bytesRead = bufferedInputStream.read(buf)) != -1) {

                outputStream.write(buf, 0, bytesRead);
                writer.flush();
                totalRead += bytesRead;
            }
            outputStream.flush();
            bufferedInputStream.close();
        }
        writer.append(tail);
        writer.flush();
        writer.close();

        // checks server's status code first
        String ret = "";
        Map<String, List<String>> headers = httpConn.getHeaderFields();
        Set<String> setS = headers.keySet();
        for(String key : setS) {
            log.debug(key + " pjkim : " + httpConn.getHeaderField(key));
            ret = key;
        }

        return ret;
    }
}

