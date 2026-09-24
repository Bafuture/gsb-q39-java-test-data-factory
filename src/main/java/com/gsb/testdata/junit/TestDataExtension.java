package com.gsb.testdata.junit;

import com.gsb.testdata.Template;
import com.gsb.testdata.TestData;
import com.gsb.testdata.TestDataException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 extension injecting generated objects into test method parameters
 * annotated with {@link Fixture}.
 *
 * <pre>{@code
 * @ExtendWith(TestDataExtension.class)
 * class UserTest {
 *   static Template<User> users = TestData.template(User.class, 42L).build();
 *
 *   @Test
 *   void works(@Fixture("users") User user) { ... }
 * }
 * }</pre>
 */
public class TestDataExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(Fixture.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) {
    Parameter parameter = parameterContext.getParameter();
    Fixture fixture = parameter.getAnnotation(Fixture.class);
    Template<?> template = fixture.value().isEmpty()
        ? TestData.template(parameter.getType()).build()
        : lookupTemplate(extensionContext.getRequiredTestInstance(), fixture.value());
    if (fixture.seed() != Long.MIN_VALUE) {
      template = template.withSeed(fixture.seed());
    }
    try {
      return template.create();
    } catch (TestDataException e) {
      throw new ParameterResolutionException(
          "Failed to generate fixture for parameter " + parameter, e);
    }
  }

  private static Template<?> lookupTemplate(Object testInstance, String name) {
    Class<?> testClass = testInstance.getClass();
    for (Class<?> current = testClass; current != null; current = current.getSuperclass()) {
      try {
        Field field = current.getDeclaredField(name);
        field.setAccessible(true);
        Object value = Modifier.isStatic(field.getModifiers())
            ? field.get(null)
            : field.get(testInstance);
        return asTemplate(value, name, testClass);
      } catch (NoSuchFieldException ignored) {
        // keep walking up the hierarchy
      } catch (IllegalAccessException e) {
        throw new ParameterResolutionException(
            "Cannot access template field '" + name + "'", e);
      }
    }
    for (Class<?> current = testClass; current != null; current = current.getSuperclass()) {
      try {
        Method method = current.getDeclaredMethod(name);
        method.setAccessible(true);
        Object value = Modifier.isStatic(method.getModifiers())
            ? method.invoke(null)
            : method.invoke(testInstance);
        return asTemplate(value, name, testClass);
      } catch (NoSuchMethodException ignored) {
        // keep walking up the hierarchy
      } catch (ReflectiveOperationException e) {
        throw new ParameterResolutionException(
            "Cannot invoke template method '" + name + "'", e);
      }
    }
    throw new ParameterResolutionException(
        "No template field or no-arg method named '" + name + "' found on "
            + testClass.getName());
  }

  private static Template<?> asTemplate(Object value, String name, Class<?> testClass) {
    if (value instanceof Template<?> template) {
      return template;
    }
    throw new ParameterResolutionException(
        "'" + name + "' on " + testClass.getName() + " is not a Template: " + value);
  }
}
