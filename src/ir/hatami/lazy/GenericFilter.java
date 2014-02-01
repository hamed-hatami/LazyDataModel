package ir.hatami.lazy;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class GenericFilter implements Serializable {

    private static final String METHOD_GET_PREFIX = "get";
    private String targetMethod;
    private String value;


    public GenericFilter(String filterField, String value) {
        super();
        this.targetMethod = prepareTargetMethod(filterField);
        this.value = LangUtils.getEnglishNumber(value);
    }

    public boolean filtering(Object object) throws Exception {
        Object v = this.getValue(object);
        return LangUtils.getEnglishNumber((String) v).contains(value);
    }

    private final static String prepareTargetMethod(String name) {
        StringBuffer fieldName = new StringBuffer(METHOD_GET_PREFIX);
        fieldName.append(name.substring(0, 1).toUpperCase());
        fieldName.append(name.substring(1));
        return fieldName.toString();
    }

    private final Method getMethod(Object obj) throws NoSuchMethodException {
        return obj.getClass().getMethod(targetMethod, null);
    }

    private final static Object invoke(Method method, Object obj) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, null);
    }

    private Object getValue(Object obj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return invoke(getMethod(obj), obj);
    }

}
