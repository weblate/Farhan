package ly.com.tahaben.infinite_scroll_blocker_domain.use_cases

import ly.com.tahaben.infinite_scroll_blocker_domain.util.AccessibilityServiceUtils

class IsServiceEnabled(
    private val accessibilityUtil: AccessibilityServiceUtils
) {

    operator fun invoke(): Boolean {
        return accessibilityUtil.checkIfServiceEnabled()
    }
}