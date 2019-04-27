package motif.models

import motif.ast.IrClass
import motif.ast.IrMethod

class Spread(val clazz: IrClass, val factoryMethod: FactoryMethod) {

    val sourceType: Type by lazy { factoryMethod.returnType.type }
    val qualifiedName: String by lazy { clazz.qualifiedName }

    val methods: List<Method> = clazz.methods
            .filter { method -> isSpreadMethod(method) }
            .map { method -> Method(method, this, Type.fromReturnType(method)) }

    class Method(val method: IrMethod, val spread: Spread, val returnType: Type) {

        val name = method.name
        val sourceType: Type by lazy { spread.sourceType }
        val qualifiedName: String by lazy { "${spread.qualifiedName}.${method.name}" }
    }

    companion object {

        private fun isSpreadMethod(method: IrMethod): Boolean {
            return !method.isVoid() && method.isPublic() && !method.hasParameters()
        }
    }
}