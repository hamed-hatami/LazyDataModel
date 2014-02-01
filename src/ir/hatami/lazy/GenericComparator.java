package ir.hatami.lazy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public final class GenericComparator implements Comparator, Serializable {

    private static final int LESSER = -1;
    private static final int EQUAL = 0;
    private static final int GREATER = 1;
    private static final String METHOD_GET_PREFIX = "get";
    private static final String DATATYPE_STRING = "java.lang.String";
    private static final String DATATYPE_DATE = "java.util.Date";
    private static final String DATATYPE_INTEGER = "java.lang.Integer";
    private static final String DATATYPE_LONG = "java.lang.Long";
    private static final String DATATYPE_FLOAT = "java.lang.Float";
    private static final String DATATYPE_DOUBLE = "java.lang.Double";

    private enum CompareMode {EQUAL, LESS_THAN, GREATER_THAN, DEFAULT}

    private String targetMethod;
    private boolean sortAscending;
    private Locale locale;

    public GenericComparator(String sortField, boolean sortAscending, Locale locale) {
        super();
        this.targetMethod = prepareTargetMethod(sortField);
        this.sortAscending = sortAscending;
        this.locale = locale;
    }

    @Override
    public int compare(Object o1, Object o2) {

        int response = LESSER;
        try {
            Object v1 = (null == this.targetMethod) ? o1 : getValue(o1);
            Object v2 = (null == this.targetMethod) ? o2 : getValue(o2);
            CompareMode cm = findCompareMode(v1, v2);

            if (!cm.equals(CompareMode.DEFAULT)) {
                return compareAlternate(cm);
            }

            final String returnType = (null == this.targetMethod)
                    ? o1.getClass().getName() : getMethod(o1).getReturnType().getName();
            response = compareActual(v1, v2, returnType, com.ibm.icu.text.Collator.getInstance(this.locale));
        } catch (Exception nsme) {
            nsme.printStackTrace();
        }
        return response;
    }

    private int compareAlternate(CompareMode cm) {
        int compareState = LESSER;
        switch (cm) {
            case LESS_THAN:
                compareState = LESSER * determinePosition();
                break;
            case GREATER_THAN:
                compareState = GREATER * determinePosition();
                break;
            case EQUAL:
                compareState = EQUAL * determinePosition();
                break;
        }
        return compareState;
    }

    private int compareActual(Object v1, Object v2, String returnType, com.ibm.icu.text.Collator collator) {
        int acutal = LESSER;
        if (returnType.equals(DATATYPE_INTEGER)) {
            acutal = (collator.compare(((Integer) v1), ((Integer) v2)) * determinePosition());
        } else if (returnType.equals(DATATYPE_LONG)) {
            acutal = (collator.compare(((Long) v1), ((Long) v2)) * determinePosition());
        } else if (returnType.equals(DATATYPE_STRING)) {
            acutal = (collator.compare(((String) v1), ((String) v2)) * determinePosition());
        } else if (returnType.equals(DATATYPE_DATE)) {
            acutal = (collator.compare(((Date) v1), ((Date) v2)) * determinePosition());
        } else if (returnType.equals(DATATYPE_FLOAT)) {
            acutal = (collator.compare(((Float) v1), ((Float) v2)) * determinePosition());
        } else if (returnType.equals(DATATYPE_DOUBLE)) {
            acutal = (collator.compare(((Double) v1), ((Double) v2)) * determinePosition());
        }

        return acutal;
    }

    private final static String prepareTargetMethod(String name) {
        StringBuffer fieldName = new StringBuffer(METHOD_GET_PREFIX);
        fieldName.append(name.substring(0, 1).toUpperCase());
        fieldName.append(name.substring(1));
        return fieldName.toString();
    }

    private final Method getMethod(Object obj) throws Exception {
        return obj.getClass().getMethod(targetMethod, null);
    }

    private final static Object invoke(Method method, Object obj) throws Exception {
        return method.invoke(obj, null);
    }

    private Object getValue(Object obj) throws Exception {
        return invoke(getMethod(obj), obj);
    }

    private CompareMode findCompareMode(Object o1, Object o2) {
        CompareMode cm = CompareMode.LESS_THAN;

        if (null != o1 & null != o2) {
            cm = CompareMode.DEFAULT;
        } else if (null == o1 & null != o2) {
            cm = CompareMode.LESS_THAN;
        } else if (null != o1 & null == o2) {
            cm = CompareMode.GREATER_THAN;
        } else if (null == o1 & null == o2) {
            cm = CompareMode.EQUAL;
        }

        return cm;
    }

    private int determinePosition() {
        return sortAscending ? GREATER : LESSER;
    }
}
