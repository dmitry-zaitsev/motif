package motif.models

import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType

data class Type(val type: IrType, val qualifier: IrAnnotation?) : Comparable<Type> {

    val qualifiedName: String by lazy {
        val qualifierString = qualifier?.let { "$it " } ?: ""
        "$qualifierString${type.qualifiedName}"
    }

    override fun compareTo(other: Type): Int {
        return compareKey.compareTo(other.compareKey)
    }

    private val compareKey: String by lazy {
        val qualifierString = qualifier?.let { "$it " } ?: ""
        "${type.qualifiedName}$qualifierString"
    }

    companion object {

        fun fromParameter(parameter: IrParameter): Type {
            return Type(parameter.type, parameter.getQualifier())
        }

        fun fromReturnType(method: IrMethod): Type {
            return Type(method.returnType, method.getQualifier())
        }
    }
}