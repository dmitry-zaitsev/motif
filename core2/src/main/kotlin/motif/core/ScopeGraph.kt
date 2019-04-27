package motif.core

import motif.ast.IrType
import motif.models.ChildMethod
import motif.models.Scope

internal class ScopeGraph private constructor(val scopes: List<Scope>) {

    private val scopeMap: Map<IrType, Scope> = scopes.associateBy { it.clazz.type }
    private val children: Map<Scope, List<Child>> = scopes.associate { scope -> scope to createChildren(scope) }

    val cycle: ScopeCycle? = calculateCycle()

    fun getChildren(scope: Scope): List<Child> {
        return children[scope] ?: throw NullPointerException("Scope not found: ${scope.qualifiedName}")
    }

    private fun createChildren(scope: Scope): List<Child> {
        return scope.childMethods.map { method ->
            val childScope = scopeMap[method.childScopeClass.type]
                    ?: throw IllegalStateException("Scope not found: ${scope.qualifiedName}")
            Child(scope, method, childScope)
        }
    }

    private fun calculateCycle(): ScopeCycle? {
        // Sort for stable tests
        val sortedScopes = scopes.sortedBy { it.qualifiedName }
        val cycle = Cycle.find(sortedScopes) { scope -> getChildren(scope).map { it.scope } } ?: return null
        return ScopeCycle(cycle.path)
    }

    companion object {

        fun create(scopes: List<Scope>): ScopeGraph {
            return ScopeGraph(scopes)
        }
    }
}

class Child(val parent: Scope, val method: ChildMethod, val scope: Scope)

class ScopeCycle(val path: List<Scope>)

