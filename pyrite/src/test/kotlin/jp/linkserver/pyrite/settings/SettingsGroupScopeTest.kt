package jp.linkserver.pyrite.settings

import org.junit.Test

class SettingsGroupScopeTest {
    @Test(expected = IllegalArgumentException::class)
    fun duplicateVisibleKeysAreRejected() {
        SettingsGroupScope().apply {
            item("same") {}
            item("same") {}
        }
    }

}
