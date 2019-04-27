package motif.core

import java.util.*

class Cycle<T>(val path: List<T>) {

    companion object {

        fun <T> find(items: Iterable<T>, getChildren: (T) -> Iterable<T>): Cycle<T>? {
            return CycleFinder(items, getChildren).find()
        }
    }
}

private class CycleFinder<T>(
        private val items: Iterable<T>,
        private val getChildren: (T) -> Iterable<T>) {
    
    fun find(): Cycle<T>? {
        val cyclePath = calculateCyclePath(Stack(), items) ?: return null
        return Cycle(cyclePath)
    }

    private fun calculateCyclePath(path: Stack<T>, items: Iterable<T>): List<T>? {
        items.forEach { item ->
            calculateCyclePath(path, item)?.let { cycle -> return cycle }
        }

        return null
    }

    private fun calculateCyclePath(path: Stack<T>, item: T): List<T>? {
        val seenIndex = path.indexOf(item)
        if (seenIndex != -1) {
            return path.subList(seenIndex, path.size) + item
        }

        path.push(item)

        val children = getChildren(item)

        calculateCyclePath(path, children)?.let { cycle -> return cycle }

        path.pop()

        return null
    }
}