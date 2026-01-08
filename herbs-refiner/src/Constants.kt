package refiner

import com.osmb.api.item.ItemID

const val MAX_INVENTORY_SLOTS = 28
const val VERSION_URI = "https://raw.githubusercontent.com/999kiko/OSMB/refs/heads/main/herbs-refiner/VERSION"
const val VERSION = 1.01

enum class Herb(
    val herbName: String,
    val itemId: Int,
    val pasteType: PasteType,
    val pasteQuantity: Int,
) {
    GUAM("Guam leaf", ItemID.GUAM_LEAF, PasteType.MOX, 10),
    MARRENTIL("Marrentil", ItemID.MARRENTILL, PasteType.MOX, 13),
    TARROMIN("Tarromin", ItemID.TARROMIN, PasteType.MOX, 15),
    HARRALANDER("Harralander", ItemID.HARRALANDER, PasteType.MOX, 20),
    RANARR_WEED("Ranarr weed", ItemID.RANARR_WEED, PasteType.LYE, 26),
    TOADFLAX("Toadflax", ItemID.TOADFLAX, PasteType.LYE, 32),
    IRIT_LEAF("Irit leaf", ItemID.IRIT_LEAF, PasteType.AGA,30),
    AVANTOE("Avantoe", ItemID.AVANTOE, PasteType.LYE,30),
    KWUARM("Kwuarm", ItemID.KWUARM, PasteType.LYE, 33),
    HUASCA("Huasca", ItemID.HUASCA, PasteType.AGA,20),
    SNAPDRAGON("Snapdragon", ItemID.SNAPDRAGON, PasteType.LYE,40),
    CADANTINE("Cadantine", ItemID.CADANTINE, PasteType.AGA, 34),
    LANTADYME("Lantadyme", ItemID.LANTADYME, PasteType.AGA,40),
    DWARF_WEED("Dwarf weed", ItemID.DWARF_WEED, PasteType.AGA, 42),
    TORSTOL("Torstol", ItemID.TORSTOL, PasteType.AGA, 44);

    companion object {
        fun getByPaste(pasteType: PasteType): List<Herb> {
            return entries.filter { it.pasteType == pasteType }
        }
        fun getMox(): List<Herb>  {
            return getByPaste(PasteType.MOX)
        }
        fun getLye(): List<Herb>  {
            return getByPaste(PasteType.LYE)
        }
        fun getAga(): List<Herb>  {
            return getByPaste(PasteType.AGA)
        }
        fun getByItemId(itemId: Int): Herb? {
            return entries.find { it.itemId == itemId }
        }
    }
}

enum class PasteType {
    MOX,LYE,AGA
}

data class PasteTarget(
    val pasteType: PasteType,
    val herbsToUse: List<Herb>,
    val amount: Int
)

val PASTE_ITEM_IDS = setOf(ItemID.MOX_PASTE, ItemID.AGA_PASTE, ItemID.LYE_PASTE)
