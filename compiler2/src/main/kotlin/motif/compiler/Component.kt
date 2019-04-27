package motif.compiler

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import motif.core.Graph
import motif.internal.DaggerScope
import motif.models.Scope
import motif.models.Type
import java.util.*
import javax.lang.model.element.Modifier

class Component private constructor(
        val spec: TypeSpec,
        val typeName: ClassName,
        val builder: ComponentBuilder,
        private val methodSpecs: Map<Type, MethodSpec>) {

    fun getMethodSpec(type: Type): MethodSpec {
        return methodSpecs[type]
                ?: throw NullPointerException("Could not find Component method for Type: ${type.qualifiedName}")
    }

    companion object {

        fun create(
                graph: Graph,
                scope: Scope,
                dependencies: Dependencies,
                module: Module,
                childImpls: List<ChildImpl>,
                scopeTypeName: ClassName,
                scopeImplTypeName: ClassName): Component {
            val typeName = scopeImplTypeName.nestedClass("Component")

            val builder = ComponentBuilder.create(dependencies, scopeTypeName, typeName)
            val methodSpecs = componentMethodSpecs(graph, scope, childImpls)

            val typeSpec = TypeSpec.interfaceBuilder(typeName)
            typeSpec.addAnnotation(DaggerScope::class.java)
            typeSpec.addAnnotation(AnnotationSpec.builder(dagger.Component::class.java)
                    .addMember("dependencies", "\$T.class", dependencies.typeName)
                    .addMember("modules", "\$T.class", module.typeName)
                    .build())
            typeSpec.addMethods(methodSpecs.values)
            typeSpec.addType(builder.spec)

            return Component(typeSpec.build(), typeName, builder, methodSpecs)
        }

        private fun componentMethodSpecs(
                graph: Graph,
                scope: Scope,
                children: List<ChildImpl>): SortedMap<Type, MethodSpec> {
            val nameScope = NameScope()
            val requiredTypes = (scope.accessMethods.map { it.returnType } +
                    children.flatMap { childImpl -> graph.getChildDependencies(childImpl.child).map { it.type } })
                    .toSet()
            return requiredTypes.associate { type ->
                val methodSpec = methodSpec(nameScope, type)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                type to methodSpec.build()
            }.toSortedMap()
        }
    }
}