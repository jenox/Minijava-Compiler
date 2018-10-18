package edu.kit.minijava.lexer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

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
        System.out.println(file.exists());
        String input = null;
        Lexer lexer = null;
        try {
            input = new String(Files.readAllBytes(file.toPath()));
            lexer = new Lexer(file.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONObject object = new JSONObject(input);
        String in = (String) object.get("in");
        JSONArray out = (JSONArray) object.get("out");
        // System.out.println(in);
        // System.out.println(out);

        //TODO: invoke lexer and compare results
    }

}
