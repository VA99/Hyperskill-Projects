package search

import java.util.Scanner
import java.io.File

fun indicesOf(word: String, info: List<String>): MutableList<Int> {
    val indices = mutableListOf<Int>()
    for (index in info.indices) if (info[index].contains(word)) indices += index
    return indices
}

fun main(args: Array<String>) {
    val file = File(args[1])
    val info = file.readLines()

    val people = mutableMapOf<String, MutableList<Int>>()
    for (query in info) {
        val words = query.split(" ")
        for (word in words)
            people[word.toLowerCase()] = indicesOf(word, info)
    }

    val sEngine = SearchEngine(info, people)

    val message = "=== Menu ===\n1.Find a person\n2.Print all people\n0.Exit"
    val scanner = Scanner(System.`in`)
    do {
        println(message)
        val option = scanner.nextInt()
        when (option) {
            1 -> sEngine.searchInfo()
            2 -> sEngine.printData()
            else -> if (option == 0) println("\nBye!") else println("\nIncorrect option! Try again.")
        }
    } while (option != 0)
}

class Strategy(query: String, private val people: MutableMap<String, MutableList<Int>>) {
    private val queries = query.split(" ")
    private val indices = mutableListOf<Int>()

    private fun getIndices(word: String, doIf: (String) -> Boolean): MutableList<Int> {
        return if (people.containsKey(word) && doIf(word)) people[word]!!
        else mutableListOf()
    }

    fun all(): MutableList<Int> {
        indices += getIndices(queries.first()) { true }
        for (i in 1..queries.lastIndex)
            getIndices(queries[i]) { indices.containsAll(people[it]!!) }
        return indices
    }

    fun any(): MutableList<Int> {
        for (word in queries) indices += getIndices(word) { true }
        return indices
    }

    fun none(): MutableList<Int> {
        indices += any()

        val newIndices1 = mutableListOf<Int>()
        for ((_, value) in people) if (!indices.containsAll(value)) newIndices1 += value

        val newIndices2 = mutableListOf<Int>()
        for (i in newIndices1) if (!newIndices2.contains(i)) newIndices2 += i
        return newIndices2
    }
}

class SearchEngine(private val info: List<String>, private val people: MutableMap<String, MutableList<Int>>) {
    fun searchInfo() {
        val scanner = Scanner(System.`in`)

        println("\nSelect a matching strategy: ALL, ANY, NONE")
        val st = scanner.nextLine()

        println("\nEnter a name or email to search all suitable people:")
        val query = scanner.nextLine().toLowerCase()

        val strategy = Strategy(query, people)
        val indices = when (st) {
            "ALL" -> strategy.all()
            "ANY" -> strategy.any()
            "NONE" -> strategy.none()
            else -> mutableListOf()
        }

        if (indices.isNotEmpty()) {
            println("${indices.size} persons found:")
            for (i in indices) println(info[i])
        } else println("No matching people found.")
        println()
    }

    fun printData() {
        println()
        for (query in info) println(query)
        println()
    }
}
