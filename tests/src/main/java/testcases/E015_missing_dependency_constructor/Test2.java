package testcases.E015_missing_dependency_constructor;

import motif.core.MissingDependencyError;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static MissingDependencyError error;

    public static void run() {
        assertThat(error.getTestString())
                .isEqualTo("Scope:FactoryMethodSink:String");
    }
}
