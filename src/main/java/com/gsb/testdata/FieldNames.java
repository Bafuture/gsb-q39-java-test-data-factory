package com.gsb.testdata;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/** Extracts a field name from a serializable getter/setter method reference. */
final class FieldNames {

    private FieldNames() {
    }

    static String fromLambda(Serializable lambda) {
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda serialized = (SerializedLambda) writeReplace.invoke(lambda);
            return toFieldName(serialized.getImplMethodName());
        } catch (ReflectiveOperationException e) {
            throw new TestDataException("Cannot extract field name from method reference", e);
        }
    }

    static String toFieldName(String methodName) {
        String base = methodName;
        if (base.startsWith("get") && base.length() > 3) {
            base = base.substring(3);
        } else if (base.startsWith("set") && base.length() > 3) {
            base = base.substring(3);
        } else if (base.startsWith("is") && base.length() > 2) {
            base = base.substring(2);
        } else {
            throw new TestDataException("Not a getter/setter reference: " + methodName);
        }
        return Character.toLowerCase(base.charAt(0)) + base.substring(1);
    }
}
