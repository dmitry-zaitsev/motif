package testcases.E005_dependencies_cycle;

import motif.core.DependencyCycleError;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static DependencyCycleError error;

    public static void run() {
        assertThat(error.getTestStrings())
                .containsExactly(
                        "Scope:FactoryMethodSource:String",
                        "Scope:FactoryMethodSink:String",
                        "Scope:FactoryMethodSource:String");
    }
}
