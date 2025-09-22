package 论文查重;

import static org.junit.Assert.*;
import java.io.*;
import java.nio.file.*;
import java.util.Map;
import org.junit.Test;

public class PlagiarismCheckerTest {
    
    // 测试工具方法 - 计算两个文本的相似度
    private double calculateSimilarity(String text1, String text2) {
        Map<String, Integer> freq1 = PlagiarismChecker.wordFreq(text1);
        Map<String, Integer> freq2 = PlagiarismChecker.wordFreq(text2);
        
        double simBow = PlagiarismChecker.cosineSimilarity(freq1, freq2);
        double simLCS = PlagiarismChecker.lcsSimilarity(
            PlagiarismChecker.clean(text1), 
            PlagiarismChecker.clean(text2)
        );
        
        double alpha = 0.5;
        return alpha * simBow + (1 - alpha) * simLCS;
    }
    
    // 1. 相同中文文本
    @Test
    public void testIdenticalChineseText() {
        String text1 = "这是一个测试文本，用于测试论文查重功能。";
        String text2 = "这是一个测试文本，用于测试论文查重功能。";
        
        double similarity = calculateSimilarity(text1, text2);
        assertEquals(1.0, similarity, 0.01);
    }
    
    // 2. 相同英文文本
    @Test
    public void testIdenticalEnglishText() {
        String text1 = "This is a test text for plagiarism detection.";
        String text2 = "This is a test text for plagiarism detection.";
        
        double similarity = calculateSimilarity(text1, text2);
        assertEquals(1.0, similarity, 0.01);
    }
    
    // 3. 文本增删测试
    @Test
    public void testTextWithAdditionsAndDeletions() {
        String text1 = "深度学习是机器学习的分支";
        String text2 = "深度学习是机器学习的一个重要分支，它使用多层神经网络";
        
        double similarity = calculateSimilarity(text1, text2);
        assertTrue(similarity > 0.6);
    }
    
    // 4. 文本乱序测试
    @Test
    public void testTextWithReordering() {
        String text1 = "神经网络由输入层、隐藏层和输出层组成";
        String text2 = "由输入层、输出层和隐藏层组成神经网络";
        
        double similarity = calculateSimilarity(text1, text2);
        assertTrue(similarity > 0.7);
    }
    
    // 5. 完全不同文本
    @Test
    public void testCompletelyDifferentText() {
        String text1 = "机器学习是人工智能的核心";
        String text2 = "太阳系有八大行星，地球是其中之一";
        
        double similarity = calculateSimilarity(text1, text2);
        assertTrue(similarity < 0.1);
    }
    
    // 6. 部分相同文本
    @Test
    public void testPartiallySameText() {
        String text1 = "支持向量机是一种二分类模型，它的基本模型是定义在特征空间上间隔最大的线性分类器";
        String text2 = "随机森林是一种集成学习方法，它的基本模型是定义在特征空间上间隔最大的线性分类器";
        
        double similarity = calculateSimilarity(text1, text2);
        assertTrue(similarity > 0.3 && similarity < 0.8);
    }
    
    // 7. 空文本
    @Test
    public void testEmptyText() {
        String text1 = "";
        String text2 = "这是一个测试文本";
        
        double similarity = calculateSimilarity(text1, text2);
        assertEquals(0.0, similarity, 0.01);
    }
    
    // 8. 两个空文本
    @Test
    public void testBothEmptyText() {
        String text1 = "";
        String text2 = "";
        
        double similarity = calculateSimilarity(text1, text2);
        assertEquals(0.0, similarity, 0.01);
    }
    
    // 9. 文件不存在测试
    @Test
    public void testFileNotFound() {
        String[] args = {"not_exist.txt", "not_exist2.txt", "ans.txt"};

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // 调用 main，不会抛异常，而是打印错误信息
        PlagiarismChecker.main(args);

        System.setErr(originalErr); // 恢复 System.err
        String errOutput = errContent.toString();

        assertTrue("应提示文件不存在", errOutput.contains("文件不存在") || errOutput.contains("Error"));
    }

    // 10. 输出路径错误测试
    @Test
    public void testInvalidOutputPath() throws Exception {
        String orig = "orig_test.txt";
        String plag = "plag_test.txt";
        Files.writeString(Paths.get(orig), "测试原文");
        Files.writeString(Paths.get(plag), "测试抄袭版");

        String[] args = {orig, plag, "/invalid_path/ans.txt"};

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // 调用 main，不会抛异常，而是打印错误信息
        PlagiarismChecker.main(args);

        System.setErr(originalErr); // 恢复 System.err
        String errOutput = errContent.toString();

        assertTrue("应提示文件写入错误", errOutput.contains("文件读写异常") || errOutput.contains("Error"));

        // 清理测试文件
        Files.deleteIfExists(Paths.get(orig));
        Files.deleteIfExists(Paths.get(plag));
    }

}
