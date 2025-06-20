package cn.com.wysha.config_Manager.converter;

public class DefaultConverter implements ConfigConverter<Object> {
    @Override
    public String objToString(Object o) {
        return (String) o;
    }

    @Override
    public Object stringToObj(String s) {
        return s;
    }
}
