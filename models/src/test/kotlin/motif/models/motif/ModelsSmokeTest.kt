package motif.models.motif

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ModelsSmokeTest : BaseTest() {

    @Test
    fun emptyScope() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {}
                """.trimIndent())

        val scopes = getScopes()

        assertThat(scopes).hasSize(1)

        val scope = scopes[0]

        assertThat(scope.qualifiedName).isEqualTo("test.FooScope")
        assertThat(scope.factoryMethods).isEmpty()
    }

    @Test
    fun objects() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Objects
                      class Objects {}
                    }
                """.trimIndent())

        val scope = getScopes()[0]

        assertThat(scope.factoryMethods).isEmpty()
    }

    @Test
    fun basicFactoryMethod() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Objects
                      class Objects {
                        String string(int i) {
                          return "";
                        }
                      }
                    }
                """.trimIndent())

        val factoryMethods = getScopes()[0].factoryMethods

        assertThat(factoryMethods.map { it.qualifiedName })
                .isEqualTo(listOf("test.FooScope.Objects.string"))

        val factoryMethod = factoryMethods[0]

        assertThat(factoryMethod.returnType.qualifiedName)
                .isEqualTo("java.lang.String")

        assertThat(factoryMethod.parameters.map { it.qualifiedName })
                .isEqualTo(listOf("int"))
    }

    @Test
    fun constructorFactoryMethod() {
        addClass("test.Foo",
                """
                    package test;

                    class Foo {
                      Foo(int i) {}
                    }
                """.trimIndent())
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Objects
                      abstract class Objects {
                        abstract Foo foo();
                      }
                    }
                """.trimIndent())

        val factoryMethod = getScopes()[0].factoryMethods[0]

        assertThat(factoryMethod.returnType.qualifiedName)
                .isEqualTo("test.Foo")

        assertThat(factoryMethod.parameters.map { it.qualifiedName })
                .isEqualTo(listOf("int"))
    }

    @Test
    fun bindsFactoryMethod() {
        addClass("test.Foo",
                """
                    package test;

                    class Foo {}
                """.trimIndent())

        addClass("test.Bar",
                """
                    package test;

                    class Bar extends Foo {}
                """.trimIndent())
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Objects
                      abstract class Objects {
                        abstract Foo bar(Bar bar);
                      }
                    }
                """.trimIndent())

        val factoryMethod = getScopes()[0].factoryMethods[0]

        assertThat(factoryMethod.returnType.qualifiedName)
                .isEqualTo("test.Foo")

        assertThat(factoryMethod.parameters.map { it.qualifiedName })
                .isEqualTo(listOf("test.Bar"))
    }

    @Test
    fun spread() {
        addClass("test.Foo",
                """
                    package test;

                    public class Foo {
                      Foo(int i) {}

                      public String string() {
                        return "";
                      }
                    }
                """.trimIndent())
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Objects
                      abstract class Objects {

                        @motif.Spread
                        abstract Foo foo();
                      }
                    }
                """.trimIndent())

        val factoryMethod = getScopes()[0].factoryMethods[0]

        assertThat(factoryMethod.spread).isNotNull()

        val spread = factoryMethod.spread!!

        assertThat(spread.clazz.qualifiedName).isEqualTo("test.Foo")

        assertThat(spread.methods.map { it.qualifiedName })
                .isEqualTo(listOf("test.Foo.string"))
    }

    @Test
    fun accessMethod() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {
                      String string();
                    }
                """.trimIndent())

        val scope = getScopes()[0]

        assertThat(scope.childMethods).isEmpty()
        assertThat(scope.accessMethods.map { it.qualifiedName })
                .isEqualTo(listOf("test.FooScope.string"))

        val accessMethod = scope.accessMethods[0]
        assertThat(accessMethod.returnType.qualifiedName).isEqualTo("java.lang.String")
    }

    @Test
    fun childMethod() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {
                      BarScope bar(String string);
                    }
                """.trimIndent())

        addClass(
                "test.BarScope",
                """
                    package test;

                    @motif.Scope
                    interface BarScope {}
                """.trimIndent())

        // getScopes is alphabetically sorted so FooScope is last
        val fooScope = getScopes()[1]

        assertThat(fooScope.childMethods.map { it.qualifiedName })
                .isEqualTo(listOf("test.FooScope.bar"))

        val childMethod = fooScope.childMethods[0]
        assertThat(childMethod.childScopeClass.qualifiedName).isEqualTo("test.BarScope")

        assertThat(childMethod.parameters.map { it.type.qualifiedName })
                .isEqualTo(listOf("java.lang.String"))
    }

    @Test
    fun explicitDependencies() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Dependencies
                      interface Dependencies {
                        String string();
                      }
                    }
                """.trimIndent())

        val scope = getScopes()[0]

        assertThat(scope.dependencies).isNotNull()

        val explicitDependencies = scope.dependencies!!
        assertThat(explicitDependencies.methods.map { it.returnType.qualifiedName })
                .isEqualTo(listOf("java.lang.String"))
    }
}