package io.quarkiverse.unleash;

import org.jboss.jandex.ClassInfo;

public class EmptyAnnotationNameException extends RuntimeException {

    private static final String MESSAGE = "@" + FeatureToggle.class.getName() + " and @" + FeatureVariant.class.getName()
            + " annotations must have a non empty name attribute [class=%s]";

    private final ClassInfo classInfo;

    public EmptyAnnotationNameException(ClassInfo classInfo) {
        super(String.format(MESSAGE, classInfo.name()));
        this.classInfo = classInfo;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }
}
