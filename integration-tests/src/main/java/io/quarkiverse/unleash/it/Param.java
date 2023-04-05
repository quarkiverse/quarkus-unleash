package io.quarkiverse.unleash.it;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Param {

    public String text;

    public Long value;

    public Boolean enabled;

}
