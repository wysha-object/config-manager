package cn.com.wysha.config_Manager.manger;

import cn.com.wysha.config_Manager.annotations.Config;
import cn.com.wysha.config_Manager.annotations.ConfigField;
import cn.com.wysha.config_Manager.converter.ConfigConverter;
import cn.com.wysha.config_Manager.converter.DefaultConverter;
import io.github.classgraph.ClassGraph;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class Manager {
    private static Map<String, Map<String, Set<FieldElement>>> staticTmp = null;
    private static final Map<Class<?>, Map<String, Map<String, Set<FieldElement>>>> classMemberTmp = new HashMap<>();

    protected List<Class<?>> getAllConfigType() {
        return new ClassGraph().enableAllInfo().scan().getClassesWithAnnotation(Config.class).loadClasses();
    }

    /**
     * @return map = {file, setMap}
     * <br/>
     * setMap = {section, set}
     * <br/>
     * file -> section -> key -> value
     */
    protected Map<String, Map<String, Set<FieldElement>>> getConfigField(Class<?> c) {
        Map<String, Map<String, Set<FieldElement>>> rs = new HashMap<>();

        Config config = c.getAnnotation(Config.class);

        String parentPath = config.parentFilePath();
        String parentSection = config.parentSectionPath();

        Field[] fields = c.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ConfigField.class)) {//field是需要执行IO操作的
                try {
                    field.setAccessible(true);

                    String file = parentPath;
                    String section = parentSection;
                    String key = field.getName();
                    Class<? extends ConfigConverter<?>> converterClass = DefaultConverter.class;

                    ConfigField configField = field.getAnnotation(ConfigField.class);
                    file += configField.filePath();
                    section += configField.sectionPath();
                    if (!configField.key().isBlank()) key = configField.key();
                    if (configField.converter() != null) converterClass = configField.converter();

                    ConfigConverter<?> converter = converterClass.getDeclaredConstructor().newInstance();

                    rs.computeIfAbsent(file, k -> new HashMap<>()).computeIfAbsent(section, k -> new HashSet<>()).add(new FieldElement(key, field, converter));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return rs;
    }

    private void apply(String grandFilePath, String grandSectionPath, BiConsumer<String, Map<String, Set<FieldElement>>> consumer, Map<String, Map<String, Set<FieldElement>>> tmp) {
        Map<String, Map<String, Set<FieldElement>>> rs;

        if (grandFilePath.isEmpty()){
            rs = tmp;
        }else {
            rs = new HashMap<>();
            tmp.forEach((k, v) -> rs.put(grandFilePath + k, v));
        }

        if (!grandSectionPath.isEmpty()){
            for (Map.Entry<String, Map<String, Set<FieldElement>>> entry : rs.entrySet()){
                Map<String, Set<FieldElement>> t = new HashMap<>();
                entry.getValue().forEach((k, v) -> t.put(grandSectionPath + k, v));
                entry.setValue(t);
            }
        }

        rs.forEach(consumer);
    }

    protected void forEachStaticConfig(String grandFilePath, String grandSectionPath, BiConsumer<String, Map<String, Set<FieldElement>>> consumer) {
        if (staticTmp == null) {
            staticTmp = new HashMap<>();
            List<Class<?>> classes = getAllConfigType();
            for (Class<?> c : classes) {
                Map<String, Map<String, Set<FieldElement>>> map = getConfigField(c);
                map.forEach((file, setMap) -> setMap.forEach((section, set) -> set.forEach((e) -> {
                    if (Modifier.isStatic(e.field().getModifiers())) {
                        Map<String, Set<FieldElement>> rsSetMap = staticTmp.computeIfAbsent(file, (k) -> new HashMap<>());
                        Set<FieldElement> rsSet = rsSetMap.computeIfAbsent(section, (k) -> new HashSet<>());
                        rsSet.add(e);
                    }
                })));
            }
        }

        apply(grandFilePath, grandSectionPath, consumer, staticTmp);
    }

    protected void forEachClassMemberConfig(String grandFilePath, String grandSectionPath, Class<?> c, BiConsumer<String, Map<String, Set<FieldElement>>> consumer) {
        Map<String, Map<String, Set<FieldElement>>> tmp = classMemberTmp.computeIfAbsent(c, (k) -> {
            Map<String, Map<String, Set<FieldElement>>> rsMap = new HashMap<>();

            Map<String, Map<String, Set<FieldElement>>> map = getConfigField(c);
            map.forEach((file, setMap) -> setMap.forEach((section, set) -> set.forEach((e) -> {
                if (!Modifier.isStatic(e.field().getModifiers())) {
                    Map<String, Set<FieldElement>> rsSetMap = rsMap.computeIfAbsent(file, (key) -> new HashMap<>());
                    Set<FieldElement> rsSet = rsSetMap.computeIfAbsent(section, (key) -> new HashSet<>());
                    rsSet.add(e);
                }
            })));
            return rsMap;
        });

        apply(grandFilePath, grandSectionPath, consumer, tmp);
    }

    public void readStaticConfig(String grandFilePath, String grandSectionPath) {
        forEachStaticConfig(grandFilePath, grandSectionPath, (file, map) -> read(null, file, map));
    }

    public void writeStaticConfig(String grandFilePath, String grandSectionPath) {
        forEachStaticConfig(grandFilePath, grandSectionPath, (file, map) -> write(null, file, map));
    }

    public void readStaticConfig() {
        readStaticConfig("", "");
    }

    public void writeStaticConfig() {
        writeStaticConfig("", "");
    }

    public void readObject(Object obj, String grandFilePath, String grandSectionPath) {
        Class<?> c = obj.getClass();
        forEachClassMemberConfig(grandFilePath, grandSectionPath, c, (file, map) -> read(obj, file, map));
    }

    public void writeObject(Object obj, String grandFilePath, String grandSectionPath) {
        Class<?> c = obj.getClass();
        forEachClassMemberConfig(grandFilePath, grandSectionPath, c, (file, map) -> write(obj, file, map));
    }

    public void readObject(Object obj) {
        readObject(obj, "", "");
    }

    public void writeObject(Object obj) {
        writeObject(obj, "", "");
    }

    /**
     * @param obj      指定对象,若字段为static,则置为null
     * @param filePath 文件路径
     * @param map      段映射表
     */
    protected abstract void read(Object obj, String filePath, Map<String, Set<FieldElement>> map);

    /**
     * @param obj      指定对象,若字段为static,则置为null
     * @param filePath 文件路径
     * @param map      段映射表
     */
    protected abstract void write(Object obj, String filePath, Map<String, Set<FieldElement>> map);

    protected record FieldElement(String key, Field field, ConfigConverter converter) {
    }
}