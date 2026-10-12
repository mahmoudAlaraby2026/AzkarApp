# Optimization Plan for AzkarApp

This plan implements three key optimizations to improve Google Play ratings, app size, and user experience:
1.  **Splash Screen API**: Adding modern splash screen support.
2.  **Modern Theme**: Updating to Material3 theme.
3.  **Localization Optimization**: Restricting resources to Arabic to save space.

## Proposed Changes

### [Component] Dependencies & Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Mahmoud/AndroidStudioProjects/AzkarApp/gradle/libs.versions.toml)
- Add `androidx-core-splashscreen` version and library definition.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Mahmoud/AndroidStudioProjects/AzkarApp/androidApp/build.gradle.kts)
- Add `resConfigs("ar")` to `defaultConfig`.
- Add `androidx.core.splashscreen` dependency.

### [Component] Android Resources

#### [NEW] [themes.xml](file:///C:/Users/Mahmoud/AndroidStudioProjects/AzkarApp/androidApp/src/main/res/values/themes.xml)
- Define a new theme `Theme.App` inheriting from `Theme.Material3.DayNight.NoActionBar`.
- Configure the splash screen theme.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Mahmoud/AndroidStudioProjects/AzkarApp/androidApp/src/main/AndroidManifest.xml)
- Update `android:theme` to `@style/Theme.App`.

### [Component] Android Code

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Mahmoud/AndroidStudioProjects/AzkarApp/androidApp/src/main/kotlin/com/mahmoud/azkarapp/MainActivity.kt)
- Call `installSplashScreen()` before `super.onCreate()`.

## Verification Plan

### Automated Tests
- Gradle Sync to verify dependencies.
- Build the app to ensure no resource or theme conflicts.

### Manual Verification
- Deploy to an Android device/emulator.
- Verify that the splash screen appears correctly.
- Verify that the app uses the new Material3 theme.
- Check APK size (optional but recommended) to see the impact of `resConfigs`.
