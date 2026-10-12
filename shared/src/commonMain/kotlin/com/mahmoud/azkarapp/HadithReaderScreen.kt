package com.mahmoud.azkarapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HadithReaderScreen(hadiths: List<Hadith>) {
    if (hadiths.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد أحاديث", color = TextBrown)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(hadiths) { hadith ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth()
                ) {
                    Text(
                        text = "الحديث",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HeaderGreen,
                        modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = hadith.text,
                        fontSize = 22.sp,
                        color = TextBrown,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = AccentGold.copy(alpha = 0.3f))
                    
                    Text(
                        text = "الشرح",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeaderGreen,
                        modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                    )

                    Text(
                        text = hadith.explanation,
                        fontSize = 18.sp,
                        color = TextBrown.copy(alpha = 0.8f),
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
