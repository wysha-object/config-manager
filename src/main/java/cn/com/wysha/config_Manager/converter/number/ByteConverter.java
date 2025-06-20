package cn.com.wysha.config_Manager.converter.number;

import cn.com.wysha.config_Manager.converter.ConfigConverter;

public class ByteConverter implements ConfigConverter<Byte> {
    @Override
    public String objToString(Byte obj) {
        return obj.toString();
    }

    @Override
    public Byte stringToObj(String s) {
        return Byte.parseByte(s);
    }
}
