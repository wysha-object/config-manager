package cn.com.wysha.config_Manager.manger;

import cn.com.wysha.config_Manager.config.Config;
import cn.com.wysha.config_Manager.config.ConfigField;
import cn.com.wysha.config_Manager.converter.ConfigConverter;
import cn.com.wysha.config_Manager.converter.DefaultConverter;
import io.github.classgraph.ClassGraph;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class Manager {
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
    protected Map<String, Map<String, Set<FieldElement>>> getConfigField(Class<?> c) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, Map<String, Set<FieldElement>>> rs = new HashMap<>();

        Config config = c.getAnnotation(Config.class);

        String parentPath = config.parentFilePath();
        String parentSection = config.parentSectionPath();

        Field[] fields = c.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ConfigField.class)) {//field是需要执行IO操作的
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
            }
        }
        return rs;
    }

    protected void forEachStaticConfig(Manager.Consumer consumer) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Class<?>> classes = getAllConfigType();
        for (Class<?> c : classes) {
            Map<String, Map<String, Set<FieldElement>>> map = getConfigField(c);
            map.forEach((file, setMap) -> setMap.forEach((section, set) -> set.forEach((e) -> {
                if (Modifier.isStatic(e.field().getModifiers())) {
                    consumer.run(file, section, e);
                }
            })));
        }
    }

    private void forEachClassMemberConfig(Class<?> c, Manager.Consumer consumer) throws Exception {
        getConfigField(c).forEach((file, setMap) -> setMap.forEach((section, set) -> set.forEach((e) -> {
            if (!Modifier.isStatic(e.field.getModifiers())) {
                consumer.run(file, section, e);
            }
        })));
    }

    public void readStaticConfig(String grandFilePath, String grandSectionPath) throws Exception {
        forEachStaticConfig((filePath, sectionPath, fieldElement) -> read(null, grandFilePath + filePath, grandSectionPath + sectionPath, fieldElement));
        clear();
    }

    public void writeStaticConfig(String grandFilePath, String grandSectionPath) throws Exception {
        forEachStaticConfig((filePath, sectionPath, fieldElement) -> write(null, grandFilePath + filePath, grandSectionPath + sectionPath, fieldElement));
        clear();
    }

    public void readStaticConfig() throws Exception {
        readStaticConfig("", "");
    }

    public void writeStaticConfig() throws Exception {
        writeStaticConfig("", "");
    }

    public void readObject(Object obj, String grandFilePath, String grandSectionPath) throws Exception {
        Class<?> c = obj.getClass();
        forEachClassMemberConfig(c, (filePath, sectionPath, fieldElement) -> read(obj, grandFilePath + filePath, grandSectionPath + sectionPath, fieldElement));
        clear();
    }

    public void writeObject(Object obj, String grandFilePath, String grandSectionPath) throws Exception {
        Class<?> c = obj.getClass();
        forEachClassMemberConfig(c, (filePath, sectionPath, fieldElement) -> write(obj, grandFilePath + filePath, grandSectionPath + sectionPath, fieldElement));
        clear();
    }

    public void readObject(Object obj) throws Exception {
        readObject(obj, "", "");
    }

    public void writeObject(Object obj) throws Exception {
        writeObject(obj, "", "");
    }

    /**
     * @param obj          指定对象,若字段为static,则置为null
     * @param filePath     文件路径
     * @param sectionPath  段路径
     * @param fieldElement 字段信息
     */
    protected abstract void read(Object obj, String filePath, String sectionPath, FieldElement fieldElement);

    /**
     * @param obj          指定对象,若字段为static,则置为null
     * @param filePath     文件路径
     * @param sectionPath  段路径
     * @param fieldElement 字段信息
     */
    protected abstract void write(Object obj, String filePath, String sectionPath, FieldElement fieldElement);

    /**
     * 执行可能存在的清理缓存操作
     */
    protected void clear() {
    }

    protected interface Consumer {
        void run(String filePath, String sectionPath, FieldElement fieldElement);
    }

    protected record FieldElement(String key, Field field, ConfigConverter converter) {
    }
}