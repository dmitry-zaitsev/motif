package motif.models

import motif.ast.IrClass
import motif.ast.IrMethod

class Dependencies(val clazz: IrClass, val scope: Scope) {

    val methods: List<Method> = clazz.methods
            .map { method ->
                if (method.isVoid()) throw VoidDependenciesMethod(clazz, method)
                if (method.hasParameters()) throw DependencyMethodWithParameters(clazz, method)
                val type = Type.fromReturnType(method)
                Method(this, method, type)
            }

    class Method(val dependencies: Dependencies, val method: IrMethod, val returnType: Type)

    companion object {

        fun fromScope(scope: Scope): Dependencies? {
            val dependenciesClass = scope.clazz.annotatedInnerClass(motif.Dependencies::class) ?: return null
            return Dependencies(dependenciesClass, scope)
        }
    }
}