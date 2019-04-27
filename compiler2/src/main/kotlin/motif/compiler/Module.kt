package motif.compiler

import com.squareup.javapoet.*
import motif.models.*
import javax.lang.model.element.Modifier

class Module private constructor(
        val spec: TypeSpec,
        val typeName: ClassName) {

    companion object {

        fun create(
                scope: Scope,
                objectsImpl: ObjectsImpl?,
                scopeImplTypeName: ClassName): Module {
            val typeName = scopeImplTypeName.nestedClass("Module")
            val typeSpec = TypeSpec.classBuilder(typeName)
            typeSpec.addAnnotation(dagger.Module::class.java)
            typeSpec.addModifiers(Modifier.STATIC, Modifier.ABSTRACT)

            if (objectsImpl == null) {
                return Module(typeSpec.build(), typeName)
            }

            val objectsField = objectsField(objectsImpl)

            typeSpec.addField(objectsField)

            val nameScope = NameScope()

            val methodSpecs = objectsImpl.objects.factoryMethods
                    .flatMap { factoryMethod -> methodSpecs(nameScope, factoryMethod, objectsField) }

            typeSpec.addMethods(methodSpecs)

            return Module(typeSpec.build(), typeName)
        }

        private fun methodSpecs(
                nameScope: NameScope,
                factoryMethod: FactoryMethod,
                objectsField: FieldSpec): List<MethodSpec> {
            val methodSpec = methodSpec(nameScope, factoryMethod, objectsField)
            val spreadMethodSpecs = spreadMethodSpecs(nameScope, factoryMethod)
            return spreadMethodSpecs + methodSpec
        }

        private fun methodSpec(
                nameScope: NameScope,
                factoryMethod: FactoryMethod,
                objectsField: FieldSpec): MethodSpec {
            val methodSpec = providerMethod(nameScope, factoryMethod.returnType.type, factoryMethod.isCached)
            val parameterNameScope = NameScope()
            val parameterSpecs = factoryMethod.parameters
                    .map { parameter -> parameterSpec(parameterNameScope, parameter.type) }
            methodSpec.addParameters(parameterSpecs)

            val returnStatement = returnStatement(factoryMethod, parameterSpecs, objectsField)
            methodSpec.addStatement(returnStatement)

            return methodSpec.build()
        }

        private fun returnStatement(
                factoryMethod: FactoryMethod,
                parameterSpecs: List<ParameterSpec>,
                objectsField: FieldSpec): CodeBlock {
            val callParams: String = parameterSpecs.joinToString(", ") { "\$N" }
            val methodName = factoryMethod.name
            val parameterSpecArray = parameterSpecs.toTypedArray()
            val returnType = factoryMethod.returnType.type
            return when (factoryMethod) {
                is BasicFactoryMethod -> {
                    if (factoryMethod.isStatic) {
                        CodeBlock.of("return \$T.\$N($callParams)", objectsField.type, methodName, *parameterSpecArray)
                    } else {
                        CodeBlock.of("return \$N.\$N($callParams)", objectsField, methodName, *parameterSpecArray)
                    }
                }
                is BindsFactoryMethod -> CodeBlock.of("return $callParams", *parameterSpecArray)
                is ConstructorFactoryMethod -> CodeBlock.of("return new \$T($callParams)", returnType.typeName, *parameterSpecArray)
            }
        }

        private fun spreadMethodSpecs(nameScope: NameScope, factoryMethod: FactoryMethod): List<MethodSpec> {
            return factoryMethod.spread?.let { spread ->
                spread.methods.map { spreadMethod -> spreadMethodSpec(nameScope, spreadMethod) }
            } ?: emptyList()
        }

        private fun spreadMethodSpec(nameScope: NameScope, spreadMethod: Spread.Method): MethodSpec {
            val returnType = spreadMethod.returnType
            val parameterSpec = parameterSpec(spreadMethod.sourceType, "source")
            val methodSpec = providerMethod(nameScope, returnType, spreadMethod.spread.factoryMethod.isCached)
            methodSpec.addParameter(parameterSpec)
            methodSpec.addStatement("return \$N.\$N()", parameterSpec, spreadMethod.name)
            return methodSpec.build()
        }

        private fun providerMethod(
                nameScope: NameScope,
                type: Type,
                isCached: Boolean): MethodSpec.Builder {
            return methodSpec(nameScope, type)
                    .addAnnotation(dagger.Provides::class.java)
                    .addModifiers(Modifier.STATIC)
                    .apply { if (isCached) addAnnotation(motif.internal.DaggerScope::class.java) }
        }

        private fun objectsField(objectsImpl: ObjectsImpl): FieldSpec {
            return FieldSpec.builder(
                    objectsImpl.objectsName,
                    "objects",
                    Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new \$T()", objectsImpl.objectsImplName)
                    .build()
        }
    }
}