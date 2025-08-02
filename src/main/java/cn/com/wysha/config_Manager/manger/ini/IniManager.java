package cn.com.wysha.config_Manager.manger.ini;

import cn.com.wysha.config_Manager.manger.Manager;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class IniManager extends Manager {

    private Ini getIniFile(String path) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return new Ini(file);
            }else {
                return new Ini();
            }
        } catch (InvalidFileFormatException e) {
            if (file.exists()){
                file.delete();
            }
            return new Ini();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void read(Object obj, String filePath, Map<String, Set<FieldElement>> map) {
        try {
            Ini ini = getIniFile(filePath);

            map.forEach((sectionPath, set) -> {
                Profile.Section section = ini.get(sectionPath);

                if (section != null){
                    set.forEach((fieldElement) -> {
                        String value = section.get(fieldElement.key());

                        if (value != null){
                            try {
                                fieldElement.field().set(obj, fieldElement.converter().stringToObj(value));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void write(Object obj, String filePath, Map<String, Set<FieldElement>> map) {
        try {
            Ini ini = getIniFile(filePath);

            map.forEach((sectionPath, set) -> {
                Profile.Section section = ini.get(sectionPath);
                if (section == null){
                    section = ini.add(sectionPath);
                }

                Profile.Section finalSection = section;
                set.forEach((fieldElement) -> {
                    try {
                        Object value = fieldElement.field().get(obj);

                        finalSection.put(fieldElement.key(), fieldElement.converter().objToString(value));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
            });

            ini.store(new File(filePath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
