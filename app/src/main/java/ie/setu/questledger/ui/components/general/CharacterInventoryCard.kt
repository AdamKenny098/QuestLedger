package ie.setu.questledger.ui.components.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.rules.InventoryEngine
import ie.setu.questledger.models.CharacterModel
import ie.setu.questledger.models.InventoryItemType

@Composable
fun CharacterInventoryCard(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    val inventory = character.inventory
    val usedSlots = InventoryEngine.usedSlots(inventory)
    val remainingSlots = InventoryEngine.remainingSlots(inventory)

    val consumables = inventory.items.filter { it.type == InventoryItemType.CONSUMABLE }
    val toolsAndFocuses = inventory.items.filter {
        it.type == InventoryItemType.TOOL || it.type == InventoryItemType.SPELL_FOCUS
    }
    val backpackItems = inventory.items.filter {
        it.type == InventoryItemType.BACKPACK_ITEM
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text("Used Slots: $usedSlots / ${inventory.capacitySlots}")
            Text("Remaining Slots: $remainingSlots")

            Spacer(Modifier.height(12.dp))

            Text("Consumables", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            if (consumables.isEmpty()) {
                Text("None")
            } else {
                consumables.forEach { item ->
                    Text(itemLine(item.name, item.quantity, item.slotCost))
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Spell Focus / Tools", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            if (toolsAndFocuses.isEmpty()) {
                Text("None")
            } else {
                toolsAndFocuses.forEach { item ->
                    Text(itemLine(item.name, item.quantity, item.slotCost))
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Backpack Items", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            if (backpackItems.isEmpty()) {
                Text("None")
            } else {
                backpackItems.forEach { item ->
                    Text(itemLine(item.name, item.quantity, item.slotCost))
                }
            }
        }
    }
}

private fun itemLine(
    name: String,
    quantity: Int,
    slotCost: Int
): String {
    return "$name x$quantity • Slots: ${quantity.coerceAtLeast(1) * slotCost}"
}