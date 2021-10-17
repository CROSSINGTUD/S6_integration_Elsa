package de.tu_darmstadt.crossing.composable_crypto.core;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ComponentConfiguration {
    private final List<ConfigurationProperty> properties = new ArrayList<>();

    public ComponentConfiguration prop(String property, Object value) {
        String regex = property
                .replace(".", "\\.")
                .replace("?", "[A-Za-z]+")
                .replace("*", "[A-Za-z.]+");
        Predicate<String> predicate = Pattern.compile(regex).asPredicate();
        properties.add(new ConfigurationProperty(predicate, value));
        return this;
    }

    public Object query(String property, Deque<CryptographicComponentBuilder.NamedEdge> parents) {
        if (!parents.isEmpty()) {
            CryptographicComponentBuilder.NamedEdge edge = parents.removeLast();
            Object parentResult = edge.getParent().getConfiguration().query(edge.getLabel() + "." + property, parents);
            parents.addLast(edge);
            if (parentResult != null) {
                return parentResult;
            }
        }
        for (ConfigurationProperty configurationProperty : properties) {
            if (configurationProperty.test(property)) {
                return property;
            }
        }
        return null;
    }

    private static class ConfigurationProperty implements Predicate<String> {
        private final Predicate<String> predicate;
        private final Object value;

        public ConfigurationProperty(Predicate<String> predicate, Object value) {
            this.predicate = predicate;
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public boolean test(String s) {
            return predicate.test(s);
        }
    }
}
