package motif.models

import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType

sealed class ParsingError : RuntimeException() {

    abstract val debugString: String
}

class ScopeMustBeAnInterface(val scopeClass: IrClass) : ParsingError() {

    override val debugString = "Scope must be an interface: ${scopeClass.qualifiedName}"
}

class InvalidScopeMethod(val scope: Scope, val method: IrMethod) : ParsingError() {

    override val debugString = "Scope method is invalid: ${scope.qualifiedName}.${method.name}"
}

class ObjectsFieldFound(val objectClass: IrClass) : ParsingError() {

    override val debugString = "Objects class may not have fields: ${objectClass.qualifiedName}"
}

class ObjectsConstructorFound(val objectClass: IrClass) : ParsingError() {

    override val debugString = "Objects class may not define constructors: ${objectClass.qualifiedName}"
}

class VoidFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError() {

    override val debugString = "Objects methods must be non-void: ${objects.qualifiedName}.${method.name}"
}

class NullableFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError() {

    override val debugString = "Factory method may not be nullable: ${objects.qualifiedName}.${method.name}"
}

class NullableParameter(val owner: IrClass, val method: IrMethod, val parameter: IrParameter) : ParsingError() {

    override val debugString = "Parameter may not be nullable: ${parameter.name} in ${owner.qualifiedName}.${method.name}"
}

class InvalidFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError() {

    override val debugString = "Objects method is invalid: ${objects.qualifiedName}.${method.name}"
}

class TypeNotSpreadable(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError() {

    override val debugString = "Type is not spreadable: ${type.qualifiedName} at ${objects.qualifiedName}.${method.name}"
}

class NoSuitableConstructor(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError() {

    override val debugString = "No suitable constructor found: ${type.qualifiedName} at ${objects.qualifiedName}.${method.name}"
}

class MissingInjectAnnotation(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError() {

    override val debugString = "Multiple constructors found. @Inject annotationn required: ${type.qualifiedName} at ${objects.qualifiedName}.${method.name}"
}

class NotAssignableBindsMethod(val objects: Objects, val method: IrMethod, val returnType: IrType, val parameterType: IrType) : ParsingError() {

    override val debugString = "Invalid binds method: ${objects.qualifiedName}.${method.name}"
}

class VoidDependenciesMethod(val dependenciesClass: IrClass, val method: IrMethod) : ParsingError() {

    override val debugString = "Dependencies method must be non-void: ${dependenciesClass.qualifiedName}.${method.name}"
}

class DependencyMethodWithParameters(val dependenciesClass: IrClass, val method: IrMethod) : ParsingError() {

    override val debugString = "Dependencies method must be parameterless: ${dependenciesClass.qualifiedName}.${method.name}"
}
