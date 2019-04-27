package testcases.E004_scope_cycle_2;

import motif.core.ScopeCycleError;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static ScopeCycleError error;

    public static void run() {
        assertThat(error.getTestString())
                .isEqualTo("Child, Scope, Child");
    }
}
