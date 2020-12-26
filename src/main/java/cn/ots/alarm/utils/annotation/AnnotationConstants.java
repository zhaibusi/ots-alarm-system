package cn.ots.alarm.utils.annotation;

import java.util.HashMap;
import java.util.Map;

public class AnnotationConstants {

    public static final String NOT_BLANK = "NOT_BLANK";

    public static final String NOT_SPECIAL_CHAR = "NOT_SPECIAL_CHAR";

    public static final String STR_LENGTH = "STR_LENGTH";

    public static final String NUM_LENGTH = "NUM_LENGTH";

    public static final Map<String, String> SUFFIX_MAP = new HashMap<>();

    static {
        SUFFIX_MAP.put(NOT_BLANK, "不能为空");
        SUFFIX_MAP.put(NOT_SPECIAL_CHAR, "不能含有特殊字符");
        SUFFIX_MAP.put(STR_LENGTH, "长度超出限制");
        SUFFIX_MAP.put(NUM_LENGTH, "大小超出限制");
    }
}
