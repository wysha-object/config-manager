package cn.com.wysha.config_Manager.converter.number;

import cn.com.wysha.config_Manager.converter.ConfigConverter;

public class LongConverter implements ConfigConverter<Long> {
    @Override
    public String objToString(Long obj) {
        return obj.toString();
    }

    @Override
    public Long stringToObj(String s) {
        return Long.parseLong(s);
    }
}
