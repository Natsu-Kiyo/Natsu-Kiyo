package 论文查重;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PlagiarismChecker {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Error: 参数数量错误！");
            System.err.println("Usage: java -jar main.jar [origFile] [plagFile] [ansFile]");
            return;
        }

        String origPath = args[0];
        String plagPath = args[1];
        String ansPath  = args[2];

        try {
            // 尝试读取原文和抄袭文
            String text1 = Files.readString(Paths.get(origPath));
            String text2 = Files.readString(Paths.get(plagPath));

            Map<String, Integer> freq1 = wordFreq(text1);
            Map<String, Integer> freq2 = wordFreq(text2);

            double simBow = cosineSimilarity(freq1, freq2);
            double simLCS = lcsSimilarity(clean(text1), clean(text2));

            double alpha = 0.5; // Bag-of-Words 和 LCS 各占一半
            double sim = alpha * simBow + (1 - alpha) * simLCS;

            
            // 尝试写入文件
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ansPath))) {
                writer.write(String.format(Locale.ROOT, "%.2f", sim));
            }

        } catch (NoSuchFileException e) {
            System.err.println("Error: 文件不存在 - " + e.getFile());
        } catch (IOException e) {
            System.err.println("Error: 文件读写异常 - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: 程序运行出现异常 - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String clean(String text) {
        return text.replaceAll("[^\\u4E00-\\u9FFFa-zA-Z0-9]", "");
    }

    public static Map<String, Integer> wordFreq(String text) {
        Map<String, Integer> map = new HashMap<>();
        // 只保留中英文和数字
        text = text.replaceAll("[^\\u4E00-\\u9FFFa-zA-Z0-9]", "");
        
        // 如果是中文（没有空格），逐字分词
        // 如果是英文（带空格），按空格分词
        String[] tokens;
        if (text.contains(" ")) {
            tokens = text.trim().split("\\s+");
        } else {
            tokens = text.split(""); // 中文逐字
        }
        
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            map.put(t, map.getOrDefault(t, 0) + 1);
        }
        return map;
    }

    public static double cosineSimilarity(Map<String, Integer> a, Map<String, Integer> b) {
        Set<String> all = new HashSet<>();
        all.addAll(a.keySet());
        all.addAll(b.keySet());
        double dot = 0, na = 0, nb = 0;
        for (String key : all) {
            int va = a.getOrDefault(key, 0);
            int vb = b.getOrDefault(key, 0);
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public static double lcsSimilarity(String a, String b) {
        // 处理空字符串的情况
        if (a == null || b == null) {
            return 0.0;
        }
        
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;
        }
        
        int lcs = LCSLength(a, b);
        int totalLength = a.length() + b.length();
        
        // 确保不会除以零
        if (totalLength == 0) {
            return 0.0;
        }
        
        return (2.0 * lcs) / totalLength;
    }

    public static int LCSLength(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[n][m];
    }
}