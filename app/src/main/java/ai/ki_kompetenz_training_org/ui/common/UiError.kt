package ai.ki_kompetenz_training_org.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R

/**
 * Typed, locale-independent UI errors. ViewModels emit these; screens map
 * them to localized strings via [uiErrorMessage] (ViewModels must not hold
 * localized text so that English users never see German error messages).
 */
enum class UiError {
    QUIZ_LOAD,
    SRS_LOAD,
    SRS_SAVE,
    TEAM_LOAD,
    LESSON_LOAD,
}

/** Maps a [UiError] to the localized message in the current composition locale. */
@Composable
fun uiErrorMessage(error: UiError): String = stringResource(
    when (error) {
        UiError.QUIZ_LOAD -> R.string.error_quiz_load
        UiError.SRS_LOAD -> R.string.error_srs_load
        UiError.SRS_SAVE -> R.string.error_srs_save
        UiError.TEAM_LOAD -> R.string.error_team_load
        UiError.LESSON_LOAD -> R.string.error_lesson_load
    },
)

