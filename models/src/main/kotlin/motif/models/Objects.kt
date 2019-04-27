package motif.models

import motif.ast.IrClass

class Objects private constructor(
        val clazz: IrClass,
        val scope: Scope) {

    val qualifiedName: String by lazy { clazz.qualifiedName }

    val factoryMethods = clazz.methods
            .map { method -> FactoryMethod.fromObjectsMethod(this, method) }

    companion object {

        fun fromScope(scope: Scope): Objects? {
            val objectsClass = scope.clazz.annotatedInnerClass(motif.Objects::class) ?: return null

            if (objectsClass.fields.any { !it.isStatic() }) throw ObjectsFieldFound(objectsClass)
            if (objectsClass.hasNonDefaultConstructor()) throw ObjectsConstructorFound(objectsClass)

            return Objects(objectsClass, scope)
        }
    }
}