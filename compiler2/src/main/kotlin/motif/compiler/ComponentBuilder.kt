package motif.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import dagger.BindsInstance
import dagger.Component
import javax.lang.model.element.Modifier

class ComponentBuilder private constructor(
        val spec: TypeSpec,
        val typeName: ClassName,
        val dependenciesMethod: MethodSpec,
        val scopeMethod: MethodSpec,
        val buildMethod: MethodSpec) {

    companion object {

        fun create(
                dependencies: Dependencies,
                scopeTypeName: ClassName,
                componentTypeName: ClassName): ComponentBuilder {
            val typeName = componentTypeName.nestedClass("Builder")
            val typeSpec = TypeSpec.interfaceBuilder(typeName)
            typeSpec.addAnnotation(Component.Builder::class.java)
            typeSpec.addModifiers(Modifier.PUBLIC, Modifier.STATIC)

            val dependenciesMethod = MethodSpec.methodBuilder("dependencies")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(typeName)
                    .addParameter(dependencies.typeName, "dependencies")
                    .build()

            val scopeMethod = MethodSpec.methodBuilder("scope")
                    .addAnnotation(BindsInstance::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(typeName)
                    .addParameter(scopeTypeName, "scope")
                    .build()

            val buildMethod = MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(componentTypeName)
                    .build()

            typeSpec.addMethods(listOf(
                    dependenciesMethod,
                    scopeMethod,
                    buildMethod
            ))

            return ComponentBuilder(typeSpec.build(), typeName, dependenciesMethod, scopeMethod, buildMethod)
        }
    }
}