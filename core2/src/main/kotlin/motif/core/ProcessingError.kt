package motif.core

sealed class ProcessingError

class ScopeCycleError(val cycle: ScopeCycle) : ProcessingError() {

    val testString: String by lazy {
        cycle.path.joinToString { it.clazz.type.simpleName }
    }
}

class MissingDependencyError(val missing: MissingDependency) : ProcessingError() {

    val testString: String by lazy {
        missing.sink.testString
    }
}

class UnusedDependencyError(val unused: UnusedDependency) : ProcessingError()

class DependencyCycleError(val cycle: DependencyCycle) : ProcessingError() {

    val testStrings: List<String> by lazy {
        cycle.path.map { it.testString }
    }
}

class UnexposedSourceError(val unexposedSource: UnexposedSource) : ProcessingError() {

    val testString: String by lazy {
        "${unexposedSource.source.testString}->${unexposedSource.sink.testString}"
    }
}
