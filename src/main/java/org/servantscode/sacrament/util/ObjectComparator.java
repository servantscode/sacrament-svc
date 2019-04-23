package org.servantscode.sacrament.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectComparator {
    public static Set<String> getFieldDifferences(Object o1, Object o2) {
        return Arrays.stream(o1.getClass().getMethods())
                .filter(method -> method.getName().startsWith("get") || method.getName().startsWith("is"))
                .filter(method -> {
                    try {
                        Object existing = method.invoke(o1);
                        return existing == null?
                                method.invoke(o2) == null:
                                existing.equals(method.invoke(o2));
                    } catch (Exception e) {
                        throw new RuntimeException("Could not invoke method: " + method.getName(), e);
                    }
                })
                .map(Method::getName).collect(Collectors.toSet());
    }
}
