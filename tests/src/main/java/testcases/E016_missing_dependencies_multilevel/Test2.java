package testcases.E016_missing_dependencies_multilevel;

import motif.core.MissingDependencyError;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static List<MissingDependencyError> errors;

    public static void run() {
        assertThat(errors.stream()
                .map(MissingDependencyError::getTestString)
                .collect(Collectors.toList()))
                .containsExactly(
                        "Grandchild:AccessMethodSink:Integer",
                        "Child:AccessMethodSink:String");
    }
}
