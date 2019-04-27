package testcases.E006_dependencies_cycle_2;

import motif.core.DependencyCycleError;

import static com.google.common.truth.Truth.assertThat;

// TODO Use different dependency types for clarity.
public class Test2 {

    public static DependencyCycleError error;

    public static void run() {
        assertThat(error.getTestStrings())
                .containsExactly(
                        "Scope:FactoryMethodSource:String",
                        "Scope:FactoryMethodSink:String",
                        "Scope:FactoryMethodSource:String",
                        "Scope:FactoryMethodSink:String",
                        "Scope:FactoryMethodSource:String");
    }
}
