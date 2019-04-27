package motif.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import motif.core.Graph
import motif.models.Scope
import motif.models.Type
import java.util.*
import javax.lang.model.element.Modifier

class Dependencies private constructor(
        val spec: TypeSpec,
        val typeName: TypeName,
        private val methodSpecs: SortedMap<Type, Method>) {

    private val methodList = methodSpecs.values.toList()

    fun getMethodSpec(type: Type): MethodSpec? {
        return methodSpecs[type]?.methodSpec
    }

    fun isEmpty(): Boolean {
        return methodSpecs.isEmpty()
    }

    fun types(): List<Type> {
        return methodSpecs.keys.toList()
    }

    fun getMethods(): List<Method> {
        return methodList
    }

    class Method(val type: Type, val methodSpec: MethodSpec)

    companion object {

        fun create(
                graph: Graph,
                scope: Scope,
                scopeImplTypeName: ClassName): Dependencies {
            val sinks = graph.getDependencies(scope)
            val nameScope = NameScope()
            val typeName = scopeImplTypeName.nestedClass("Dependencies")

            val methods: SortedMap<Type, Method> = sinks
                    .map { it.type }
                    .toSet()
                    .associateWith { type ->
                        val methodSpec = methodSpec(nameScope, type)
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .build()
                        Method(type, methodSpec)
                    }
                    .toSortedMap()

            val typeSpec = TypeSpec.interfaceBuilder(typeName)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethods(methods.values.map { it.methodSpec })
                    .build()

            return Dependencies(typeSpec, typeName, methods)
        }
    }
}