package com.swapnil.donutchart.presentation

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DonutChart(
    data: Map<String, Int>,
    radiusOuter: Dp = 120.dp,
    chartBarWidth: Dp = 25.dp,
    animDuration: Int = 1000,
    arcSpacing: Float = 18f, // Space between arcs
    ringBgColor: Color = Color.Transparent,
    ringBorderColor: Color = Color.Transparent,
    minValue:Int = 0,
    maxValue:Int = 120,
    imageUrls: List<String?>, // New parameter for images
) {

    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var floatValue by remember { mutableStateOf(listOf<Float>()) }

    val isSelected = remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val imageLoader = ImageLoader(context)


// Load the image asynchronously
    val imageBitmaps = remember { mutableStateListOf<ImageBitmap?>(null, null, null, null, null) }

    val arcColors = listOf(
        listOf(Color(0xFFff7e5f), Color(0xFFfeb47b)),
        listOf(Color(0xFF00c6ff), Color(0xFF0072ff)),
        listOf(Color(0xFFf7971e), Color(0xFFffd200)),
        listOf( Color(0xFF0E5C4C),Color(0xFF23af92)),
        listOf(Color(0xFFee9ca7), Color(0xFFffdde1))
    )

    var animationPlayed by remember { mutableStateOf(false) }
    var lastValue = 0f

    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) radiusOuter.value * 2f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            easing = LinearOutSlowInEasing
        )
    )

    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 90f * 11f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            easing = LinearOutSlowInEasing
        )
    )

    LaunchedEffect(imageUrls) {
        imageUrls.forEachIndexed { index, url ->
            if (url != null) {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(50, 50)
                    .allowHardware(false) // Disable hardware bitmaps
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    imageBitmaps[index] = result.drawable.toBitmap().copy(Bitmap.Config.ARGB_8888, true).asImageBitmap()
                }
            }
        }
    }


    LaunchedEffect(data) {
        val totalSum = data.values.sum()
        val gapCount = data.size  // one gap per slice, or adjust as needed
        val totalGapDegrees = gapCount * arcSpacing
        val sliceDegrees = 360f - totalGapDegrees

        floatValue = data.values.map { value ->
            (sliceDegrees * value.toFloat() / totalSum.toFloat())
        }
    }

    LaunchedEffect(Unit) {
        /* val result = imageLoader.execute(imageRequest)
         if (result is SuccessResult) {
             imageBitmap = result.drawable.toBitmap().asImageBitmap()
         }*/

        animationPlayed = true

        delay(2000L) // Delay for 1 second (1000 milliseconds)
        isSelected.value = true
    }



    Column(
        modifier = Modifier.fillMaxWidth()
            .height(300.dp)
            .offset(y=20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(animateSize.dp),
            contentAlignment = Alignment.Center
        ) {

            Canvas(
                modifier = Modifier
                    .offset { IntOffset.Zero }
                    .size(radiusOuter * 2f)
                    .rotate(animateRotation)
            ) {

                // **Draw the background ring (Donut Shape)**
                drawArc(
                    color = ringBorderColor, // Gray border color
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx() * 2, cap = StrokeCap.Butt) // Slightly larger width
                )

                // **Draw the background ring (Donut Shape)**
                drawArc(
                    color = ringBgColor, // Dark background ring color
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke((chartBarWidth * 2 - 10.dp ).toPx() , cap = StrokeCap.Butt) // No rounded edges
                )

                val outerRadius = (radiusOuter + chartBarWidth / 2f).toPx() + 40 // Convert to Px
                val gap = 10f
                val width = size.width
                val height = size.height
                val circleThickness = width / 25f
                circleCenter = Offset(x = width / 2f, y = height / 2f)

                for (i in 0 until (maxValue - minValue)) {
                    val color = ringBorderColor
                    val angleInDegrees = i * 360f / (maxValue - minValue).toFloat()
                    val angleInRad = angleInDegrees * PI / 180f + PI / 2f

                    val yGapAdjustment = cos(angleInDegrees * PI / 180f) * gap
                    val xGapAdjustment = -sin(angleInDegrees * PI / 180f) * gap

                    val start = Offset(
                        x = (outerRadius * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                        y = (outerRadius * sin(angleInRad) + circleCenter.y + yGapAdjustment).toFloat()
                    )

                    val end = Offset(
                        x = (outerRadius * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                        y = (outerRadius * sin(angleInRad) + circleCenter.y + circleThickness + yGapAdjustment).toFloat()
                    )

                    rotate(
                        angleInDegrees,
                        pivot = start
                    ) {
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // **Draw the arcs on top**
                floatValue.forEachIndexed { index, value ->
                    val startAngleRad = Math.toRadians(lastValue.toDouble())
                    val endAngleRad = Math.toRadians((lastValue + value).toDouble())

                    val startX = (size.width / 2) + (radiusOuter.toPx() * cos(startAngleRad)).toFloat()
                    val startY = (size.height / 2) + (radiusOuter.toPx() * sin(startAngleRad)).toFloat()

                    val endX = (size.width / 2) + (radiusOuter.toPx() * cos(endAngleRad)).toFloat()
                    val endY = (size.height / 2) + (radiusOuter.toPx() * sin(endAngleRad)).toFloat()

                    val brush = Brush.linearGradient(
                        colors = arcColors[index],
                        start = Offset(startX, startY),
                        end = Offset(endX, endY)
                    )

                    drawArc(
                        brush = brush,
                        startAngle = lastValue,
                        sweepAngle = maxOf(0f, value), // Ensure it's not negative
                        useCenter = false,
                        style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Round) // Rounded edges
                    )

                    // **Calculate Midpoint Angle**
                    val midAngle = lastValue + (value / 2)
                    val midAngleRad = Math.toRadians(midAngle.toDouble())

                    // **Find the Line Start & End Coordinates**
                    val lineStart = Offset(
                        x = (size.width / 2) + ((radiusOuter.toPx() + chartBarWidth.toPx() / 2) * cos(midAngleRad)).toFloat(),
                        y = (size.height / 2) + ((radiusOuter.toPx() + chartBarWidth.toPx() / 2) * sin(midAngleRad)).toFloat()
                    )

                    val lineEnd = Offset(
                        x = (size.width / 2) + ((radiusOuter.toPx() + chartBarWidth.toPx() + 15.dp.toPx()) * cos(midAngleRad)).toFloat(),
                        y = (size.height / 2) + ((radiusOuter.toPx() + chartBarWidth.toPx() + 15.dp.toPx()) * sin(midAngleRad)).toFloat()
                    )

                    // **Draw the Highlighted Line**
                    drawLine(
                        color = arcColors[index].first(), // Start color of the gradient
                        start = lineStart,
                        end = lineEnd,
                        strokeWidth = 4.dp.toPx(), // Slightly thicker for visibility
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = arcColors[index].first(), // Same color as the highlighted line
                        radius = 12.dp.toPx(), // Adjust size as needed
                        center = lineEnd // Place it at the end of the line
                    )


                    drawIntoCanvas { canvas ->
                        imageBitmaps.getOrNull(index)?.let { bitmap ->
                            val imageSize = 28.dp.toPx() // Adjust size as needed
                            val imageOffset = Offset(
                                x = lineEnd.x - imageSize / 2 + 18,
                                y = lineEnd.y - imageSize / 2 + 18
                            )

                            val roundBitmap = bitmap.asAndroidBitmap().let {
                                Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888).apply {
                                    val canvas = android.graphics.Canvas(this)
                                    val paint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        shader = android.graphics.BitmapShader(it, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
                                    }
                                    val radius = it.width.coerceAtMost(it.height) / 2f
                                    canvas.drawCircle(it.width / 2f, it.height / 2f, radius, paint)
                                }
                            }.asImageBitmap()

                            canvas.drawImage(
                                image = roundBitmap,
                                topLeftOffset = imageOffset,
                                paint = Paint()
                            )
                        }
                    }

                    lastValue += value + arcSpacing // Add spacing between arcs
                }

            }
        }
    }
}
