package com.physlab.electric.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class UserTemplate(
    val id: Long,
    val name: String,
    val grade: String,
    val category: String,
    val payload: String,
    val createdAt: Long,
)

object UserTemplateStore {
    private const val PREFS = "electric_lab_user_templates"
    private const val KEY = "templates"

    fun load(context: Context): List<UserTemplate> = runCatching {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val array = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val payload = item.optString("payload")
                if (payload.isBlank()) continue
                add(
                    UserTemplate(
                        id = item.optLong("id", index.toLong()),
                        name = item.optString("name", "我的模板"),
                        grade = item.optString("grade", "自定义"),
                        category = item.optString("category", "自定义"),
                        payload = payload,
                        createdAt = item.optLong("createdAt", 0L),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())

    fun add(context: Context, name: String, grade: String, category: String, payload: String): UserTemplate {
        val current = load(context)
        val template = UserTemplate(
            id = (current.maxOfOrNull { it.id } ?: 0L) + 1L,
            name = name,
            grade = grade,
            category = category,
            payload = payload,
            createdAt = System.currentTimeMillis(),
        )
        save(context, current + template)
        return template
    }

    fun remove(context: Context, id: Long) {
        save(context, load(context).filterNot { it.id == id })
    }

    fun save(context: Context, templates: List<UserTemplate>) {
        val array = JSONArray()
        for (template in templates) {
            array.put(
                JSONObject().apply {
                    put("id", template.id)
                    put("name", template.name)
                    put("grade", template.grade)
                    put("category", template.category)
                    put("payload", template.payload)
                    put("createdAt", template.createdAt)
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
