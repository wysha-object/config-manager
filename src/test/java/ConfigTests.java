import cn.com.wysha.config_Manager.config.ConfigField;
import cn.com.wysha.config_Manager.manger.Manager;
import cn.com.wysha.config_Manager.manger.ini.IniManager;
import cn.com.wysha.config_Manager.config.Config;
import org.junit.Test;

import java.util.Objects;

public class ConfigTests {
    @Test
    public void test(){
        try {
            Manager manager = new IniManager();
            manager.readStaticConfig();
            TestClass.a = "TEST0";
            manager.writeStaticConfig();
            manager.readStaticConfig();
            if (!Objects.equals(TestClass.a, "TEST0")) throw new RuntimeException();
            TestClass.a = "TEST1";
            manager.writeStaticConfig();
            manager.readStaticConfig();
            if (!Objects.equals(TestClass.a, "TEST1")) throw new RuntimeException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@Config(parentFilePath = "testData/config.ini", parentSectionPath = "ABC")
class TestClass {
    @ConfigField
    public static String a;
}

