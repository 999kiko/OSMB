package refiner.ui

import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.stage.Stage
import refiner.*

class UI {
    private val targets = mutableListOf<PasteTarget>()

    fun createScene(): Scene {
        val root = VBox(15.0).apply {
            padding = Insets(20.0)
            alignment = Pos.TOP_CENTER
            style = "-fx-background-color: #242a2c;"
        }

        val moxLabel = Label("Select mox herbs").apply {
            styleClass.add("label")
        }

        val moxListView = ListView<Herb>().apply {
            items.addAll(Herb.getMox())
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            prefHeight = 100.0
        }

        val moxAmountField = TextField().apply {
            promptText = "Amount of mox to make, ignore or set to 0 for none"
        }

        val moxBox = VBox(5.0, moxLabel, moxListView, moxAmountField)

        val agaLabel = Label("Select aga herbs").apply {
            styleClass.add("label")
        }

        val agaListView = ListView<Herb>().apply {
            items.addAll(Herb.getAga())
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            prefHeight = 100.0
        }

        val agaAmountField = TextField().apply {
            promptText = "Amount of aga to make, ignore or set to 0 for none"
        }

        val agaBox = VBox(5.0, agaLabel, agaListView, agaAmountField)

        val lyeLabel = Label("Select lye herbs").apply {
            styleClass.add("label")
        }

        val lyeListView = ListView<Herb>().apply {
            items.addAll(Herb.getLye())
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            prefHeight = 100.0
        }

        val lyeAmountField = TextField().apply {
            promptText = "Amount of lye to make, ignore or set to 0 for none"
        }

        val lyeBox = VBox(5.0, lyeLabel, lyeListView, lyeAmountField)

        val confirmButton = Button("Start").apply {
            setPrefSize(150.0, 40.0)
            styleClass.add("start-btn")
            setOnAction {
                val moxListItems = moxListView.selectionModel.selectedItems
                val parsedMox = parseValue(moxListItems, PasteType.MOX, moxAmountField.text)

                val agaListItems = agaListView.selectionModel.selectedItems
                val parsedAga = parseValue(agaListItems, PasteType.AGA, agaAmountField.text)

                val lyeListItems = lyeListView.selectionModel.selectedItems
                val parsedLye = parseValue(lyeListItems, PasteType.LYE, lyeAmountField.text)

                targets.addAll(listOfNotNull(parsedMox, parsedAga, parsedLye))

                if (targets.isNotEmpty()) {
                    val stage = scene.window as Stage?
                    stage?.close()
                }
            }
        }

        val buttonContainer = HBox().apply {
            alignment = Pos.CENTER
            children.add(confirmButton)
        }

        root.children.addAll(
            moxBox,
            lyeBox,
            agaBox,
            buttonContainer
        )

        val scene = Scene(root, 400.0, 600.0)
        scene.stylesheets.add("style.css")

        return scene
    }

    private fun parseValue(list: ObservableList<Herb>, pasteType: PasteType, amount: String): PasteTarget? {
        val intAmount = amount.toIntOrNull() ?: return null

        if (intAmount <= 0) {
            return null
        }

        return PasteTarget(
            pasteType,
            list.toList(),
            amount.toInt()
        )
    }

    fun getTargets(): List<PasteTarget> {
        return targets
    }
}