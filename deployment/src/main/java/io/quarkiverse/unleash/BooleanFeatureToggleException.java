package io.quarkiverse.unleash;

import jakarta.enterprise.inject.Instance;

import org.jboss.jandex.ClassInfo;

public class BooleanFeatureToggleException extends RuntimeException {

    private static final String MESSAGE = "@" + FeatureToggle.class.getName()
            + " is not allowed on a boolean field or method argument, please use the " + Instance.class.getName()
            + "<Boolean> type instead [class=%s]";

    private final ClassInfo classInfo;

    public BooleanFeatureToggleException(ClassInfo classInfo) {
        super(String.format(MESSAGE, classInfo.name()));
        this.classInfo = classInfo;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }
}
