package cn.com.wysha.config_Manager.converter;

public interface ConfigConverter<T> {
    String objToString(T obj);
    T stringToObj(String s);
}
