package com.example.ui

import com.example.audio.AudioEffectItem
import com.example.audio.AudioEffectParameters
import com.example.audio.BgmStyle
import com.example.audio.VoiceEffect
import com.example.model.ScriptLine
import com.example.ui.components.VoicePresetType

/**
 * Represents an undoable/redoable state snapshot in the Dubbing Studio editor.
 */
data class EditorStateSnapshot(
    val actionDescription: String,
    val recordedAudioPath: String?,
    val selectedVoiceEffect: VoiceEffect,
    val selectedVoicePreset: VoicePresetType,
    val selectedAudioEffectItem: AudioEffectItem,
    val customEffectParams: AudioEffectParameters,
    val originalVolume: Float,
    val dubVolume: Float,
    val bgmVolume: Float,
    val selectedBgmStyle: BgmStyle,
    val scriptLines: List<ScriptLine>,
    val trimmerAudioPath: String?,
    val trimmerStartSeconds: Float,
    val trimmerEndSeconds: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Thread-safe Undo/Redo Stack Manager for editing operations.
 */
class UndoRedoStackManager(private val maxStackSize: Int = 30) {
    private val undoStack = ArrayDeque<EditorStateSnapshot>()
    private val redoStack = ArrayDeque<EditorStateSnapshot>()

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun getUndoCount(): Int = undoStack.size
    fun getRedoCount(): Int = redoStack.size

    fun peekUndo(): EditorStateSnapshot? = undoStack.lastOrNull()
    fun peekRedo(): EditorStateSnapshot? = redoStack.lastOrNull()

    /**
     * Push current state before making a new modification.
     * Clears redo stack because a new action branches from here.
     */
    fun pushUndo(snapshot: EditorStateSnapshot) {
        if (undoStack.size >= maxStackSize) {
            undoStack.removeFirst()
        }
        undoStack.addLast(snapshot)
        redoStack.clear()
    }

    /**
     * Executes an undo:
     * - Takes current state and pushes to redoStack
     * - Pops previous state from undoStack and returns it
     */
    fun performUndo(currentState: EditorStateSnapshot): EditorStateSnapshot? {
        if (undoStack.isEmpty()) return null
        
        // Push current state to redo
        if (redoStack.size >= maxStackSize) {
            redoStack.removeFirst()
        }
        redoStack.addLast(currentState)

        // Pop last state from undo
        return undoStack.removeLast()
    }

    /**
     * Executes a redo:
     * - Takes current state and pushes to undoStack
     * - Pops next state from redoStack and returns it
     */
    fun performRedo(currentState: EditorStateSnapshot): EditorStateSnapshot? {
        if (redoStack.isEmpty()) return null

        // Push current state to undo
        if (undoStack.size >= maxStackSize) {
            undoStack.removeFirst()
        }
        undoStack.addLast(currentState)

        // Pop last state from redo
        return redoStack.removeLast()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
