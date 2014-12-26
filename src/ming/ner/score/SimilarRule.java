package ming.ner.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mingleis on 2014/12/25.
 */
public class SimilarRule implements Rule{
    private Map<String ,String[] > similarDict;
    private String similarDictPath = "./score/similarDict.txt";
    private static String  SPILIT=",";
    private static String  SPILITKEY = ":";
    private static float MATCHSCORE = 1.0f;

    public SimilarRule() {
        super();
        this.loadSimilarDict();
    }
    /**
     * 同一词分词，若A、B为同义词，B、C为同义词，则ABC为同义词，同义词文件格式为A,B,C， 加载进内存的map结构为 A：ABC，B：ABC，C:ABC
     * @return
     */
    private boolean loadSimilarDict() {
        try {
            similarDict = new HashMap<String, String[]>();
            File file = new File(similarDictPath);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedInputStream = new BufferedReader(reader);
            String cur = bufferedInputStream.readLine();
            while (cur != null && cur != "") {
                String [] curarray = cur.split(SPILIT);
                for (String cureach : curarray) {
                    similarDict.put(cureach, curarray);
                }
                cur = bufferedInputStream.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public float compareWords(String word1, String word2) {
        float score = 0.0f;
        if (similarDict.get(word1) != null) {
            for (String tempeach : similarDict.get(word1)) {
                if (word2.equalsIgnoreCase(tempeach)) {
                    score = MATCHSCORE;
                    break;
                }
            }
        }
        return  score;
    }
}
