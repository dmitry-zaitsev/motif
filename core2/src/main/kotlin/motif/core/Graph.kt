package motif.core

import motif.models.Scope

class Graph private constructor(
        private val scopeGraph: ScopeGraph,
        private val dependencyGraph: DependencyGraph) {

    val processingErrors: List<ProcessingError> by lazy {
        listOfNotNull(scopeGraph.cycle).map { ScopeCycleError(it) } +
                dependencyGraph.missingDependencies.map { MissingDependencyError(it) } +
                dependencyGraph.unusedDependencies.map { UnusedDependencyError(it) } +
                listOfNotNull(dependencyGraph.dependencyCycle).map { DependencyCycleError(it) } +
                dependencyGraph.unexposedSources.map { UnexposedSourceError(it) }
    }

    val scopes = scopeGraph.scopes

    fun getChildren(scope: Scope): List<Child> {
        return scopeGraph.getChildren(scope)
    }

    fun getDependencies(scope: Scope): List<Sink> {
        return dependencyGraph.getDependencies(scope)
    }

    fun getChildDependencies(child: Child): List<Sink> {
        return dependencyGraph.getDependencies(child)
    }

    companion object {

        fun create(scopes: List<Scope>): Graph {
            val scopeGraph = ScopeGraph.create(scopes)
            val dependencyGraph = if (scopeGraph.cycle == null) {
                DependencyGraph.create(scopeGraph)
            } else {
                DependencyGraph.createEmpty()
            }
            return Graph(scopeGraph, dependencyGraph)
        }
    }
}