package de.tu_darmstadt.crossing.composable_crypto.core;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class CryptographicComponentBuilder<T extends CryptographicComponent> {
    private ComponentConfiguration configuration;

    public CryptographicComponentBuilder<? extends CryptographicComponent> configure(ComponentConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public T build() {
        return build(new ArrayDeque<>());
    }

    public abstract T build(Deque<NamedEdge> parents);

    public T build(Deque<NamedEdge> parents, String label) {
        parents.addLast(new NamedEdge(label, this));
        T result = build(parents);
        parents.removeLast();
        return result;
    }

    protected ComponentConfiguration getConfiguration() {
        return configuration;
    }

    public static class NamedEdge {
        private final String label;
        private final CryptographicComponentBuilder<? extends CryptographicComponent> parent;

        public NamedEdge(String label, CryptographicComponentBuilder<? extends CryptographicComponent> child) {
            this.label = label;
            this.parent = child;
        }

        public String getLabel() {
            return label;
        }

        public CryptographicComponentBuilder<? extends CryptographicComponent> getParent() {
            return parent;
        }
    }
}
