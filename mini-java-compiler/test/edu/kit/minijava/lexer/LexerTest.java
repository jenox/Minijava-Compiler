package edu.kit.minijava.lexer;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static junit.framework.TestCase.*;

@RunWith(Parameterized.class)
public class LexerTest {

    public static final String TEST_DIR = "test/edu/kit/minijava/lexer/testcases/";

    @Parameters()
    public static File[] data() {
        File dir = new File(TEST_DIR);
        File[] files = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".json");
            }
        });
        System.out.println(files.length);
        return files;
    }


    /**
     * current file under test
     */
    @Parameter
    public static File file;

    /**
     * runs all testcases in testcases folder
     */
    @Test
    public void test() {
        assertTrue(this.file.exists());

        try {
            String text = new String(Files.readAllBytes(this.file.toPath()));
            JSONObject object = new JSONObject(text);

            assertNotNull(text);
            assertNotNull(object);
            assertTrue(object.get("in") instanceof String);
            assertTrue(object.get("out") instanceof JSONArray);

            List<String> receivedTokens = this.receivedTokensWhenLexing(object.getString("in"));
            List<String> expectedTokens = this.expectedTokensInArray(object.getJSONArray("out"));

            assertEquals(receivedTokens, expectedTokens);
        } catch (Exception e) {
            fail();
        }
    }

    private List<String> receivedTokensWhenLexing(String text) {
        Lexer lexer = new Lexer(text);
        List<String> receivedTokens = new ArrayList<>();

        try {
            while (true) {
                Token token = lexer.nextToken();

                if (token != null) {
                    receivedTokens.add(token.toString());
                } else {
                    break;
                }
            }
        } catch (LexerException exception) {
            receivedTokens.add("ERROR");
        }

        return receivedTokens;
    }

    private List<String> expectedTokensInArray(JSONArray array) {
        List<String> expectedTokens = new ArrayList<>();

        for (Object element : array) {
            assertTrue(element instanceof String);

            expectedTokens.add((String)element);
        }

        return expectedTokens;
    }
}
