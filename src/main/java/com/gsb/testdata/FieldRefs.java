package com.gsb.testdata;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Locale;

/** Extracts bean property names from {@link FieldRef} method references. */
final class FieldRefs {

  private FieldRefs() {
  }

  static String propertyName(FieldRef<?, ?> ref) {
    try {
      Method writeReplace = ref.getClass().getDeclaredMethod("writeReplace");
      writeReplace.setAccessible(true);
      SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(ref);
      return propertyName(lambda.getImplMethodName());
    } catch (ReflectiveOperationException e) {
      throw new TestDataException("Unable to resolve field reference: " + ref, e);
    }
  }

  static String propertyName(String methodName) {
    if (methodName.startsWith("set") && methodName.length() > 3) {
      return decapitalize(methodName.substring(3));
    }
    if (methodName.startsWith("get") && methodName.length() > 3) {
      return decapitalize(methodName.substring(3));
    }
    if (methodName.startsWith("is") && methodName.length() > 2) {
      return decapitalize(methodName.substring(2));
    }
    throw new TemplateConfigurationException(
        "Method reference '" + methodName + "' is not a setter/getter; "
            + "use a reference like User::setName or the field name directly");
  }

  private static String decapitalize(String name) {
    if (name.length() > 1 && Character.isUpperCase(name.charAt(0))
        && Character.isUpperCase(name.charAt(1))) {
      return name;
    }
    return name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
  }
}
