package motif.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import motif.ast.IrClass
import motif.ast.compiler.CompilerClass
import motif.models.Objects
import motif.models.Scope
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ObjectsImpl private constructor(
        val spec: TypeSpec,
        val objects: Objects,
        val objectsName: ClassName,
        val objectsImplName: ClassName) {

    companion object {

        fun create(env: ProcessingEnvironment, scope: Scope, scopeImplTypeName: ClassName): ObjectsImpl? {
            val objects = scope.objects ?: return null
            val objectsClass = objects.clazz as CompilerClass
            val objectsName = objectsClass.typeName
            val objectsImplName = scopeImplTypeName.nestedClass("Objects")

            val typeSpec = TypeSpec.classBuilder(objectsImplName)
            typeSpec.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            if (objectsClass.kind == IrClass.Kind.INTERFACE) {
                typeSpec.addSuperinterface(objectsName)
            } else {
                typeSpec.superclass(objectsName)
            }

            val overriddenMethods = scope.factoryMethods
                    .filter { it.method.isAbstract() }
                    .map {
                        overrideSpec(env, it.method)
                                .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                                .build()
                    }
            typeSpec.addMethods(overriddenMethods)

            return ObjectsImpl(typeSpec.build(), objects, objectsName, objectsImplName)
        }
    }
}