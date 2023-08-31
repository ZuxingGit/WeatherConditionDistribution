import com.DS.utils.fileScanner.ReadFile;
import com.DS.utils.json.JSONHandler;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AllClassTest {
    static final String currentWorkDirectory = System.getProperty("user.dir");

    private static final String C = "contentServer";
    private static final String A = "aggregationServer";
    
    
    @Test
    public void readFile2JSON_Test() {
        ReadFile readFile=new ReadFile();
        JSONHandler handler=new JSONHandler();
        String origin = readFile.readFrom("", "source.txt", C);
        String JSON = handler.string2JSON(origin);
        System.out.println(JSON);
        
        assertTrue(JSON.contains("{"));
        assertTrue(JSON.contains("\""));
        assertTrue(JSON.contains("{"));
    }
}
