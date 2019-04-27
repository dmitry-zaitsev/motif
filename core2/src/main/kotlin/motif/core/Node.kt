package motif.core

import motif.models.*

sealed class Node {

    abstract val scope: Scope
    abstract val type: Type

    val testString: String by lazy {
        "${scope.clazz.type.simpleName}:${this::class.java.simpleName}:${type.type.simpleName}"
    }
}

sealed class Source : Node() {

    abstract val isExposed: Boolean
}

sealed class Sink : Node()

class FactoryMethodSource(val factoryMethod: FactoryMethod) : Source() {

    override val scope = factoryMethod.objects.scope
    override val type = factoryMethod.returnType.type
    override val isExposed = factoryMethod.isExposed
}

class ScopeSource(override val scope: Scope) : Source() {

    override val type = Type(scope.clazz.type, null)
    override val isExposed = false
}

class SpreadSource(val spreadMethod: Spread.Method) : Source() {

    override val scope = spreadMethod.spread.factoryMethod.objects.scope
    override val type = spreadMethod.returnType
    override val isExposed = spreadMethod.spread.factoryMethod.isExposed
}

class ChildParameterSource(val parameter: ChildMethod.Parameter) : Source() {

    override val scope = parameter.method.scope
    override val type = parameter.type
    override val isExposed = parameter.isExposed
}

class FactoryMethodSink(val parameter: FactoryMethod.Parameter) : Sink() {

    override val scope = parameter.factoryMethod.objects.scope
    override val type = parameter.type
}

class AccessMethodSink(val accessMethod: AccessMethod) : Sink() {

    override val scope = accessMethod.scope
    override val type = accessMethod.returnType
}