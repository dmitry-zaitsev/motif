package testcases.E002_nonstandard_dependencies_name;

import motif.core.MissingDependencyError;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static MissingDependencyError error;

    public static void run() {
        assertThat(error.getTestString())
                .isEqualTo("Scope:AccessMethodSink:String");
    }
}
