package minesweeper

import kotlin.random.Random
import java.util.Scanner
val random: Random = Random.Default
val scanner: Scanner = Scanner(System.`in`)

fun main() {
    print(TextList().textList[0])
    val game = Game(scanner.nextInt())
    while (!game.stopGame) {
        game.printingField()
        print(TextList().textList[1])
        game.markCell(scanner.nextInt() - 1, scanner.nextInt() - 1, scanner.next())
        if (game.counting("*") == game.numberOfMines && game.counting("X") == 0) {
            game.printingField()
            println(TextList().textList[3])
            break
        }
        if (game.stopGame) game.gameOver()
    }

}

class TextList {
    val textList: List<String> = listOf(
        "How many mines do you want on the field? ",
        "Set/unset mine marks or claim a cell as free ",
        "There is a number here!",
        "Congratulations! You found all the mines!",
        "You stepped on a mine and failed!"
    )
}
class Game(
    val numberOfMines: Int,
    private val sizeOfTheField: Int = 9,
    private val field: MutableList<MutableList<Int>> = MutableList(sizeOfTheField) { MutableList(sizeOfTheField) { 0 } }
) {
    private val stringField = MutableList<MutableList<String>>(sizeOfTheField) { mutableListOf() }
    private val memSafe = mutableListOf<String>()
    private val memDigit = mutableListOf<String>()
    private val memBomb = mutableListOf<String>()
    var stopGame = false

    init {
        countingMines()
        minesAround()
        for (ind in 0 until sizeOfTheField) {
            stringField[ind] = toStringList()[ind]
        }
    }

    private fun plantingMine() {
        val row = random.nextInt(sizeOfTheField)
        val column = random.nextInt(sizeOfTheField)
        if (field[row][column] != 9) {
            field[row][column] = 9
        } else plantingMine()
    }

    private fun countingMines() {
        repeat(numberOfMines) {
            plantingMine()
        }
    }

    private fun minesAround() {
        for (row in field.indices) {
            for (column in field[row].indices) {
                if (field[row][column] == 9) {
                    for (indR in row - 1..row + 1) {
                        for (indC in column - 1..column + 1) {
                            try {
                                if (field [indR][indC] != 9) field [indR][indC] += 1
                            } catch (_:Exception) {}
                        }
                    }
                }
            }
        }
    }

    private fun toStringList(): MutableList<MutableList<String>> {
        val strList = MutableList<MutableList<String>>(sizeOfTheField) { mutableListOf() }
        for (list in field.indices) {
            field[list].forEach {
                when (it) {
                    9 -> strList[list].add("X")
                    0 -> strList[list].add("/")
                    else -> strList[list].add(it.toString())
                }
            }
        }
        return strList
    }

    private fun hidingField(row: Int, column: Int): Boolean {
        val target = row.toString() + column.toString()
        return target in memDigit || target in memSafe || target in memBomb || stringField[row][column] == "*"
    }

    fun printingField() {
        val outputField = MutableList(sizeOfTheField) { MutableList(sizeOfTheField) { "" } }
        val intArr = 1..outputField.size
        for (row in stringField.indices) {
            for (column in stringField[row].indices) {
                val cell = if (!hidingField(row, column)) "." else stringField[row][column]
                outputField[row][column] = cell
            }
        }
        println(" |${ intArr.joinToString("") }|")
        println("-|${ "-".repeat(outputField.size) }|")
        for (ind in 1.. outputField.size) {
            println("$ind|${ outputField[ind - 1].joinToString("") }|")
        }
        println("-|${ "-".repeat(outputField.size) }|")
    }

    fun markCell(column: Int, row: Int, command: String) {
        if (command == "mine") {
            if (stringField[row][column] != "*") {
                if (stringField[row][column] in "1".."8" && row.toString() + column.toString() in memDigit) {
                    println(TextList().textList[2])
                } else
                    when (stringField[row][column]) {
                        in "1".."8" -> memDigit.add(row.toString() + column.toString()).also { stringField[row][column] = "*" }
                        "/" -> memSafe.add(row.toString() + column.toString()).also { stringField[row][column] = "*" }
                        "X" -> memBomb.add(row.toString() + column.toString()).also { stringField[row][column] = "*" }
                    }
            } else
                when (row.toString() + column.toString()) {
                    in memDigit -> memDigit.remove(row.toString() + column.toString()).also {
                        stringField[row][column] = field[row][column].toString()
                    }
                    in memSafe -> memSafe.remove(row.toString() + column.toString()).also {
                        stringField[row][column] = "/"
                    }
                    in memBomb -> memBomb.remove(row.toString() + column.toString()).also {
                        stringField[row][column] = "X"
                    }
                }
        } else if (command == "free") {
            if (stringField[row][column] != "*") {
                when {
                    stringField[row][column] in "1".."8" -> memDigit.add(row.toString() + column.toString())
                    stringField[row][column] == "/" -> memSafe.add(row.toString() + column.toString()).also {
                        openSafeCells(row, column)
                    }
                    field[row][column] == 9 -> stopGame = true.also { println(TextList().textList[4]) }
                }
            } else
                when (row.toString() + column.toString()) {
                    in memDigit -> stringField[row][column] = field[row][column].toString()
                    in memSafe -> stringField[row][column] = "/". also {
                        openSafeCells(row, column)
                    }
                    in memBomb -> stopGame = true
                }
        }
    }

    fun gameOver() {
        for (row in field.indices) {
            for (column in field[row].indices) {
                if (field[row][column] == 9) {
                    memBomb.add( row.toString() + column.toString())
                } else if (stringField[row][column] == "*" && row.toString() + column.toString() in memBomb) {
                    stringField[row][column] = "X"
                }
            }
        }
        printingField()
        println(TextList().textList[4])
    }

    fun counting(target: String): Int {
        val sumList = mutableListOf<Int>()
        stringField.forEach { list ->
            sumList.add(list.count { it == target})
        }
        return sumList.sum()
    }

    private val checkList = mutableListOf<String>()

    private fun openSafeCells(row: Int, column: Int) {
        for (indR in row - 1..row + 1) {
            for (indC in column - 1..column + 1) {
                if (indR in 0 until stringField.size
                    && indC in 0 until stringField.size
                    && indR.toString() + indC.toString() !in checkList) {
                    if (stringField[indR][indC] == "/" && indR.toString() + indC.toString() !in memSafe) {
                        memSafe.add(indR.toString() + indC.toString())
                        checkList.add(indR.toString() + indC.toString())
                        openSafeCells(indR, indC)
                    } else if (stringField[indR][indC] in "1".."8" && indR.toString() + indC.toString() !in memDigit) {
                        memDigit.add(indR.toString() + indC.toString())
                        checkList.add(indR.toString() + indC.toString())
                    } else if ( stringField[indR][indC] == "*" && indR.toString() + indC.toString() in memSafe) {
                        stringField[indR][indC] = "/"
                        checkList.add(indR.toString() + indC.toString())
                        openSafeCells(indR, indC)
                    } else if (stringField[indR][indC] == "*" && indR.toString() + indC.toString() in memDigit) {
                        stringField[indR][indC] = field[indR][indC].toString()
                        checkList.add(indR.toString() + indC.toString())
                    }
                }
            }
        }
    }
}