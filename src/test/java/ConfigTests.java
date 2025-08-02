import cn.com.wysha.config_Manager.annotations.ConfigField;
import cn.com.wysha.config_Manager.manger.Manager;
import cn.com.wysha.config_Manager.annotations.Config;
import cn.com.wysha.config_Manager.manger.ini.IniManager;
import cn.com.wysha.config_Manager.manger.xml.XmlManager;
import org.junit.Test;

import java.util.Objects;

public class ConfigTests {
    @Test
    public void test(){
        try {
            TestClass testObj = new TestClass();
            Manager manager = new XmlManager();
            manager.readStaticConfig();
            testObj.a = "TEST0";
            manager.writeObject(testObj);
            manager.readObject(testObj);
            if (!Objects.equals(testObj.a, "TEST0")) throw new RuntimeException();
            testObj.a = "TEST1";
            manager.writeObject(testObj);
            manager.readObject(testObj);
            if (!Objects.equals(testObj.a, "TEST1")) throw new RuntimeException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@Config(parentFilePath = "testData/config.txt", parentSectionPath = "ABC")
class TestClass {
    @ConfigField(sectionPath = "aaa111")
    public String a;
    @ConfigField(sectionPath = ".aaa222")
    public String b = "123";
    @ConfigField
    public String c = "456";
}

