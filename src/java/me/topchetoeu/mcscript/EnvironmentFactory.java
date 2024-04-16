package me.topchetoeu.mcscript;

import java.util.Map;

import me.topchetoeu.jscript.runtime.Context;

public interface EnvironmentFactory {
    Iterable<String> dependancies();

    String name();
    void apply(Context ctx, Map<String, EnvironmentFactory> dependancies);
}
