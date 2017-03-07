package util

import java.util.LinkedList

fun <T : Any, X : Any> Iterable<T>.mapReduce(block: (T, (X) -> Unit) -> Unit): Iterable<X> {
	val result = LinkedList<X>()

	this.forEach {
		block(it, {result.add(it)})
	}

	return result
}