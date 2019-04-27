package testcases.E001_missing_dependencies;

import motif.core.MissingDependencyError;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static MissingDependencyError error;

    public static void run() {
        assertThat(error.getTestString())
                .isEqualTo("Scope:AccessMethodSink:String");
    }
}
