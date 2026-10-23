package com.physlab.electric.ui

import android.content.Context

object ChallengeProgressStore {
    private const val PREFS = "challenge_progress"
    private const val VERSION_KEY = "progress_version"

    fun load(context: Context): Map<Int, Int> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ChallengeTask.entries.mapNotNull { task ->
            val score = prefs.getInt(task.name, 0)
            if (score > 0) task.ordinal to score else null
        }.toMap()
    }

    fun save(context: Context, scores: Map<Int, Int>) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(VERSION_KEY, 1)
        for (task in ChallengeTask.entries) {
            editor.putInt(task.name, scores[task.ordinal] ?: 0)
        }
        editor.commit()
    }
}
