package com.mahmoud.azkarapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import coil3.compose.AsyncImage
import azkarapp.shared.generated.resources.*

@Composable
fun ForcedUpdateDialog(
    isAndroid: Boolean = true,
    onUpdateClick: () -> Unit
) {
    Dialog(
        onDismissRequest = { }, // Cannot dismiss
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .size(330.dp) // Square shape - slightly larger to fit everything
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), // Slightly less padding to fit content
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Spread content to fill square
            ) {
                // App Icon
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(80.dp), // Slightly smaller icon
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, HeaderGreen.copy(alpha = 0.2f))
                ) {
                    AsyncImage(
                        model = Res.drawable.img_azkar,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp).fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }

                Text(
                    text = "تحديث جديد متوفر",
                    color = HeaderGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "يرجى تحديث التطبيق للاستمرار في استخدامه والحصول على أحدث المميزات.",
                    color = TextBrown.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Custom Icon depending on platform
                        if (isAndroid) {
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(34.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                
                                val path = androidx.compose.ui.graphics.Path()

                                // Blue part (Left)
                                path.reset()
                                path.moveTo(0f, 0f)
                                path.lineTo(w * 0.55f, h * 0.5f)
                                path.lineTo(0f, h)
                                path.close()
                                drawPath(path, Color(0xFF00A1F1))

                                // Green part (Top)
                                path.reset()
                                path.moveTo(0f, 0f)
                                path.lineTo(w, h * 0.45f)
                                path.lineTo(w * 0.55f, h * 0.5f)
                                path.close()
                                drawPath(path, Color(0xFF7DB700))

                                // Yellow part (Right)
                                path.reset()
                                path.moveTo(w, h * 0.45f)
                                path.lineTo(w, h * 0.55f)
                                path.lineTo(w * 0.55f, h * 0.5f)
                                path.close()
                                drawPath(path, Color(0xFFFFBB00))

                                // Red part (Bottom)
                                path.reset()
                                path.moveTo(w * 0.55f, h * 0.5f)
                                path.lineTo(w, h * 0.55f)
                                path.lineTo(0f, h)
                                path.close()
                                drawPath(path, Color(0xFFF25022))
                            }
                        } else {
                            // App Store Icon
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(34.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                val strokeW = w * 0.12f

                                drawCircle(color = Color(0xFF007AFF))

                                // Draw the 'A' shape
                                drawLine(
                                    color = Color.White,
                                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f),
                                    end = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.75f),
                                    strokeWidth = strokeW,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                drawLine(
                                    color = Color.White,
                                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f),
                                    end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.75f),
                                    strokeWidth = strokeW,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                drawLine(
                                    color = Color.White,
                                    start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.55f),
                                    end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.55f),
                                    strokeWidth = strokeW,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            "الذهاب للتحديث",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
