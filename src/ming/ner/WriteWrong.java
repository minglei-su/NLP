package ming.ner;

import java.io.*;

/**
 * Created by mingleis on 2014/12/15.
 */
public class WriteWrong {
    private String filePath="./models/dbwrong.txt";
    private FileWriter fileWriter;
    private static WriteWrong writeWrong;
    private WriteWrong() {
        try {
            File file = new File(filePath);
            fileWriter = new FileWriter(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static WriteWrong getInstance() {
        if (writeWrong == null) {
            writeWrong = new WriteWrong();
        }
        return  writeWrong;
    }
    public void wrintInfo(String[][] s) {
        try {
            for (int i=0;i < s[0].length; i++)
                fileWriter.write(s[0][i]);
            fileWriter.write("\r\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
