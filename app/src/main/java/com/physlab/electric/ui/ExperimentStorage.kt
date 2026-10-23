package com.physlab.electric.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class MeasurementRecord(
    val createdAt: Long,
    val sourceVoltage: Float,
    val totalCurrent: Float,
    val voltmeterValues: List<Float> = emptyList(),
    val ammeterValues: List<Float> = emptyList(),
)

data class SavedSnapshot(
    val id: Long,
    val createdAt: Long,
    val name: String,
    val payload: String,
    val componentCount: Int,
    val wireCount: Int,
)

object SnapshotStore {
    private const val PREFS = "electric_lab_snapshots"
    private const val KEY = "snapshots"

    fun load(context: Context): List<SavedSnapshot> {
        return runCatching {
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY, "[]") ?: "[]"
            val array = JSONArray(raw)
            val result = mutableListOf<SavedSnapshot>()
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val payload = item.optString("payload")
                if (payload.isBlank()) continue
                result += SavedSnapshot(
                    id = item.optLong("id", index.toLong()),
                    createdAt = item.optLong("createdAt", 0L),
                    name = item.optString("name", "实验快照"),
                    payload = payload,
                    componentCount = item.optInt("componentCount", 0),
                    wireCount = item.optInt("wireCount", 0),
                )
            }
            result
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, snapshots: List<SavedSnapshot>) {
        runCatching {
            val array = JSONArray()
            for (snapshot in snapshots) {
                array.put(
                    JSONObject().apply {
                        put("id", snapshot.id)
                        put("createdAt", snapshot.createdAt)
                        put("name", snapshot.name)
                        put("payload", snapshot.payload)
                        put("componentCount", snapshot.componentCount)
                        put("wireCount", snapshot.wireCount)
                    },
                )
            }
            context.applicationContext
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, array.toString())
                .apply()
        }
    }
}
