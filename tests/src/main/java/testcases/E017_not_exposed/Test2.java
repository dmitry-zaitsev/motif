package testcases.E017_not_exposed;

import motif.core.UnexposedSourceError;

import static com.google.common.truth.Truth.assertThat;

public class Test2 {

    public static UnexposedSourceError error;

    public static void run() {
        assertThat(error.getTestString())
                .isEqualTo("Scope:FactoryMethodSource:String->Child:AccessMethodSink:String");
    }
}
