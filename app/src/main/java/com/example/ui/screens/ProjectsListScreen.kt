package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DubbingProject
import com.example.model.DubbingClip
import com.example.model.SampleClipsRepository
import com.example.ui.DubbingViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GalleryLayoutMode {
    GRID,
    LIST
}

@Composable
fun ProjectsListScreen(
    viewModel: DubbingViewModel,
    onOpenProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allSavedProjects.collectAsStateWithLifecycle()
    val playingProjectId by viewModel.previewPlayingProjectId.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var layoutMode by remember { mutableStateOf(GalleryLayoutMode.GRID) }

    // Dialog States
    var projectToRename by remember { mutableStateOf<DubbingProject?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<DubbingProject?>(null) }

    // Filter projects based on search query and category
    val filteredProjects = remember(projects, searchQuery, selectedCategoryFilter) {
        projects.filter { proj ->
            val matchesQuery = searchQuery.isBlank() ||
                    proj.title.contains(searchQuery, ignoreCase = true) ||
                    proj.clipTitle.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "الكل" -> true
                "مسجل فقط 🎙️" -> !proj.recordedAudioPath.isNullOrBlank() && File(proj.recordedAudioPath).exists()
                else -> {
                    val clip = SampleClipsRepository.clips.find { it.id == proj.clipId }
                    clip?.category == selectedCategoryFilter
                }
            }
            matchesQuery && matchesCategory
        }
    }

    val categories = listOf("الكل", "مسجل فقط 🎙️", "كرتون", "وثائقي", "خيال علمي", "كوميدي", "رياضة")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141218))
    ) {
        if (layoutMode == GalleryLayoutMode.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header & Stats
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProjectsGalleryHeader(
                        totalProjectsCount = projects.size,
                        layoutMode = layoutMode,
                        onToggleLayout = { layoutMode = it },
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        categories = categories,
                        selectedCategory = selectedCategoryFilter,
                        onSelectCategory = { selectedCategoryFilter = it }
                    )
                }

                if (filteredProjects.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyProjectsGalleryState(
                            hasSearch = searchQuery.isNotBlank() || selectedCategoryFilter != "الكل",
                            onResetFilters = {
                                searchQuery = ""
                                selectedCategoryFilter = "الكل"
                            }
                        )
                    }
                } else {
                    items(filteredProjects, key = { it.id }) { project ->
                        val clip = SampleClipsRepository.getClipById(project.clipId)
                        val isPlaying = playingProjectId == project.id

                        ProjectThumbnailCard(
                            project = project,
                            clip = clip,
                            isPlaying = isPlaying,
                            onPlayPausePreview = { viewModel.previewProjectAudio(project) },
                            onOpen = {
                                viewModel.loadProject(project)
                                onOpenProject()
                            },
                            onShare = { viewModel.shareProject(project) },
                            onExport = {
                                viewModel.loadProject(project)
                                viewModel.openExportDialog()
                            },
                            onDuplicate = { viewModel.duplicateProject(project) },
                            onRename = {
                                projectToRename = project
                                renameInputText = project.title
                            },
                            onDelete = { projectToDelete = project }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProjectsGalleryHeader(
                        totalProjectsCount = projects.size,
                        layoutMode = layoutMode,
                        onToggleLayout = { layoutMode = it },
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        categories = categories,
                        selectedCategory = selectedCategoryFilter,
                        onSelectCategory = { selectedCategoryFilter = it }
                    )
                }

                if (filteredProjects.isEmpty()) {
                    item {
                        EmptyProjectsGalleryState(
                            hasSearch = searchQuery.isNotBlank() || selectedCategoryFilter != "الكل",
                            onResetFilters = {
                                searchQuery = ""
                                selectedCategoryFilter = "الكل"
                            }
                        )
                    }
                } else {
                    items(filteredProjects, key = { it.id }) { project ->
                        val clip = SampleClipsRepository.getClipById(project.clipId)
                        val isPlaying = playingProjectId == project.id

                        ProjectListThumbnailCard(
                            project = project,
                            clip = clip,
                            isPlaying = isPlaying,
                            onPlayPausePreview = { viewModel.previewProjectAudio(project) },
                            onOpen = {
                                viewModel.loadProject(project)
                                onOpenProject()
                            },
                            onShare = { viewModel.shareProject(project) },
                            onExport = {
                                viewModel.loadProject(project)
                                viewModel.openExportDialog()
                            },
                            onDuplicate = { viewModel.duplicateProject(project) },
                            onRename = {
                                projectToRename = project
                                renameInputText = project.title
                            },
                            onDelete = { projectToDelete = project }
                        )
                    }
                }
            }
        }

        // Rename Dialog
        projectToRename?.let { proj ->
            AlertDialog(
                onDismissRequest = { projectToRename = null },
                title = {
                    Text(
                        text = "إعادة تسمية المشروع ✏️",
                        color = Color(0xFFE6E1E5),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "أدخل الاسم الجديد لمشروع الدبلجة:",
                            color = Color(0xFFCAC4D0),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = renameInputText,
                            onValueChange = { renameInputText = it },
                            placeholder = { Text("اسم المشروع...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedTextColor = Color(0xFFE6E1E5),
                                unfocusedTextColor = Color(0xFFE6E1E5)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameInputText.isNotBlank()) {
                                viewModel.renameProject(proj, renameInputText)
                            }
                            projectToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        )
                    ) {
                        Text("حفظ التعديل", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToRename = null }) {
                        Text("إلغاء", color = Color(0xFFCAC4D0))
                    }
                },
                containerColor = Color(0xFF2B2930)
            )
        }

        // Delete Confirmation Dialog
        projectToDelete?.let { proj ->
            AlertDialog(
                onDismissRequest = { projectToDelete = null },
                title = {
                    Text(
                        text = "حذف مشروع الدبلجة؟ 🗑️",
                        color = Color(0xFFF2B8B5),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "هل أنت متأكد من رغبتك في حذف \"${proj.title}\"؟ سيتم حذف جميع التسجيلات الصوتية المرتبطة به نهائياً.",
                        color = Color(0xFFE6E1E5),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProject(proj)
                            projectToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2B8B5),
                            contentColor = Color(0xFF601410)
                        )
                    ) {
                        Text("نعم، حذف", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text("إلغاء", color = Color(0xFFCAC4D0))
                    }
                },
                containerColor = Color(0xFF2B2930)
            )
        }
    }
}

@Composable
fun ProjectsGalleryHeader(
    totalProjectsCount: Int,
    layoutMode: GalleryLayoutMode,
    onToggleLayout: (GalleryLayoutMode) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Title & Gallery Layout Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFD0BCFF),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "معرض مشاريع الدبلجة",
                            color = Color(0xFFE6E1E5),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4A4458)
                        ) {
                            Text(
                                text = "$totalProjectsCount",
                                color = Color(0xFFD0BCFF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "الوصول السريع، المعاينة المصغرة، وإدارة الدبلجة السابقة",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp
                    )
                }
            }

            // Grid / List Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2B2930),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    IconButton(
                        onClick = { onToggleLayout(GalleryLayoutMode.GRID) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (layoutMode == GalleryLayoutMode.GRID) Color(0xFFD0BCFF) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "عرض شبكي",
                            tint = if (layoutMode == GalleryLayoutMode.GRID) Color(0xFF381E72) else Color(0xFFCAC4D0),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onToggleLayout(GalleryLayoutMode.LIST) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (layoutMode == GalleryLayoutMode.LIST) Color(0xFFD0BCFF) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "عرض قائمة",
                            tint = if (layoutMode == GalleryLayoutMode.LIST) Color(0xFF381E72) else Color(0xFFCAC4D0),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث في مشاريع الدبلجة أو اسم المشهد...", color = Color(0xFF938F99), fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = Color(0xFFCAC4D0),
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color(0xFF49454F),
                focusedTextColor = Color(0xFFE6E1E5),
                unfocusedTextColor = Color(0xFFE6E1E5),
                focusedContainerColor = Color(0xFF24222B),
                unfocusedContainerColor = Color(0xFF24222B)
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("projects_search_bar")
        )

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF2B2930),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)
                    ),
                    modifier = Modifier
                        .clickable { onSelectCategory(cat) }
                        .testTag("filter_chip_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color(0xFF381E72) else Color(0xFFE6E1E5),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual Thumbnail Card for Grid View (Gallery 2-column)
 */
@Composable
fun ProjectThumbnailCard(
    project: DubbingProject,
    clip: DubbingClip,
    isPlaying: Boolean,
    onPlayPausePreview: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit = {},
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasRecording = !project.recordedAudioPath.isNullOrBlank() && File(project.recordedAudioPath).exists()
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale("ar")).format(Date(project.lastModified))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_grid_card_${project.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF24222B)),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Color(0xFFD0BCFF) else Color(0xFF49454F).copy(alpha = 0.7f)
        )
    ) {
        Column {
            // Visual Artwork / Thumbnail Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(clip.primaryColor).copy(alpha = 0.85f),
                                Color(0xFF1E1B24)
                            )
                        )
                    )
            ) {
                // Background Decorative Waveform or Pattern
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = clip.coverEmoji,
                        fontSize = 36.sp
                    )
                }

                // Top Category & Duration Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = clip.category,
                            color = Color(0xFFFFD993),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${project.durationSeconds}ث",
                            color = Color(0xFFE6E1E5),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Bottom Left: Quick Play Floating Button (if recording exists)
                if (hasRecording) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) Color(0xFFF2B8B5) else Color(0xFFD0BCFF),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .size(32.dp)
                            .clickable { onPlayPausePreview() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف" else "تشغيل المعاينة",
                                tint = if (isPlaying) Color(0xFF601410) else Color(0xFF381E72),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom Right: Voice Effect Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasRecording) "🎙️ مسجل" else "📝 مسودة",
                            color = if (hasRecording) Color(0xFFA6D4A8) else Color(0xFFCAC4D0),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Thumbnail Meta Content
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        color = Color(0xFFE6E1E5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Options Dropdown
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF2B2930))
                        ) {
                            DropdownMenuItem(
                                text = { Text("فتح في الاستوديو 🎬", color = Color(0xFFE6E1E5), fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onOpen()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("حفظ وتصدير الفيديو 💾", color = Color(0xFFD0BCFF), fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onExport()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("مشاركة الدبلجة 📤", color = Color(0xFFD0E4FF), fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onShare()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("نسخ المشروع 📋", color = Color(0xFFE6E1E5), fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("إعادة التسمية ✏️", color = Color(0xFFE6E1E5), fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onRename()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("حذف المشروع 🗑️", color = Color(0xFFF2B8B5), fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "${project.clipTitle} • $dateStr",
                    color = Color(0xFF938F99),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                // One-Tap Studio Access Button
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF383540),
                        contentColor = Color(0xFFD0BCFF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "تعديل في الاستوديو",
                        color = Color(0xFFD0BCFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Visual Thumbnail Card for List View
 */
@Composable
fun ProjectListThumbnailCard(
    project: DubbingProject,
    clip: DubbingClip,
    isPlaying: Boolean,
    onPlayPausePreview: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit = {},
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasRecording = !project.recordedAudioPath.isNullOrBlank() && File(project.recordedAudioPath).exists()
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ar")).format(Date(project.lastModified))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_list_card_${project.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF24222B)),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Color(0xFFD0BCFF) else Color(0xFF49454F).copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Thumbnail Box
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(clip.primaryColor).copy(alpha = 0.9f),
                                Color(0xFF2B2930)
                            )
                        )
                    )
                    .clickable {
                        if (hasRecording) onPlayPausePreview() else onOpen()
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = clip.coverEmoji, fontSize = 24.sp)
                }

                if (hasRecording) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) Color(0xFFF2B8B5) else Color(0xFFD0BCFF).copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isPlaying) Color(0xFF601410) else Color(0xFF381E72),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Center Project Meta
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = project.title,
                    color = Color(0xFFE6E1E5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${project.clipTitle} • $dateStr",
                    color = Color(0xFFCAC4D0),
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E1B24)
                    ) {
                        Text(
                            text = if (hasRecording) "🎙️ مسجل" else "📝 مسودة",
                            color = if (hasRecording) Color(0xFFA6D4A8) else Color(0xFF938F99),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E1B24)
                    ) {
                        Text(
                            text = "${project.durationSeconds} ثانية",
                            color = Color(0xFFD0BCFF),
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Right Quick Actions & Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Share Icon
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = Color(0xFFD0E4FF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Options Menu
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "المزيد",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF2B2930))
                    ) {
                        DropdownMenuItem(
                            text = { Text("فتح في الاستوديو 🎬", color = Color(0xFFE6E1E5), fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حفظ وتصدير الفيديو 💾", color = Color(0xFFD0BCFF), fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onExport()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("نسخ المشروع 📋", color = Color(0xFFE6E1E5), fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("إعادة التسمية ✏️", color = Color(0xFFE6E1E5), fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف المشروع 🗑️", color = Color(0xFFF2B8B5), fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty Gallery Placeholder
 */
@Composable
fun EmptyProjectsGalleryState(
    hasSearch: Boolean,
    onResetFilters: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF24222B)),
        border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF383540),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (hasSearch) "🔍" else "🎬", fontSize = 30.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (hasSearch) "لم يتم العثور على نتائج مطابقة" else "لا توجد مشاريع دبلجة بعد",
                color = Color(0xFFE6E1E5),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (hasSearch)
                    "جرب البحث بكلمات أخرى أو قم بإلغاء الفلتر الحالي"
                else
                    "اختر أي مشهد من مكتبة المشاهد وابدأ دبلجة أول مقطع بصوتك ثم احفظه هنا!",
                color = Color(0xFFCAC4D0),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (hasSearch) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFF)),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إعادة تعيين الفلاتر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
