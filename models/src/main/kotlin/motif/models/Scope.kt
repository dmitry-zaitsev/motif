package motif.models

import motif.ast.IrClass
import motif.ast.IrType

class Scope private constructor(val clazz: IrClass) {

    val qualifiedName: String by lazy { clazz.qualifiedName }

    val objects: Objects? = Objects.fromScope(this)

    private val scopeMethods = clazz.methods.map { method -> ScopeMethod.fromScopeMethod(this, method) }

    val accessMethods: List<AccessMethod> = scopeMethods.mapNotNull { method -> method as? AccessMethod }

    val childMethods: List<ChildMethod> = scopeMethods.mapNotNull { method -> method as? ChildMethod }

    val factoryMethods: List<FactoryMethod> = objects?.factoryMethods ?: emptyList()

    val dependencies: Dependencies? = Dependencies.fromScope(this)

    companion object {

        fun fromClass(clazz: IrClass): Scope {
            if (clazz.kind != IrClass.Kind.INTERFACE) throw ScopeMustBeAnInterface(clazz)

            return Scope(clazz)
        }

        fun fromClasses(scopeClasses: List<IrClass>): List<Scope> {
            return ScopeFactory(scopeClasses).create()
        }
    }
}

private class ScopeFactory(
        private val initialScopeClasses: List<IrClass>) {

    private val scopeMap: MutableMap<IrType, Scope> = mutableMapOf()
    private val visited: MutableSet<IrType> = mutableSetOf()

    fun create(): List<Scope> {
        initialScopeClasses.forEach(this::visit)
        return scopeMap.values.toList()
    }

    private fun visit(scopeClass: IrClass) {
        val scopeType = scopeClass.type

        if (visited.contains(scopeType)) return
        visited.add(scopeType)

        scopeMap.computeIfAbsent(scopeType) {
            val scope = Scope.fromClass(scopeClass)
            scope.childMethods.forEach { childMethod ->
                visit(childMethod.childScopeClass)
            }
            scope
        }
    }
}