package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DubbingClip
import com.example.model.SampleClipsRepository
import com.example.ui.DubbingViewModel

@Composable
fun ClipsLibraryScreen(
    viewModel: DubbingViewModel,
    onClipSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Studio Banner
        item {
            Spacer(Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_dubbing_hero),
                        contentDescription = "Studio Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF1C1B1F).copy(alpha = 0.9f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "مكتبة المشاهد الجاهزة للدبلجة",
                            color = Color(0xFFE6E1E5),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اختر أي مقطع وابدأ بالدبلجة بصوتك أو بأصوات الذكاء الاصطناعي",
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "المشاهد المتاحة للدبلجة (${SampleClipsRepository.clips.size})",
                    color = Color(0xFFE6E1E5),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Clips List
        items(SampleClipsRepository.clips) { clip ->
            ClipCard(
                clip = clip,
                onSelect = {
                    viewModel.loadClip(clip)
                    onClipSelected()
                }
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun ClipCard(
    clip: DubbingClip,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("clip_item_${clip.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = BorderStroke(1.dp, Color(0xFF49454F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF4A4458),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = clip.coverEmoji, fontSize = 24.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = clip.title,
                            color = Color(0xFFE6E1E5),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = clip.description,
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footer info & Dub Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4A4458),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = clip.category,
                            color = Color(0xFFD0BCFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Duration
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF938F99),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${clip.durationSeconds} ثانية",
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Dialogues count
                    Text(
                        text = "${clip.scriptLines.size} حوارات",
                        color = Color(0xFF938F99),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("dub_clip_btn_${clip.id}")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF381E72), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ابدأ الدبلجة", color = Color(0xFF381E72), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
