package ie.setu.questledger.ui.components.general.inventory

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
import ie.setu.questledger.data.rules.CurrencyRules
import ie.setu.questledger.data.rules.InventoryEngine
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.inventory.InventoryItemType
import kotlin.math.roundToInt

@Composable
fun CharacterInventoryCard(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    val inventory = character.inventory
    val usedSlots = InventoryEngine.usedSlots(inventory)
    val remainingSlots = InventoryEngine.remainingSlots(inventory)
    val totalWeight = InventoryEngine.totalWeightLb(inventory)

    val ammunition = inventory.items.filter { it.type == InventoryItemType.AMMUNITION }
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
            Text("Carried Weight: ${formatWeight(totalWeight)} lb.")

            Spacer(Modifier.height(8.dp))

            Text("Currency", style = MaterialTheme.typography.titleSmall)
            Text(CurrencyRules.formatWallet(character))

            Spacer(Modifier.height(12.dp))

            InventorySection("Ammunition", ammunition)

            Spacer(Modifier.height(12.dp))

            InventorySection("Consumables", consumables)

            Spacer(Modifier.height(12.dp))

            InventorySection("Spell Focus / Tools", toolsAndFocuses)

            Spacer(Modifier.height(12.dp))

            InventorySection("Backpack Items", backpackItems)
        }
    }
}

@Composable
private fun InventorySection(
    title: String,
    items: List<ie.setu.questledger.models.inventory.InventoryItemModel>
) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(6.dp))
    if (items.isEmpty()) {
        Text("None")
    } else {
        items.forEach { item ->
            val details = buildList {
                add("x${item.quantity}")
                if (item.weightLb > 0.0) {
                    add("${formatWeight(item.weightLb * item.quantity)} lb.")
                }
                if (item.costCp > 0) add(CurrencyRules.formatCost(item.costCp))
            }
            Text("${item.name} • ${details.joinToString(" • ")}")
        }
    }
}

private fun formatWeight(value: Double): String {
    return ((value * 10.0).roundToInt() / 10.0).toString()
}
