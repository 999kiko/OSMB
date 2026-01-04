package refiner

import com.osmb.api.scene.RSObject
import com.osmb.api.script.Script
import com.osmb.api.script.ScriptDefinition
import com.osmb.api.script.SkillCategory
import com.osmb.api.utils.RandomUtils
import com.osmb.api.visual.drawing.Canvas
import refiner.ui.UI
import java.awt.Color
import java.awt.Font

@ScriptDefinition(
    name = "Herb Refiner",
    author = "Kiko",
    version = VERSION,
    description = "Refines herbs for the mixology minigame",
    skillCategory = SkillCategory.HERBLORE,
)
class Refiner(scriptCore: Any) : Script(scriptCore) {

    private var targets = listOf<PasteTarget>()
    private val herbsToWithdraw = mutableMapOf<Herb, Int>()
    private var herbsWithdrawn = mutableMapOf<Herb, Int>()
    private val paintData = PaintData(this)

    override fun onStart() {
        val remoteVersion = VersionChecker.getVersion()

        if (remoteVersion != null && VERSION != remoteVersion) {
            log("There is a new version ($remoteVersion) available, please download and restart.")
            stop()
        }

        val ui = UI()

        val scene = ui.createScene()

        stageController.show(scene, "Herb refiner", false)

        if (ui.getTargets().isEmpty()) {
            log("Invalid targets selected")
            stop()
        }

        targets = ui.getTargets()
    }

    override fun poll(): Int {
        if (herbsToWithdraw.isEmpty()) {
            calculateHerbsNeeded()
            return 500
        }

        val itemIds = herbsToWithdraw.keys.map { it.itemId }.toSet()
        val inventory = widgetManager.inventory.search(itemIds) ?: return -1

        val isFinished = herbsWithdrawn.values.all { it == 0 }
        if (isFinished && !inventory.containsAny(itemIds)) {
            log("Finished!")
            widgetManager?.logoutTab?.logout()
            stop()
        }

        if (!inventory.containsAny(itemIds)) {
            useBank()
            return 500
        }

        // Inventory contains the herbs, use refiner
        val refiner = objectManager.getRSObject {
            it.name?.equals("Refiner") == true
        } ?: return -1

        val tapResponse = finger.tapGetResponse(true, refiner.convexHull.getResized(0.5))
        if (!tapResponse?.action.equals("Operate", true)) {
            // action missed, return earlier
            return RandomUtils.weightedRandom(200, 600)
        }

        return RandomUtils.weightedRandom(300, 900)
    }

    private fun getBankObject(): RSObject? {
        return objectManager.getRSObject {
            it.name?.equals("Bank Chest") == true && it.actions?.contains("Use") == true
        }
    }

    private fun useBank() {
        val bankObject = getBankObject() ?: return

        if (!widgetManager.bank.isVisible) {
            bankObject.interact("Use")
            pollFramesUntil({ widgetManager.bank.isVisible }, 3000)
            return
        }

        val itemIds = herbsToWithdraw.keys.map { it.itemId }.toSet() + PASTE_ITEM_IDS
        val inventory = widgetManager.inventory.search(itemIds)

        // Deposit inventory
        if (inventory.containsAny(itemIds) && !widgetManager.bank.depositAll(emptySet())) {
            pollFramesUntil({ false }, RandomUtils.uniformRandom(500, 1200))
            return
        }

        val bank = widgetManager.bank.search(itemIds)

        var interactionFailed = false
        var amountWithdrawn = 0

        val remainingHerbs = herbsWithdrawn.filter { it.value > 0 }.toList().sortedBy { it.second }.toMap()

        for ((herb, amount) in remainingHerbs) {
            val herbId = herb.itemId
            bank.getItem(herbId) ?: continue

            val amountToWithdraw = minOf(amount, MAX_INVENTORY_SLOTS - amountWithdrawn)

            interactionFailed = !widgetManager.bank.withdraw(herbId, amountToWithdraw)

            // withdraw
            if (!interactionFailed) {
                herbsWithdrawn[herb] = maxOf(0, amount - amountToWithdraw)
                pollFramesUntil({ false }, RandomUtils.uniformRandom(500, 1200))

                amountWithdrawn += amountToWithdraw

                if (amountWithdrawn == MAX_INVENTORY_SLOTS) {
                    widgetManager.bank.close()
                    break
                }
            } else {
                // deposit all again as a failsafe, probably want to have some sort of retry mechanism here
                widgetManager.bank.depositAll(emptySet())
                break
            }
        }

        if (!interactionFailed) {
            widgetManager.bank.close()
        }
    }

    private fun calculateHerbsNeeded() {
        val bankObject = getBankObject() ?: return

        if (!widgetManager.bank.isVisible) {
            bankObject.interact("Use")
            pollFramesUntil({ widgetManager.bank.isVisible }, RandomUtils.uniformRandom(700, 2000))
            return
        }

        // Deposit everything to bank
        widgetManager.bank.depositAll(emptySet())

        val mappedTargets = targets.associate { it.pasteType to it.amount }

        val itemIds = targets.flatMap { it.herbsToUse }.mapTo(mutableSetOf()) { it.itemId }
        val bankItemGroup = widgetManager.bank.search(itemIds) ?: return

        val availableAmounts = mutableMapOf<PasteType, Int>()

        for (item in bankItemGroup.recognisedItems) {
            val herb = Herb.getByItemId(item.id) ?: continue

            val pasteNeeded = mappedTargets[herb.pasteType] ?: 0
            val available = availableAmounts[herb.pasteType] ?: 0

            if (available >= pasteNeeded) {
                continue
            }

            val remaining = pasteNeeded - available
            val herbsNeeded = (remaining + herb.pasteQuantity - 1) / herb.pasteQuantity
            val herbsAvailable = item.stackAmount
            val herbsToUse = minOf(herbsNeeded, herbsAvailable)

            val pasteAmount = herbsToUse * herb.pasteQuantity

            log("[${herb.herbName}]: Using $herbsToUse herbs (${pasteAmount} paste)")

            availableAmounts[herb.pasteType] = available + pasteAmount
            herbsToWithdraw[herb] = herbsToUse
        }

        for ((pasteType, amount) in availableAmounts) {
            val pasteNeeded = mappedTargets[pasteType] ?: 0

            if (amount < pasteNeeded) {
                log("Insufficient amount in bank for $pasteType")
                stop()
            }
        }

        herbsWithdrawn = herbsToWithdraw
    }

    override fun promptBankTabDialogue(): Boolean {
        return true
    }

    override fun regionsToPrioritise(): IntArray {
        return intArrayOf(5521)
    }

    override fun onPaint(c: Canvas) {
        val fontBold = Font("Arial", Font.BOLD, 13)
        val font = Font("Arial", Font.PLAIN, 12)

        val width = targets.count() * 90

        c.fillRect(5, 40, width, 100, Color.BLACK.rgb, 1.0)
        c.drawRect(5, 40, width, 100, Color.GREEN.rgb)

        c.drawText("Herb Refiner by Kiko", 10, 55, Color.WHITE.rgb, fontBold)
        c.drawText("Ver: $VERSION", 10, 70, Color.WHITE.rgb, font)

        val targetString = targets.map { it.pasteType to it.amount }.joinToString(" | ") { (pasteType, amount) ->
            "$pasteType: $amount"
        }

        c.drawText("Time running: ${paintData.getFormattedRuntime()}", 11, 90, Color.WHITE.rgb, font)
        c.drawText("Targets: $targetString", 11, 105, Color.WHITE.rgb, font)

        c.drawText("XP gained: ${paintData.getXpGained()}", 11, 120, Color.WHITE.rgb, font)
        c.drawText("XP/hr: ${paintData.getXpPerHour()}", 11, 135, Color.WHITE.rgb, font)
    }
}