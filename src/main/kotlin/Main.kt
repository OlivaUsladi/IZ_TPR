import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.min

fun main() = application {
    val applicationState = remember { MyApplicationState() }

    for (window in applicationState.windows) {
        key(window) {
            when (window.type) {
                WindowType.FIRST_SCREEN -> firstScreen(window)
                WindowType.SECOND_SCREEN -> secondScreen(window)
            }
        }
    }
}

enum class WindowType {
    FIRST_SCREEN,
    SECOND_SCREEN
}

private class MyApplicationState {
    val windows = mutableStateListOf<MyWindowState>()

    init {
        windows += MyWindowState("Initial window", WindowType.FIRST_SCREEN)
    }

    fun openNewWindow() {
        windows += MyWindowState("Результаты", WindowType.SECOND_SCREEN)
    }

    fun exit() {
        windows.clear()
    }

    private fun MyWindowState(
        title: String,
        type: WindowType = WindowType.FIRST_SCREEN
    ) = MyWindowState(
        title,
        type,
        openNewWindow = ::openNewWindow,
        exit = ::exit,
        windows::remove
    )
}

private class MyWindowState(
    val title: String,
    val type: WindowType,
    val openNewWindow: () -> Unit,
    val exit: () -> Unit,
    private val close: (MyWindowState) -> Unit
) {
    fun close() = close(this)
}

// Класс для хранения данных об ограничении
data class Constraint(
    val x1: String,
    val x2: String,
    val isGreaterThan: Boolean, // true - >=, false - <=
    val value: String // значение ограничения
)

val criteria = mutableStateListOf<Criterion>()
val constraints = mutableStateListOf<Constraint>()
val k_x1 = mutableStateOf(0)
val k_x2 = mutableStateOf(0)

// Класс для хранения данных о критерии
data class Criterion(
    val x1: String,
    val x2: String,
    val isMaximized: Boolean, // true - максимизация, false - минимизация
    val constraintValue: String // значение для дополнительного ограничения
)

@Composable
private fun firstScreen(
    state: MyWindowState
) = Window(onCloseRequest = state::close, title = state.title) {
    MenuBar {
        Menu("File") {
            Item("New window", onClick = state.openNewWindow)
            Item("Exit", onClick = state.exit)
        }
    }

    val x1 = remember { mutableStateOf("0") }
    val x2 = remember { mutableStateOf("0") }

    Column {
        Box {
            Row {
                Column {
                    Text(
                        text = "Введите главный критерий",
                        fontSize = 20.sp
                    )
                    Row {
                        TextField(
                            value = x1.value,
                            onValueChange = { newText -> x1.value = newText},
                            placeholder = { Text("0") },
                            modifier = Modifier.width(70.dp)
                        )
                        Text("x1",
                            fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("+")
                        Spacer(modifier = Modifier.width(5.dp))
                        TextField(
                            value = x2.value,
                            onValueChange = { x2.value = it },
                            placeholder = { Text("0") },
                            modifier = Modifier.width(70.dp)
                        )
                        Text("x2",
                            fontSize = 20.sp)
                    }

                    // RadioButton для главного критерия
                    Row {
                        Text("Оптимизация:")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(
                            selected = true,
                            onClick = { }
                        )
                        Text("Максимизация", modifier = Modifier.padding(end = 8.dp))
                    }

                    Button(onClick = {
                        criteria.add(Criterion("0", "0", true, "0"))
                    }) {
                        Text("Добавить критерий")
                    }

                    criteria.forEachIndexed { index, criterion ->
                        Column {
                            Row {
                                TextField(
                                    value = criterion.x1,
                                    onValueChange = { newValue ->
                                        criteria[index] = criterion.copy(x1 = newValue)
                                    },
                                    modifier = Modifier.width(70.dp)
                                )
                                Text("x1",
                                    fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("+",
                                    fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                TextField(
                                    value = criterion.x2,
                                    onValueChange = { newValue ->
                                        criteria[index] = criterion.copy(x2 = newValue)
                                    },
                                    modifier = Modifier.width(70.dp)
                                )
                                Text("x2",
                                    fontSize = 20.sp)
                            }

                            // Группа RadioButton для выбора типа оптимизации
                            Row {
                                Text("Оптимизация:")
                                Spacer(modifier = Modifier.width(8.dp))

                                // Максимизация
                                Row(Modifier.clickable {
                                    criteria[index] = criterion.copy(isMaximized = true)
                                }) {
                                    RadioButton(
                                        selected = criterion.isMaximized,
                                        onClick = {
                                            criteria[index] = criterion.copy(isMaximized = true)
                                        }
                                    )
                                    Text("Максимизация", modifier = Modifier.padding(end = 8.dp))
                                }

                                // Минимизация
                                Row(Modifier.clickable {
                                    criteria[index] = criterion.copy(isMaximized = false)
                                }) {
                                    RadioButton(
                                        selected = !criterion.isMaximized,
                                        onClick = {
                                            criteria[index] = criterion.copy(isMaximized = false)
                                        }
                                    )
                                    Text("Минимизация")
                                }
                            }

                            // Поле для ввода значения дополнительного ограничения
                            Row {
                                Text(
                                    text = if (criterion.isMaximized) ">=" else "<=",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                TextField(
                                    value = criterion.constraintValue,
                                    onValueChange = { newValue ->
                                        criteria[index] = criterion.copy(constraintValue = newValue)
                                    },
                                    modifier = Modifier.width(70.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(60.dp))
                Column {
                    Text("Ограничения", fontSize = 20.sp)

                    Button(onClick = {
                        constraints.add(Constraint("0", "0", true, "0"))
                    }) {
                        Text("Добавить ограничение")
                    }

                    constraints.forEachIndexed { index, constraint ->
                        Column {
                            Row {
                                TextField(
                                    value = constraint.x1,
                                    onValueChange = { newValue ->
                                        constraints[index] = constraint.copy(x1 = newValue)
                                    },
                                    modifier = Modifier.width(70.dp)
                                )
                                Text("x1",
                                    fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("+",
                                    fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                TextField(
                                    value = constraint.x2,
                                    onValueChange = { newValue ->
                                        constraints[index] = constraint.copy(x2 = newValue)
                                    },
                                    modifier = Modifier.width(70.dp)
                                )
                                Text("x2",
                                    fontSize = 20.sp)

                                Row {
                                    Row(Modifier.clickable {
                                        constraints[index] = constraint.copy(isGreaterThan = true)
                                    }) {
                                        RadioButton(
                                            selected = constraint.isGreaterThan,
                                            onClick = {
                                                constraints[index] = constraint.copy(isGreaterThan = true)
                                            }
                                        )
                                        Text("≥", modifier = Modifier.padding(end = 8.dp),
                                            fontSize = 20.sp)
                                    }

                                    Row(Modifier.clickable {
                                        constraints[index] = constraint.copy(isGreaterThan = false)
                                    }) {
                                        RadioButton(
                                            selected = !constraint.isGreaterThan,
                                            onClick = {
                                                constraints[index] = constraint.copy(isGreaterThan = false)
                                            }
                                        )
                                        Text("≤", modifier = Modifier.padding(end = 8.dp),
                                            fontSize = 20.sp)
                                    }
                                }

                                TextField(
                                    value = constraint.value,
                                    onValueChange = { newValue ->
                                        constraints[index] = constraint.copy(value = newValue)
                                    },
                                    modifier = Modifier.width(70.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Button(onClick = {
            // Сохраняем коэффициенты главного критерия
            k_x1.value = x1.value.toIntOrNull() ?: 0
            k_x2.value = x2.value.toIntOrNull() ?: 0

            // Открываем окно с результатами
            state.openNewWindow()
        }){
            Text("Посчитать")
        }
    }
}


@Composable
private fun secondScreen(
    state: MyWindowState
) = Window(onCloseRequest = state::close, title = "Результаты") {
    Column {
        Text("Результаты расчета:", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))


        Text("Главный критерий:\n" + "${k_x1.value}x1 + ${k_x2.value}x2")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Критерии:")
        criteria.forEachIndexed { index, criterion ->
            Text("Критерий ${index + 1}: ${criterion.x1}x1 + ${criterion.x2}x2, ${if (criterion.isMaximized) ">=" else "<="} ${criterion.constraintValue}"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Ограничения:")
        constraints.forEachIndexed { index, constraint ->
            Text("Ограничение ${index + 1}: ${constraint.x1}x1 + ${constraint.x2}x2 ${
                if (constraint.isGreaterThan) "≥" else "≤"
            } ${constraint.value}")
        }

        //Делаем остальные критерии ограничениями
        val allConstraints = constraints + criteria.map { criterion ->
            Constraint(
                x1 = criterion.x1,
                x2 = criterion.x2,
                isGreaterThan = criterion.isMaximized,
                value = criterion.constraintValue
            )
        }

        //Переводим строки в числа
        val parsedConstraints = allConstraints.mapNotNull { constraint ->
            try {
                val a = constraint.x1.toDouble()
                val b = constraint.x2.toDouble()
                val c = constraint.value.toDouble()
                val isGreaterThan = constraint.isGreaterThan
                ConstraintEquation(a, b, isGreaterThan, c)
            } catch (e: NumberFormatException) {
                null
            }
        }


        //пересечение точек
        val intersectionPoints = findIntersectionPoints(parsedConstraints)


        //Проверяем область
        val feasiblePoints = intersectionPoints.filter { point ->
            parsedConstraints.all { constraint ->
                val value = constraint.a * point.x + constraint.b * point.y
                if (constraint.isGreaterThan) value >= constraint.c else value <= constraint.c
            }
        }

        //Считаем значение главного критерия в этих точках
        val mainCriterionValues = feasiblePoints.map { point ->
            point.x * k_x1.value + point.y * k_x2.value
        }

        //Поиск оптимальной точки
        val optimalIndex = mainCriterionValues.indices.maxByOrNull { mainCriterionValues[it] } ?: -1
        val optimalPoint = if (optimalIndex >= 0) feasiblePoints[optimalIndex] else null
        val optimalValue = if (optimalIndex >= 0) mainCriterionValues[optimalIndex] else null



        Spacer(modifier = Modifier.height(16.dp))

        if (optimalPoint != null && optimalValue != null) {
            Text("Оптимальная точка:", fontWeight = FontWeight.Bold)
            Text("x1 = ${"%.2f".format(optimalPoint.x)}, x2 = ${"%.2f".format(optimalPoint.y)}")
            Text("Значение главного критерия: ${"%.2f".format(optimalValue)}")
        } else {
            Text("Не удалось найти оптимальное решение", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("График:", fontWeight = FontWeight.Bold)
        Canvas(Modifier.size(500.dp).background(Color.White)) {
            val padding = 50f
            val scale = min(size.width, size.height) / 20f

            drawLine(
                color = Color.Black,
                start = Offset(padding, size.height - padding),
                end = Offset(size.width - padding, size.height - padding),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Black,
                start = Offset(padding, size.height - padding),
                end = Offset(padding, padding),
                strokeWidth = 2f
            )

            drawText("x1", Offset(size.width - padding + 10, size.height - padding - 10))
            drawText("x2", Offset(padding + 10, padding - 20))

            parsedConstraints.forEach { constraint ->
                val points = getLinePoints(constraint, size, padding, scale)
                if (points != null) {
                    drawLine(
                        color = Color(0xFF8888FF),
                        start = points.first,
                        end = points.second,
                        strokeWidth = 2f
                    )



                }
            }


            if (feasiblePoints.isNotEmpty()) {
                val path = Path().apply {
                    val firstPoint = feasiblePoints.first()
                    val screenPoint = toScreenCoords(firstPoint, size, padding, scale)
                    moveTo(screenPoint.x, screenPoint.y)

                    feasiblePoints.drop(1).forEach { point ->
                        val screenPoint = toScreenCoords(point, size, padding, scale)
                        lineTo(screenPoint.x, screenPoint.y)
                    }

                    close()
                }

                drawPath(
                    path = path,
                    color = Color(0x55FF8888),
                    style = Stroke(width = 3f)
                )
            }


            if (optimalPoint != null) {
                val screenPoint = toScreenCoords(optimalPoint, size, padding, scale)
                drawCircle(
                    color = Color.Red,
                    radius = 8f,
                    center = screenPoint
                )
            }
        }
    }
}

//Перевод из обычной системы координат в систему Jetpack Compose
private fun toScreenCoords(
    point: Point,
    size: androidx.compose.ui.geometry.Size,
    padding: Float,
    scale: Float
): Offset {
    return Offset(
        padding + point.x.toFloat() * scale,
        size.height - padding - point.y.toFloat() * scale
    )
}


private fun getLinePoints(
    constraint: ConstraintEquation,
    size: androidx.compose.ui.geometry.Size,
    padding: Float,
    scale: Float
): Pair<Offset, Offset>? {
    val width = size.width - 2 * padding
    val height = size.height - 2 * padding

    if (constraint.b == 0.0) {
        if (constraint.a == 0.0) return null
        val x = constraint.c / constraint.a
        return Pair(
            Offset(padding + x.toFloat() * scale, padding),
            Offset(padding + x.toFloat() * scale, size.height - padding)
        )
    } else {
        val x1 = 0.0
        val y1 = (constraint.c - constraint.a * x1) / constraint.b

        val x2 = width / scale
        val y2 = (constraint.c - constraint.a * x2) / constraint.b


        if ((y1 < 0 && y2 < 0) || (y1 > height/scale && y2 > height/scale)) {
            val y3 = 0.0
            val x3 = (constraint.c - constraint.b * y3) / constraint.a

            val y4 = height / scale
            val x4 = (constraint.c - constraint.b * y4) / constraint.a

            return Pair(
                Offset(padding + x3.toFloat() * scale, size.height - padding),
                Offset(padding + x4.toFloat() * scale, padding)
            )
        }

        return Pair(
            Offset(padding + x1.toFloat() * scale, size.height - padding - y1.toFloat() * scale),
            Offset(padding + x2.toFloat() * scale, size.height - padding - y2.toFloat() * scale)
        )
    }
}

data class ConstraintEquation(
    val a: Double,
    val b: Double,
    val isGreaterThan: Boolean,
    val c: Double
)

data class Point(val x: Double, val y: Double)

//Пересечение точек
fun findIntersectionPoints(constraints: List<ConstraintEquation>): List<Point> {
    val points = mutableListOf<Point>()

    for (i in constraints.indices) {
        for (j in i + 1 until constraints.size) {
            val c1 = constraints[i]
            val c2 = constraints[j]

            val det = c1.a * c2.b - c2.a * c1.b
            if (det != 0.0) {
                val x = (c2.b * c1.c - c1.b * c2.c) / det
                val y = (c1.a * c2.c - c2.a * c1.c) / det
                points.add(Point(x, y))
            }
        }
    }

    return points
}


private fun DrawScope.drawText(text: String, position: Offset) {

}