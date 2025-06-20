package cn.com.wysha.config_Manager.converter.number;

import cn.com.wysha.config_Manager.converter.ConfigConverter;

public class IntegerConverter implements ConfigConverter<Integer> {
    @Override
    public String objToString(Integer obj) {
        return obj.toString();
    }

    @Override
    public Integer stringToObj(String s) {
        return Integer.parseInt(s);
    }
}
