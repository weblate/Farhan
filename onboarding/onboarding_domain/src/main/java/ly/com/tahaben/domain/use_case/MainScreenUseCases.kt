package ly.com.tahaben.domain.use_case

data class MainScreenUseCases(
    val getDarkModePreference: GetDarkModePreference,
    val saveDarkModePreference: SaveDarkModePreference,
    val isMainSwitchEnabled: IsMainSwitchState,
    val setMainSwitchState: SetMainSwitchState
)
