package cn.com.wysha.config_Manager.annotations;

import cn.com.wysha.config_Manager.converter.ConfigConverter;
import cn.com.wysha.config_Manager.converter.DefaultConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField {
    String filePath() default "";
    String sectionPath() default "";
    String key() default "";
    Class<? extends ConfigConverter<?>> converter() default DefaultConverter.class;
}
