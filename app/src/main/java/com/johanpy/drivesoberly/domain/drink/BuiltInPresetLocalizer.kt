package com.johanpy.drivesoberly.domain.drink

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.johanpy.drivesoberly.R
import java.util.Locale
import kotlin.math.abs

object BuiltInPresetLocalizer {
    private const val DOUBLE_EPSILON = 0.001

    private data class BuiltInPresetSpec(
        val volume: Double,
        val degree: Double,
        val emoji: String,
        val nameResId: Int,
    )

    private val supportedLanguageTags = listOf("en", "fr-FR", "de-DE", "es-ES", "it-IT")

    private val builtInPresetSpecs =
        listOf(
            BuiltInPresetSpec(130.0, 13.0, "🍷", R.string.preset_red_wine),
            BuiltInPresetSpec(250.0, 4.5, "🍺", R.string.preset_light_beer),
            BuiltInPresetSpec(500.0, 4.5, "🍺", R.string.preset_light_beer),
            BuiltInPresetSpec(330.0, 9.0, "🍺", R.string.preset_triple_beer),
            BuiltInPresetSpec(250.0, 2.5, "🍺", R.string.preset_soft_cider),
            BuiltInPresetSpec(80.0, 17.0, "🥃", R.string.preset_martini),
            BuiltInPresetSpec(80.0, 30.0, "🥃", R.string.preset_whisky),
        )

    fun localizedNameOrNull(
        context: Context,
        volume: Double,
        degree: Double,
        emoji: String,
    ): String? =
        builtInPresetSpecs
            .firstOrNull { spec ->
                abs(spec.volume - volume) < DOUBLE_EPSILON &&
                    abs(spec.degree - degree) < DOUBLE_EPSILON &&
                    spec.emoji == emoji
            }?.let { spec ->
                context.getString(spec.nameResId)
            }

    fun shouldRelocalizeStoredName(
        context: Context,
        currentName: String,
        volume: Double,
        degree: Double,
        emoji: String,
    ): Boolean {
        val spec =
            builtInPresetSpecs.firstOrNull {
                abs(it.volume - volume) < DOUBLE_EPSILON &&
                    abs(it.degree - degree) < DOUBLE_EPSILON &&
                    it.emoji == emoji
            } ?: return false

        return supportedLocalizedNames(context, spec.nameResId).contains(currentName)
    }

    private fun supportedLocalizedNames(
        context: Context,
        nameResId: Int,
    ): Set<String> =
        supportedLanguageTags
            .map { languageTag -> localizedString(context, nameResId, languageTag) }
            .toSet()

    private fun localizedString(
        context: Context,
        nameResId: Int,
        languageTag: String,
    ): String {
        val configuration = Configuration(context.resources.configuration)
        val locale = Locale.forLanguageTag(languageTag)
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration).getString(nameResId)
    }
}