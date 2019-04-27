package motif.core

import motif.models.Dependencies
import motif.models.FactoryMethod
import motif.models.Scope
import motif.models.Type

internal class DependencyGraph(
        private val sinkToSource: Map<Sink, List<Source>>,
        private val sourceToSink: Map<Source, List<Sink>>,
        private val dependencies: Map<Scope, List<Sink>>,
        private val childDependencies: Map<Child, List<Sink>>,
        val missingDependencies: List<MissingDependency>,
        val unusedDependencies: List<UnusedDependency>,
        val unexposedSources: List<UnexposedSource>,
        val dependencyCycle: DependencyCycle?) {

    val sinks: List<Sink> = sinkToSource.keys.toList()
    val sources: List<Source> = sourceToSink.keys.toList()

    fun getDependencies(scope: Scope): List<Sink> {
        return dependencies[scope] ?: throw NullPointerException("Scope not found: ${scope.qualifiedName}")
    }

    fun getDependencies(child: Child): List<Sink> {
        return childDependencies[child] ?: throw NullPointerException("Child not found: ${child.method.qualifiedName}")
    }

    fun getSinks(source: Source): List<Sink> {
        return sourceToSink[source] ?: emptyList()
    }

    fun getSources(sink: Sink): List<Source> {
        return sinkToSource[sink] ?: emptyList()
    }

    companion object {

        fun create(scopeGraph: ScopeGraph): DependencyGraph {
            return DependencyGraphFactory(scopeGraph).create()
        }

        fun createEmpty(): DependencyGraph {
            return DependencyGraph(
                    mapOf(),
                    mapOf(),
                    mapOf(),
                    mapOf(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    null)
        }
    }
}

private class DependencyGraphFactory(private val scopeGraph: ScopeGraph) {

    private val sinkToSource = mutableMapOf<Sink, MutableList<Source>>()
    private val sourceToSink = mutableMapOf<Source, MutableList<Sink>>()
    private val scopeDependencies = mutableMapOf<Scope, List<Sink>>()
    private val childDependencies = mutableMapOf<Child, List<Sink>>()
    private val nodeToDependencies = mutableMapOf<Node, MutableList<Node>>()
    private val missingDependencies = mutableListOf<MissingDependency>()
    private val unusedDependencies = mutableListOf<UnusedDependency>()
    private val unexposedSources = mutableListOf<UnexposedSource>()

    fun create(): DependencyGraph {
        scopeGraph.scopes.forEach { scope -> getDependencies(scope) }
        return DependencyGraph(
                sinkToSource.toMap(),
                sourceToSink.toMap(),
                scopeDependencies.toMap(),
                childDependencies.toMap(),
                missingDependencies.toList(),
                unusedDependencies.toList(),
                unexposedSources.toList(),
                calculateDependencyCycle())
    }

    private fun calculateDependencyCycle(): DependencyCycle? {
        val cycle = Cycle.find(nodeToDependencies.keys) { node -> nodeToDependencies[node] ?: emptyList() } ?: return null
        return DependencyCycle(cycle.path)
    }

    private fun getDependencies(scope: Scope): List<Sink> {
        return scopeDependencies.computeIfAbsent(scope) {
            val dependencies = computeDependencies(scope)
            scope.dependencies?.restrict(scope, dependencies) ?: dependencies
        }
    }

    private fun Dependencies.restrict(scope: Scope, dependencies: List<Sink>): List<Sink> {
        val requiredTypes: Map<Type, Sink> = dependencies.associateBy { it.type }
        val declaredTypes: Set<Type> = methods.map { it.returnType }.toSet()
        val requiredButNotDeclared: List<Sink> = dependencies.filter { required -> !declaredTypes.contains(required.type) }
        val declaredButNotRequired: List<Dependencies.Method> = methods.filter { declared -> !requiredTypes.contains(declared.returnType) }

        requiredButNotDeclared.forEach {
            missingDependencies.add(MissingDependency(scope, it))
        }

        declaredButNotRequired.forEach {
            unusedDependencies.add(UnusedDependency(it))
        }

        // This loop can be merged with declaredButNotRequired if needed
        return methods.mapNotNull { method -> requiredTypes[method.returnType] }
    }

    private fun computeDependencies(scope: Scope): List<Sink> {
        val scopeNodes = getScopeNodes(scope)
        val scopeSinks: List<Sink> = scopeNodes.mapNotNull { it as? Sink }.apply { populateSinks(this) }
        val scopeSources: List<Source> = scopeNodes.mapNotNull { it as? Source }.apply { populateSources(this) }

        val scopeDependencies: List<Sink> = scopeSinks.satisfy(scopeSources)

        val childDependencies: List<Sink> = scopeGraph.getChildren(scope)
                .flatMap { childDependencies(it) }
                .satisfy(scopeSources) { source, sink ->
                    if (source.isExposed) {
                        true
                    } else {
                        unexposedSources.add(UnexposedSource(source, sink))
                        false
                    }
                }

        return childDependencies + scopeDependencies
    }

    private fun getScopeNodes(scope: Scope): List<Node> {
        return getFactoryMethodNodes(scope) + getAccessMethodNodes(scope) + ScopeSource(scope)
    }

    private fun getAccessMethodNodes(scope: Scope): List<Node> {
        return scope.accessMethods.map { AccessMethodSink(it) }
    }

    private fun getFactoryMethodNodes(scope: Scope): List<Node> {
        return scope.factoryMethods.flatMap { getFactoryMethodNodes(it) }
    }

    private fun getFactoryMethodNodes(factoryMethod: FactoryMethod): List<Node> {
        val sinks = factoryMethod.parameters.map { FactoryMethodSink(it) }
        val source = FactoryMethodSource(factoryMethod)
        sinks.forEach { sink -> getNodeDependencies(source).add(sink) }
        return sinks + source + getSpreadNodes(factoryMethod, source)
    }

    private fun getSpreadNodes(factoryMethod: FactoryMethod, factoryMethodSource: FactoryMethodSource): List<Node> {
        val spread = factoryMethod.spread ?: return emptyList()
        val sources = spread.methods.map { SpreadSource(it) }
        sources.forEach { source -> getNodeDependencies(source).add(factoryMethodSource) }
        return sources
    }

    private fun childDependencies(child: Child): List<Sink> {
        return childDependencies.computeIfAbsent(child) {
            val parameterSources: List<Source> = child.method.parameters
                    .map { parameter -> ChildParameterSource(parameter) }
                    .apply { populateSources(this) }
            val childDependencies: List<Sink> = getDependencies(child.scope)
            childDependencies.satisfy(parameterSources) { source, sink ->
                if (sink.scope == child.scope || source.isExposed) {
                    true
                } else {
                    unexposedSources.add(UnexposedSource(source, sink))
                    false
                }
            }
        }
    }

    private fun List<Sink>.satisfy(sources: List<Source>, filter: (Source, Sink) -> Boolean = { _, _ -> true}): List<Sink> {
        return this.filter { sink ->
            sources.filter { source ->
                source.type == sink.type && filter(source, sink)
            }.onEach { match ->
                getSources(sink).add(match)
                getSinks(match).add(sink)
                getNodeDependencies(sink).add(match)
            }.isEmpty()
        }
    }

    private fun getNodeDependencies(node: Node): MutableList<Node> {
        return nodeToDependencies.computeIfAbsent(node) { mutableListOf() }
    }

    private fun getSources(sink: Sink): MutableList<Source> {
        return sinkToSource.computeIfAbsent(sink) { mutableListOf() }
    }

    private fun getSinks(source: Source): MutableList<Sink> {
        return sourceToSink.computeIfAbsent(source) { mutableListOf() }
    }

    private fun populateSinks(sinks: List<Sink>) {
        sinks.forEach { getSources(it) }
    }

    private fun populateSources(sources: List<Source>) {
        sources.forEach { getSinks(it) }
    }
}

class MissingDependency(val top: Scope, val sink: Sink)

class UnusedDependency(val method: Dependencies.Method)

class DependencyCycle(val path: List<Node>)

class UnexposedSource(val source: Source, val sink: Sink)
