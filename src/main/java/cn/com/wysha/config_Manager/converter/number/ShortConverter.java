package cn.com.wysha.config_Manager.converter.number;

import cn.com.wysha.config_Manager.converter.ConfigConverter;

public class ShortConverter implements ConfigConverter<Short> {
    @Override
    public String objToString(Short obj) {
        return obj.toString();
    }

    @Override
    public Short stringToObj(String s) {
        return Short.parseShort(s);
    }
}
