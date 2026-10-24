package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.EnglishLevel
import com.example.data.model.GrammarCorrection
import com.example.data.model.ThemeColorOption
import com.example.data.model.ThemeModeOption
import com.example.data.model.VocabularySuggestion
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val grammarListType = Types.newParameterizedType(List::class.java, GrammarCorrection::class.java)
    private val grammarListAdapter = moshi.adapter<List<GrammarCorrection>>(grammarListType)

    private val vocabListType = Types.newParameterizedType(List::class.java, VocabularySuggestion::class.java)
    private val vocabListAdapter = moshi.adapter<List<VocabularySuggestion>>(vocabListType)

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.let { stringListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        return if (json.isNullOrEmpty()) emptyList() else try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromGrammarList(list: List<GrammarCorrection>?): String {
        return list?.let { grammarListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toGrammarList(json: String?): List<GrammarCorrection> {
        return if (json.isNullOrEmpty()) emptyList() else try {
            grammarListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromVocabList(list: List<VocabularySuggestion>?): String {
        return list?.let { vocabListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toVocabList(json: String?): List<VocabularySuggestion> {
        return if (json.isNullOrEmpty()) emptyList() else try {
            vocabListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromEnglishLevel(level: EnglishLevel?): String = level?.name ?: EnglishLevel.BEGINNER.name

    @TypeConverter
    fun toEnglishLevel(name: String?): EnglishLevel = try {
        EnglishLevel.valueOf(name ?: EnglishLevel.BEGINNER.name)
    } catch (e: Exception) {
        EnglishLevel.BEGINNER
    }

    @TypeConverter
    fun fromThemeColor(color: ThemeColorOption?): String = color?.name ?: ThemeColorOption.BLUE.name

    @TypeConverter
    fun toThemeColor(name: String?): ThemeColorOption = try {
        ThemeColorOption.valueOf(name ?: ThemeColorOption.BLUE.name)
    } catch (e: Exception) {
        ThemeColorOption.BLUE
    }

    @TypeConverter
    fun fromThemeMode(mode: ThemeModeOption?): String = mode?.name ?: ThemeModeOption.SYSTEM.name

    @TypeConverter
    fun toThemeMode(name: String?): ThemeModeOption = try {
        ThemeModeOption.valueOf(name ?: ThemeModeOption.SYSTEM.name)
    } catch (e: Exception) {
        ThemeModeOption.SYSTEM
    }
}
