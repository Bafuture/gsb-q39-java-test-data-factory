package com.gsb.testdata.junit;

import com.gsb.testdata.DataFactory;
import com.gsb.testdata.Template;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JUnit 5 extension injecting generated objects into test method parameters:
 *
 * <pre>{@code
 * @ExtendWith(TestDataExtension.class)
 * class UserTest {
 *     @Test
 *     void works(@Generated User user) { ... }
 * }
 * }</pre>
 *
 * A template registered via {@link #registerTemplate(Class, Template)} takes
 * precedence; otherwise a default template is used. {@link Generated#seed()}
 * makes the injected value reproducible.
 */
public final class TestDataExtension implements ParameterResolver {

    private static final Map<Class<?>, Template<?>> REGISTERED = new ConcurrentHashMap<>();

    public static <T> void registerTemplate(Class<T> type, Template<T> template) {
        REGISTERED.put(type, template);
    }

    public static void clearTemplates() {
        REGISTERED.clear();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.isAnnotated(Generated.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        Generated annotation = parameterContext.getParameter().getAnnotation(Generated.class);
        boolean hasSeed = annotation.seed() != Long.MIN_VALUE;
        if (!hasSeed) {
            Template<?> registered = REGISTERED.get(type);
            if (registered != null) {
                return registered.create();
            }
        }
        var builder = DataFactory.template(type);
        if (hasSeed) {
            builder.seed(annotation.seed());
        }
        return builder.build().create();
    }
}
