package cn.ots.alarm.utils.annotation;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

public class AnnotationUtils {

    public static String valid(Object obj) {
        Class<?> aClass = obj.getClass();
        Field[] fields = aClass.getDeclaredFields();
        try {
            for (Field field : fields) {
                validHandler(field, obj);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void validHandler(Field field, Object obj) throws IllegalAccessException {
        validBase(field, obj);
        validSize(field, obj);
    }

    private static void validSize(Field field, Object obj) throws IllegalAccessException {
        ValidSize annotation = field.getAnnotation(ValidSize.class);
        if (annotation != null) {
            String name = annotation.name();
            field.setAccessible(true);
            Object value = field.get(obj);
            int num = Integer.parseInt(nullObjDefaultStr(value));
            if (num < annotation.min() || num > annotation.max()) {
                throw new RException(10000, StringUtils.join(name, AnnotationConstants.SUFFIX_MAP.get(AnnotationConstants.NUM_LENGTH)));
            }
        }
    }

    public static void validBase(Field field, Object obj) throws IllegalAccessException {
        ValidBase annotation = field.getAnnotation(ValidBase.class);
        if (annotation != null) {
            String name = annotation.name();
            field.setAccessible(true);
            Object value = field.get(obj);
            //长度校验
            int max = annotation.maxLength();
            int min = annotation.minLength();
            int length = StringUtils.length(nullObjDefaultStr(value));
            if (length < min || length > max) {
                throw new RException(10000, StringUtils.join(name, AnnotationConstants.SUFFIX_MAP.get(AnnotationConstants.STR_LENGTH)));
            }
            //基础校验
            String[] valid = annotation.valid();
            for (String v : valid) {
                if (StringUtils.equals(v, AnnotationConstants.NOT_BLANK)) {
                    if (StringUtils.isBlank(nullObjDefaultStr(value))) {
                        throw new RException(10000, StringUtils.join(name, AnnotationConstants.SUFFIX_MAP.get(v)));
                    }
                }
                if (StringUtils.equals(v, AnnotationConstants.NOT_SPECIAL_CHAR)) {
                    if (CommonUtils.isSpecialChar(nullObjDefaultStr(value))) {
                        throw new RException(10000, StringUtils.join(name, AnnotationConstants.SUFFIX_MAP.get(v)));
                    }
                }
            }
        }
    }

    public static String nullObjDefaultStr(Object obj) {
        return ObjectUtils.defaultIfNull(obj, StringUtils.EMPTY).toString();
    }

}
