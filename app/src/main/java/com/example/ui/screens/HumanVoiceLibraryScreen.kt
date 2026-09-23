package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceProfile
import com.example.audio.library.HumanVoiceLibraryRepository
import com.example.audio.library.HumanVoiceModel
import com.example.audio.library.VoiceAgeGroup
import com.example.audio.library.VoiceCategory
import com.example.audio.library.VoiceDialect
import com.example.audio.library.VoiceGender
import com.example.audio.library.VoiceLibraryFilter
import com.example.ui.DubbingViewModel
import com.example.R
import com.example.ui.components.AccessibleImageCard
import com.example.ui.components.AudioWaveformVisualizer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HumanVoiceLibraryScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val favorites by HumanVoiceLibraryRepository.favoritesFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<VoiceGender?>(null) }
    var selectedDialect by remember { mutableStateOf<VoiceDialect?>(null) }
    var selectedCategory by remember { mutableStateOf<VoiceCategory?>(null) }
    var selectedAgeGroup by remember { mutableStateOf<VoiceAgeGroup?>(null) }
    var onlyVerified by remember { mutableStateOf(false) }
    var onlyFavorites by remember { mutableStateOf(false) }
    var showAiMatcherDialog by remember { mutableStateOf(false) }

    // Currently Auditioning Voice
    var activeAuditionVoiceId by remember { mutableStateOf<String?>(null) }
    var auditionAmplitude by remember { mutableStateOf(0.75f) }

    // Page state
    var currentPage by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val currentFilter = remember(
        searchQuery,
        selectedGender,
        selectedDialect,
        selectedCategory,
        selectedAgeGroup,
        onlyVerified,
        onlyFavorites
    ) {
        VoiceLibraryFilter(
            searchQuery = searchQuery,
            selectedGender = selectedGender,
            selectedDialect = selectedDialect,
            selectedCategory = selectedCategory,
            selectedAgeGroup = selectedAgeGroup,
            onlyVerified = onlyVerified,
            onlyFavorites = onlyFavorites
        )
    }

    val displayedVoices = remember(currentFilter, currentPage) {
        HumanVoiceLibraryRepository.searchAndFilterVoices(
            filter = currentFilter,
            pageIndex = 0,
            pageSize = (currentPage + 1) * 35
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("human_voice_library_screen"),
        containerColor = Color(0xFF141218),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مكتبة الأصوات البشرية للدبلجة 🎙️",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF))
                            ) {
                                Text(
                                    text = "4,800+ صوت بشري 🌟",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "أصوات طبيعية حقيقية بكافة اللهجات العربية وأنماط الإلقاء للدبلجة الفورية",
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    // AI Voice Matcher Assistant Action
                    IconButton(
                        onClick = { showAiMatcherDialog = true },
                        modifier = Modifier.testTag("ai_voice_matcher_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "مساعد المطابقة الصوتية الذكي",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1D1B20)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Accessible Visual Guide Banner for Blind & All Users
            AccessibleImageCard(
                imageRes = R.drawable.img_voice_characters,
                title = "مكتبة الأصوات الحية والشخصيات الكرتونية 🎭",
                visualDescription = "لوحة بصرية ثلاثية الأبعاد تجسد شخصيات كرتونية وأصوات درامية ووثائقية بعدة نبرات ولهجات عربية، جاهزة للاستخدام في دبلجة أعمالك.",
                accessibilityHint = "اضغط على زر الاستماع للوصف الصوتي لسماع هذا التوجيه نطقاً بصوت واضح لدعم المكفوفين وضعاف البصر.",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                badgeText = "دعم صوتي للمكفوفين ♿🔊"
            )

            // Search & Instant Filter Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211F26)),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Text Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "ابحث بالاسم، اللهجة (فصحى، خليجي، مصري، شامي)، أو الأسلوب (وثائقي، أنمي)...",
                                fontSize = 12.sp,
                                color = Color(0xFF938F99)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح",
                                        tint = Color(0xFFCAC4D0)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF191720),
                            unfocusedContainerColor = Color(0xFF191720),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    // Horizontal Scrollable Category & Dialect Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All Voices
                        FilterChip(
                            selected = selectedGender == null && selectedCategory == null && selectedDialect == null && !onlyFavorites,
                            onClick = {
                                selectedGender = null
                                selectedCategory = null
                                selectedDialect = null
                                selectedAgeGroup = null
                                onlyFavorites = false
                                onlyVerified = false
                            },
                            label = { Text("الكل (4,800+)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6750A4),
                                selectedLabelColor = Color.White
                            )
                        )

                        // Favorites Filter
                        FilterChip(
                            selected = onlyFavorites,
                            onClick = { onlyFavorites = !onlyFavorites },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("المفضلة (${favorites.size})", fontSize = 11.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C2D12),
                                selectedLabelColor = Color.White
                            )
                        )

                        // Verified Pro Filter
                        FilterChip(
                            selected = onlyVerified,
                            onClick = { onlyVerified = !onlyVerified },
                            label = { Text("موثق Pro 🌟", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF065F46),
                                selectedLabelColor = Color.White
                            )
                        )

                        // Gender Chips
                        VoiceGender.values().forEach { gender ->
                            FilterChip(
                                selected = selectedGender == gender,
                                onClick = {
                                    selectedGender = if (selectedGender == gender) null else gender
                                },
                                label = { Text("${gender.emoji} ${gender.titleArabic}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4A4458),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Category Chips
                        VoiceCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text("${cat.emoji} ${cat.titleArabic}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E3A8A),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Dialect Chips
                        VoiceDialect.values().forEach { dia ->
                            FilterChip(
                                selected = selectedDialect == dia,
                                onClick = {
                                    selectedDialect = if (selectedDialect == dia) null else dia
                                },
                                label = { Text("${dia.flagEmoji} ${dia.titleArabic}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF064E3B),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Results Counter & Active Audition Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تم العثور على ${displayedVoices.size} صوت متطابق",
                    color = Color(0xFFCAC4D0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                if (activeAuditionVoiceId != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1B4B),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "معاينة صوتية نشطة 🔊",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Voices List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedVoices, key = { it.id }) { voice ->
                    val isFavorite = favorites.contains(voice.id)
                    val isAuditioning = activeAuditionVoiceId == voice.id

                    VoiceProfileCard(
                        voice = voice,
                        isFavorite = isFavorite,
                        isAuditioning = isAuditioning,
                        onToggleFavorite = {
                            HumanVoiceLibraryRepository.toggleFavorite(voice.id)
                        },
                        onPlayAudition = {
                            if (isAuditioning) {
                                activeAuditionVoiceId = null
                                viewModel.ttsManager.stop()
                            } else {
                                activeAuditionVoiceId = voice.id
                                viewModel.ttsManager.speakText(
                                    text = voice.sampleArabicPhrase,
                                    profile = VoiceProfile(
                                        id = voice.id,
                                        titleArabic = voice.nameArabic,
                                        subtitleArabic = voice.titleArabic,
                                        emoji = voice.avatarEmoji,
                                        pitch = voice.basePitch,
                                        speechRate = voice.speechRate,
                                        description = voice.titleArabic
                                    ),
                                    onDone = {
                                        if (activeAuditionVoiceId == voice.id) {
                                            activeAuditionVoiceId = null
                                        }
                                    }
                                )
                            }
                        },
                        onApplyToDubbing = {
                            // Apply selected voice parameters directly to ViewModel
                            viewModel.ttsManager.speakText(
                                text = "تم تفعيل صوت ${voice.nameArabic} للدبلجة بنجاح!",
                                profile = VoiceProfile(
                                    id = voice.id,
                                    titleArabic = voice.nameArabic,
                                    subtitleArabic = voice.titleArabic,
                                    emoji = voice.avatarEmoji,
                                    pitch = voice.basePitch,
                                    speechRate = voice.speechRate
                                )
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تم تعيين صوت (${voice.nameArabic}) للدبلجة بنجاح! 🎙️")
                            }
                            onNavigateToStudio()
                        },
                        onUseInTts = {
                            onNavigateToTts()
                        }
                    )
                }

                // Load More Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { currentPage++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2930)),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            modifier = Modifier.testTag("load_more_voices_btn")
                        ) {
                            Text(
                                text = "عرض المزيد من الأصوات البشرية (صفحة ${currentPage + 1}) ⏬",
                                color = Color(0xFFEADDFF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // AI Voice Matcher Dialog
    if (showAiMatcherDialog) {
        AiVoiceMatcherModal(
            viewModel = viewModel,
            onDismiss = { showAiMatcherDialog = false },
            onSelectMatchedVoice = { matchedVoice ->
                showAiMatcherDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تطبيق صوت (${matchedVoice.nameArabic}) المتطابق مع نبرتك! ✨")
                }
                onNavigateToStudio()
            }
        )
    }
}

/**
 * Individual Voice Profile Card in the Library
 */
@Composable
fun VoiceProfileCard(
    voice: HumanVoiceModel,
    isFavorite: Boolean,
    isAuditioning: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayAudition: () -> Unit,
    onApplyToDubbing: () -> Unit,
    onUseInTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_card_${voice.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAuditioning) Color(0xFF221F35) else Color(0xFF211F26)
        ),
        border = BorderStroke(
            1.2.dp,
            if (isAuditioning) Color(0xFF00E5FF) else Color(0xFF49454F).copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Avatar, Name, Title, Badges, Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar with Color & Emoji
                    Surface(
                        shape = CircleShape,
                        color = Color(voice.avatarColorHex),
                        modifier = Modifier.size(44.dp),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = voice.avatarEmoji,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = voice.nameArabic,
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (voice.isVerifiedPro) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "موثق",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "★ ${voice.rating}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = voice.titleArabic,
                            color = Color(0xFFD0BCFF),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "تفضيل",
                        tint = if (isFavorite) Color(0xFFFF5252) else Color(0xFFCAC4D0)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Dialect, Category & Age Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2C2836)
                ) {
                    Text(
                        text = "${voice.dialect.flagEmoji} ${voice.dialect.titleArabic}",
                        color = Color(0xFFEADDFF),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2C2836)
                ) {
                    Text(
                        text = "${voice.category.emoji} ${voice.category.titleArabic}",
                        color = Color(0xFFE0F2FE),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2C2836)
                ) {
                    Text(
                        text = "${voice.gender.emoji} ${voice.gender.titleArabic}",
                        color = Color(voice.gender.colorHex),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Spoken Sample Phrase Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF17151D),
                border = BorderStroke(1.dp, Color(0xFF3B3847)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(9.dp)) {
                    Text(
                        text = "عينة صوتية للنطق:",
                        color = Color(0xFF938F99),
                        fontSize = 9.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "« ${voice.sampleArabicPhrase} »",
                        color = Color(0xFFF4EFF4),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    if (isAuditioning) {
                        Spacer(Modifier.height(6.dp))
                        AudioWaveformVisualizer(
                            amplitude = 0.85f,
                            isActive = true,
                            barCount = 32,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Acoustic Parameter Meters (Pitch, Warmth, Resonance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "طبقة الصوت: ${(voice.basePitch * 100).toInt()}%",
                        color = Color(0xFFCAC4D0),
                        fontSize = 9.5.sp
                    )
                    Text(
                        text = "• الدفء: ${(voice.toneWarmth * 100).toInt()}%",
                        color = Color(0xFFCAC4D0),
                        fontSize = 9.5.sp
                    )
                    Text(
                        text = "• الرنين: ${(voice.resonance * 100).toInt()}%",
                        color = Color(0xFFCAC4D0),
                        fontSize = 9.5.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Audition Button
                Button(
                    onClick = onPlayAudition,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAuditioning) Color(0xFFFF5252) else Color(0xFF4A4458)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isAuditioning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isAuditioning) "إيقاف" else "استماع",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isAuditioning) "إيقاف" else "استماع للعينة",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Apply to Dubbing Track
                Button(
                    onClick = onApplyToDubbing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "تعيين للدبلجة 🎬",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * AI Voice Matcher Modal
 * Analyzes audio / user pitch to recommend the top human voices from the thousands in the library.
 */
@Composable
fun AiVoiceMatcherModal(
    viewModel: DubbingViewModel,
    onDismiss: () -> Unit,
    onSelectMatchedVoice: (HumanVoiceModel) -> Unit
) {
    val state = viewModel.uiState.collectAsState().value
    val genderAnalysis = viewModel.genderAnalysisResult.collectAsState().value
    var isAnalyzing by remember { mutableStateOf(false) }
    var matchedVoices by remember {
        mutableStateOf<List<HumanVoiceModel>>(
            HumanVoiceLibraryRepository.matchVoiceByAcousticSignature(
                detectedPitchHz = genderAnalysis.pitchHz,
                detectedGenderArabic = state.lastDetectedGender.titleArabic
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B26)),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "مساعد المطابقة الصوتية الذكي (AI Voice Matcher)",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "إغلاق",
                                tint = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "يحلل الذكاء الاصطناعي نبرتك وطبقة الصوت (Hz) لاقتراح أفضل 5 ممثلين صوتيين يطابقون طبقتك وخامتك تماماً من بين آلاف الأصوات في المكتبة.",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    // Detected Acoustic Specs
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF14121A),
                        border = BorderStroke(1.dp, Color(0xFF4F378B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("التردد المقاس", color = Color(0xFF938F99), fontSize = 10.sp)
                                Text(
                                    text = String.format("%.0f Hz", genderAnalysis.pitchHz.coerceAtLeast(130f)),
                                    color = Color(0xFF00E5FF),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                                color = Color(0xFF49454F)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("الطبقة المقدرة", color = Color(0xFF938F99), fontSize = 10.sp)
                                Text(
                                    text = state.lastDetectedGender.titleArabic,
                                    color = Color(state.lastDetectedGender.colorHex),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "الأصوات البشرية الأكثر مطابقة لنبرتك 🎯:",
                        color = Color(0xFFEADDFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matchedVoices) { voice ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF272433),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectMatchedVoice(voice) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = voice.avatarEmoji, fontSize = 20.sp)
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

                                    Button(
                                        onClick = { onSelectMatchedVoice(voice) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                    ) {
                                        Text(
                                            text = "اختيار 🎙️",
                                            color = Color(0xFF00373E),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
