package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceProfile
import com.example.audio.library.HumanVoiceLibraryRepository
import com.example.audio.library.HumanVoiceModel
import com.example.audio.library.VoiceCategory
import com.example.audio.library.VoiceDialect
import com.example.audio.library.VoiceGender
import com.example.audio.library.VoiceLibraryFilter
import com.example.ui.DubbingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLibrarySheet(
    viewModel: DubbingViewModel,
    onDismiss: () -> Unit,
    onSelectVoice: (HumanVoiceModel) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<VoiceCategory?>(null) }
    var selectedGender by remember { mutableStateOf<VoiceGender?>(null) }
    var auditionVoiceId by remember { mutableStateOf<String?>(null) }

    val filter = remember(searchQuery, selectedCategory, selectedGender) {
        VoiceLibraryFilter(
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            selectedGender = selectedGender
        )
    }

    val voices = remember(filter) {
        HumanVoiceLibraryRepository.searchAndFilterVoices(filter, pageIndex = 0, pageSize = 50)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1B26),
        dragHandle = null,
        modifier = Modifier.testTag("voice_library_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مكتبة الأصوات البشرية الشاملة 🎙️",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اختر من بين 4,800+ صوت بشري متميز للدبلجة الحالية",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFFCAC4D0)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن صوت ممثل، لهجة، أو تصنيف...", fontSize = 12.sp, color = Color(0xFF938F99)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFD0BCFF)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color(0xFFCAC4D0))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF14121A),
                    unfocusedContainerColor = Color(0xFF14121A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF49454F)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null && selectedGender == null,
                    onClick = {
                        selectedCategory = null
                        selectedGender = null
                    },
                    label = { Text("الكل (4,800+)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF6750A4), selectedLabelColor = Color.White)
                )

                VoiceCategory.values().take(6).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text("${cat.emoji} ${cat.titleArabic}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF1E3A8A), selectedLabelColor = Color.White)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Voices List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(voices) { voice ->
                    val isPlaying = auditionVoiceId == voice.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isPlaying) Color(0xFF262235) else Color(0xFF24212C)),
                        border = BorderStroke(1.dp, if (isPlaying) Color(0xFF00E5FF) else Color(0xFF49454F).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(voice.avatarColorHex),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = voice.avatarEmoji, fontSize = 16.sp)
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = voice.nameArabic,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${voice.dialect.flagEmoji} ${voice.titleArabic}",
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Play Preview
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            auditionVoiceId = null
                                            viewModel.ttsManager.stop()
                                        } else {
                                            auditionVoiceId = voice.id
                                            viewModel.ttsManager.speakText(
                                                text = voice.sampleArabicPhrase,
                                                profile = VoiceProfile(
                                                    id = voice.id,
                                                    titleArabic = voice.nameArabic,
                                                    subtitleArabic = voice.titleArabic,
                                                    emoji = voice.avatarEmoji,
                                                    pitch = voice.basePitch,
                                                    speechRate = voice.speechRate
                                                ),
                                                onDone = {
                                                    if (auditionVoiceId == voice.id) auditionVoiceId = null
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "استماع",
                                        tint = if (isPlaying) Color(0xFFFF5252) else Color(0xFF00E5FF)
                                    )
                                }

                                // Select
                                Button(
                                    onClick = { onSelectVoice(voice) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                                ) {
                                    Text("اختيار 🎙️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
