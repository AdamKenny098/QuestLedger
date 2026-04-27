package ie.setu.questledger.ui.components.general.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.models.inventory.CharacterInventory
import ie.setu.questledger.models.inventory.InventoryItemModel

@Composable
fun CharacterInventoryEditorCard(
    inventory: CharacterInventory,
    onRemoveItem: (String) -> Unit,
    onAddTestPotion: () -> Unit,
    onAddTestTool: () -> Unit,
    onAddTestShield: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Edit Inventory",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onAddTestPotion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Test Potion")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddTestTool,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Test Tool")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddTestShield,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Test Shield")
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Remove Items",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.height(8.dp))

            if (inventory.items.isEmpty()) {
                Text("No items to remove.")
            } else {
                inventory.items.forEach { item ->
                    RemoveItemRow(
                        item = item,
                        onRemoveItem = onRemoveItem
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun RemoveItemRow(
    item: InventoryItemModel,
    onRemoveItem: (String) -> Unit
) {
    Button(
        onClick = { onRemoveItem(item.id) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Remove ${item.name}")
    }
}