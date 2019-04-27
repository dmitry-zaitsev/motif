package testcases.E003_scope_cycle;

import motif.core.ScopeCycleError;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static ScopeCycleError error;

    public static void run() {
        assertThat(error.getTestString())
                .isEqualTo("Scope, Scope");
    }
}
