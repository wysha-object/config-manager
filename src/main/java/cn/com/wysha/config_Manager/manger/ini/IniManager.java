package cn.com.wysha.config_Manager.manger.ini;

import cn.com.wysha.config_Manager.manger.Manager;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动读取和写入ini配置文件
 */
public class IniManager extends Manager {
    private final Map<String, Ini> iniMap = new HashMap<>();

    private Ini getIniFile(String path) {
        return iniMap.computeIfAbsent(path, k ->{
            try {
                File file = new File(path);
                if (!file.exists()) {
                    File fileParent = file.getParentFile();
                    if (fileParent!=null&&!fileParent.exists()) {
                        fileParent.mkdirs();
                    }
                    file.createNewFile();
                }
                return new Ini(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    protected void read(Object obj, String filePath, String sectionPath, FieldElement fieldElement) {
        try {
            Ini ini = getIniFile(filePath);

            Profile.Section section = ini.get(sectionPath);

            if (section != null){
                String value = section.get(fieldElement.key());

                if (value != null){
                    fieldElement.field().set(obj, fieldElement.converter().stringToObj(value));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void write(Object obj, String filePath, String sectionPath, FieldElement fieldElement) {
        try {
            Ini ini = getIniFile(filePath);

            Profile.Section section = ini.computeIfAbsent(sectionPath, o->ini.add(sectionPath));

            Object value = fieldElement.field().get(obj);

            if (value != null){
                section.put(fieldElement.key(), fieldElement.converter().objToString(value));
            }

            ini.store();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void clear() {
        iniMap.clear();
    }
}
