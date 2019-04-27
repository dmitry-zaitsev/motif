package motif.core

import com.google.common.truth.Truth.assertThat
import motif.models.Scope
import org.junit.Test

class DependencyGraphSmokeTest : BaseTest() {

    @Test
    fun empty() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {}
                """.trimIndent())

        val scopeGraph = getScopeGraph()
        val dependencyGraph = DependencyGraph.create(scopeGraph)

        val fooScope = scopeGraph.scopes[0]
        assertThat(dependencyGraph.dependenciesSorted(fooScope))
                .isEmpty()

        val sinks = dependencyGraph.sinksSorted
        val sources = dependencyGraph.sourcesSorted

        assertThat(sinks).isEmpty()

        assertThat(sources.map { it.testString })
                .isEqualTo(listOf("FooScope:ScopeSource:FooScope"))
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

        val scopeGraph = getScopeGraph()
        val dependencyGraph = DependencyGraph.create(scopeGraph)

        val fooScope = scopeGraph.scopes[0]
        assertThat(dependencyGraph.dependenciesSorted(fooScope).map { it.testString })
                .isEqualTo(listOf("FooScope:AccessMethodSink:String"))

        val sinks = dependencyGraph.sinksSorted
        val sources = dependencyGraph.sourcesSorted

        assertThat(sinks.map { it.testString })
                .isEqualTo(listOf("FooScope:AccessMethodSink:String"))

        assertThat(sources.map { it.testString })
                .isEqualTo(listOf("FooScope:ScopeSource:FooScope"))

        assertThat(dependencyGraph.getSources(sinks[0]))
                .isEmpty()

        assertThat(dependencyGraph.getSinks(sources[0]))
                .isEmpty()
    }

    @Test
    fun factoryMethod() {
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

        val scopeGraph = getScopeGraph()
        val dependencyGraph = DependencyGraph.create(scopeGraph)

        val fooScope = scopeGraph.scopes[0]
        assertThat(dependencyGraph.dependenciesSorted(fooScope).map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        val sinks = dependencyGraph.sinksSorted
        val sources = dependencyGraph.sourcesSorted

        assertThat(sinks.map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        assertThat(sources.map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSource:String", "FooScope:ScopeSource:FooScope"))

        assertThat(dependencyGraph.getSources(sinks[0]))
                .isEmpty()

        assertThat(dependencyGraph.getSinks(sources[0]))
                .isEmpty()

        assertThat(dependencyGraph.getSinks(sources[1]))
                .isEmpty()
    }

    @Test
    fun resolvedSink() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {

                      @motif.Objects
                      class Objects {

                        int i() {
                          return 1;
                        }

                        String string(int i) {
                          return "";
                        }
                      }
                    }
                """.trimIndent())

        val scopeGraph = getScopeGraph()
        val dependencyGraph = DependencyGraph.create(scopeGraph)

        val fooScope = scopeGraph.scopes[0]
        assertThat(dependencyGraph.dependenciesSorted(fooScope))
                .isEmpty()

        val sinks = dependencyGraph.sinksSorted
        val sources = dependencyGraph.sourcesSorted

        assertThat(sinks.map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        assertThat(sources.map { it.testString })
                .isEqualTo(listOf(
                        "FooScope:FactoryMethodSource:String",
                        "FooScope:FactoryMethodSource:int",
                        "FooScope:ScopeSource:FooScope"))

        assertThat(dependencyGraph.getSources(sinks[0]).map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSource:int"))

        assertThat(dependencyGraph.getSinks(sources[0]))
                .isEmpty()

        assertThat(dependencyGraph.getSinks(sources[1]).map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        assertThat(dependencyGraph.getSinks(sources[2]))
                .isEmpty()
    }

    @Test
    fun multiScope() {
        addClass(
                "test.FooScope",
                """
                    package test;

                    @motif.Scope
                    interface BarScope {

                      FooScope foo();

                      @motif.Objects
                      class Objects {

                        @motif.Expose
                        int i() {
                          return 1;
                        }
                      }
                    }

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

        val scopeGraph = getScopeGraph()
        val dependencyGraph = DependencyGraph.create(scopeGraph)

        val scopes = scopeGraph.scopes.sortedBy { it.qualifiedName }

        val barScope = scopes[0]
        val fooScope = scopes[1]

        assertThat(dependencyGraph.dependenciesSorted(fooScope).map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        assertThat(dependencyGraph.dependenciesSorted(barScope))
                .isEmpty()

        val sinks = dependencyGraph.sinksSorted
        val sources = dependencyGraph.sourcesSorted

        assertThat(sinks.map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        assertThat(sources.map { it.testString })
                .isEqualTo(listOf(
                        "BarScope:FactoryMethodSource:int",
                        "BarScope:ScopeSource:BarScope",
                        "FooScope:FactoryMethodSource:String",
                        "FooScope:ScopeSource:FooScope"))

        assertThat(dependencyGraph.getSources(sinks[0]).map { it.testString })
                .isEqualTo(listOf("BarScope:FactoryMethodSource:int"))

        assertThat(dependencyGraph.getSinks(sources[0]).map { it.testString })
                .isEqualTo(listOf("FooScope:FactoryMethodSink:int"))

        assertThat(dependencyGraph.getSinks(sources[1]))
                .isEmpty()

        assertThat(dependencyGraph.getSinks(sources[2]))
                .isEmpty()

        assertThat(dependencyGraph.getSinks(sources[3]))
                .isEmpty()
    }

    private fun DependencyGraph.dependenciesSorted(scope: Scope): List<Sink> {
        return getDependencies(scope).sortedBy { it.testString }
    }

    private val DependencyGraph.sinksSorted: List<Sink>
        get() = sinks.sortedBy { it.testString }

    private val DependencyGraph.sourcesSorted: List<Source>
        get() = sources.sortedBy { it.testString }
}