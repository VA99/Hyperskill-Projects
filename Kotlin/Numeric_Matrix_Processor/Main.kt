package processor

import java.util.Scanner

class Matrix(private val n: Int, private val m: Int) {
    private var values = Array(n) { Array(m) { .0 } }

    fun setMatrix() {
        val matrixScanner = Scanner(System.`in`)
        for (i in 0 until this.n) {
            val rowText = matrixScanner.nextLine()
            val rowScanner = Scanner(rowText)
            for (j in 0 until this.m) if (rowScanner.hasNextLine()) this.values[i][j] += rowScanner.next().toDouble()
        }
    }

    val info: String
        get() {
            var result = "The result is:\n"
            for (row in this.values) {
                for (element in row) {
                    var p = 1
                    var c = 0
                    while (element * p - Math.floor(element * p) != .0) {
                        p *= 10
                        c++
                    }
                    if (c > 2) c = 2
                    result += if (p == 1) "%4d ".format(element.toInt()) else "%${3 + c}.${c}f ".format(element)
                }
                result += '\n'
            }
            return result
        }

    fun sameDimensions(newMatrix: Matrix) = this.n == newMatrix.n && this.m == newMatrix.m
    fun equalInnerDimensions(newMatrix: Matrix) = this.m == newMatrix.n

    fun add(nextMatrix: Matrix): Matrix {
        val newMatrix = Matrix(this.n, this.m)
        for (i in 0 until this.n) {
            for (j in 0 until this.m) {
                newMatrix.values[i][j] = this.values[i][j] + nextMatrix.values[i][j]
            }
        }
        return newMatrix
    }

    fun multiply(constant: Double): Matrix {
        val newMatrix = Matrix(this.n, this.m)
        for (i in 0 until this.n) {
            for (j in 0 until this.m) {
                newMatrix.values[i][j] = constant * this.values[i][j]
            }
        }
        return newMatrix
    }

    fun multiply(nextMatrix: Matrix): Matrix {
        val newMatrix = Matrix(this.n, nextMatrix.m)
        for (i in 0 until this.n) {
            for (j in 0 until nextMatrix.m) {
                for (x in 0 until this.m) {
                    newMatrix.values[i][j] += this.values[i][x] * nextMatrix.values[x][j]
                }
            }
        }
        return newMatrix
    }

    fun mainDiagonalTranspose(): Matrix {
        val newMatrix = Matrix(this.m, this.n)
        for (i in 0 until this.n) for (j in 0 until this.m) newMatrix.values[j][i] = this.values[i][j]
        return newMatrix
    }

    fun sideDiagonalTranspose(): Matrix {
        val newMatrix = Matrix(this.m, this.n)
        for (i in this.n - 1 downTo 0) for (j in this.m - 1 downTo 0) newMatrix.values[j][i] = this.values[this.n - 1 - i][this.m - 1 - j]
        return newMatrix
    }

    fun verticalTranspose(): Matrix {
        val newMatrix = Matrix(this.n, this.m)
        for (i in 0 until this.n) {
            for (j in 0 until this.m / 2) {
                newMatrix.values[i][j] = this.values[i][newMatrix.values.size - 1 - j]
                newMatrix.values[i][newMatrix.values.size - 1 - j] = this.values[i][j]
            }
        }
        return newMatrix
    }

    fun horizontalTranspose(): Matrix {
        val newMatrix = Matrix(this.n, this.m)
        for (i in 0 until this.n / 2) {
            for (j in 0 until this.m) {
                newMatrix.values[i][j] = this.values[newMatrix.values.size - 1 - i][j]
                newMatrix.values[newMatrix.values.size - 1 - i][j] = this.values[i][j]
            }
        }
        return newMatrix
    }

    private fun matrixOfMinor(matrix: Matrix = this, row: Int = 0, column: Int = 0): Matrix {
        val newMatrix = Matrix(this.n - 1, this.m - 1)
        newMatrix.values = matrix.values.filterIndexed { index, _ -> index != row }.toTypedArray()
        newMatrix.values.forEachIndexed { i, doubles -> newMatrix.values[i] = doubles.filterIndexed { j, _ -> j != column }.toTypedArray() }
        return newMatrix
    }

    fun determinant(matrix: Matrix = this, row: Int = 0, column: Int = 0): Double {
        if (matrix.values.size == 1) return matrix.values[0][0]
        if (column == matrix.values.size) return .0

        val newMatrix = matrixOfMinor(matrix, row, column)
        val unaryOp = if ((row + column) % 2 == 0) 1 else -1
        return unaryOp * matrix.values[row][column] * determinant(newMatrix, row, 0) + determinant(matrix, row, column + 1)
    }

    private fun minorOf(row: Int, column: Int): Double {
        val matrix = matrixOfMinor(this, row, column)
        return this.determinant(matrix)
    }

    private fun cofactorOf(row: Int, column: Int): Double {
        val unaryOp = if ((row + column) % 2 == 0) 1 else -1
        return unaryOp * this.minorOf(row, column)
    }

    fun adjoint(): Matrix {
        val matrix = Matrix(this.n, this.m)
        for (i in 0 until this.n) for (j in 0 until this.m) matrix.values[i][j] = cofactorOf(i, j)
        return matrix.mainDiagonalTranspose()
    }
}

class MatrixCalculator {
    fun menu() {
        println("1. Add matrices")
        println("2. Multiply matrix to a constant")
        println("3. Multiply matrices")
        println("4. Transpose matrix")
        println("5. Calculate a determinant")
        println("6. Inverse matrix")
        println("0. Exit")
        print("Your choice: > ")
    }

    fun transposeMenu() {
        println("\n1. Main diagonal")
        println("2. Side diagonal")
        println("3. Vertical line")
        println("4. Horizontal line")
        print("Your choice: > ")
    }

    var menuChoice = ""
    var transposeChoice = ""
    var dimensions = listOf<String>()
    val n: Int get() = dimensions.first().toInt()
    val m: Int get() = dimensions.last().toInt()

    val matricesSize: Int
        get() = when (menuChoice) {
            in "13" -> 2
            in "2456" -> 1
            else -> 0
        }
}

fun main() {
    val scanner = Scanner(System.`in`)
    val matrixCalculator = MatrixCalculator()

    do {
        matrixCalculator.menu()
        matrixCalculator.menuChoice = scanner.nextLine()

        if (matrixCalculator.menuChoice != "0") {
            if (matrixCalculator.menuChoice == "4") {
                matrixCalculator.transposeMenu()
                matrixCalculator.transposeChoice = scanner.nextLine()
            }

            var matrices = arrayOf<Matrix>()
            for (n in 1..matrixCalculator.matricesSize) {
                print("Enter matrix size: ")
                matrixCalculator.dimensions = scanner.nextLine().split(' ')
                matrices += Matrix(matrixCalculator.n, matrixCalculator.m)
                println("Enter matrix:")
                matrices.last().setMatrix()
            }

            when (matrixCalculator.menuChoice) {
                "1" -> {
                    if (matrices.first().sameDimensions(matrices.last())) {
                        println(matrices.first().add(matrices.last()).info)
                    } else println("ERROR")
                }
                "2" -> {
                    val constant = scanner.nextLine().toDouble()
                    println(matrices.first().multiply(constant).info)
                }
                "3" -> {
                    if (matrices.first().equalInnerDimensions(matrices.last())) {
                        println(matrices.first().multiply(matrices.last()).info)
                    }
                }
                "4" -> {
                    when(matrixCalculator.transposeChoice) {
                        "1" -> println(matrices.first().mainDiagonalTranspose().info)
                        "2" -> println(matrices.first().sideDiagonalTranspose().info)
                        "3" -> println(matrices.first().verticalTranspose().info)
                        "4" -> println(matrices.first().horizontalTranspose().info)
                    }
                }
                "5" -> {
                    println("The result is:")
                    println(matrices.first().determinant())
                    println()
                }
                "6" -> {
                    val det = matrices.first().determinant()
                    if (det != .0) {
                        val detInverse = 1 / det
                        val invertedMatrix = matrices.first().adjoint().multiply(detInverse)
                        println(invertedMatrix.info)
                    }
                }
            }
        }
    } while (matrixCalculator.menuChoice != "0")
}
