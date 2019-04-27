package motif.models

import motif.Expose
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter

sealed class ScopeMethod {

    companion object {

        fun fromScopeMethod(scope: Scope, method: IrMethod): ScopeMethod {
            val returnClass: IrClass? = method.returnType.resolveClass()
            if (returnClass != null && returnClass.hasAnnotation(motif.Scope::class)) {
                return ChildMethod(method, scope, returnClass)
            }

            if (!method.hasParameters() && !method.isVoid()) {
                return AccessMethod(method, scope)
            }

            throw InvalidScopeMethod(scope, method)
        }
    }
}

class AccessMethod(val method: IrMethod, val scope: Scope) : ScopeMethod() {

    val qualifiedName: String by lazy { "${scope.qualifiedName}.${method.name}" }

    val returnType = Type.fromReturnType(method)
}

class ChildMethod(
        val method: IrMethod,
        val scope: Scope,
        val childScopeClass: IrClass) : ScopeMethod() {

    val qualifiedName: String by lazy { "${scope.qualifiedName}.${method.name}" }

    val parameters: List<Parameter> = method.parameters.map { Parameter(this, it) }

    class Parameter(val method: ChildMethod, val parameter: IrParameter) {

        val type = Type.fromParameter(parameter)
        val isExposed = parameter.hasAnnotation(Expose::class)
    }
}