package motif.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScopeGraphSmokeTest : BaseTest() {

    @Test
    fun scopeCycle() {
        addClass(
                "test.BarScope",
                """
                    package test;

                    @motif.Scope
                    interface FooScope {
                      BarScope bar();
                    }

                    @motif.Scope
                    interface BarScope {
                      FooScope foo();
                    }
                """.trimIndent())

        val scopeGraph = getScopeGraph()
        val scopeCycle = scopeGraph.cycle

        assertThat(scopeCycle).isNotNull()
        assertThat(scopeCycle!!.path.map { it.qualifiedName })
                .isEqualTo(listOf("test.BarScope", "test.FooScope", "test.BarScope"))
    }
}