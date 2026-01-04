package refiner

import com.osmb.api.ScriptCore
import com.osmb.api.ui.component.tabs.skill.SkillType

data class PaintData(
    private val core: ScriptCore,
    private val startTime: Long = System.currentTimeMillis()
) {

    private val skill = SkillType.HERBLORE

    fun getXpGained(): Int {
        return core.xpTrackers[skill]?.xpGained?.toInt() ?: 0
    }

    fun getXpPerHour(): Int {
        return core.xpTrackers[skill]?.xpPerHour ?: 0
    }

    fun getFormattedRuntime(): String {
        val elapsed = System.currentTimeMillis() - startTime

        val totalSeconds = elapsed / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
