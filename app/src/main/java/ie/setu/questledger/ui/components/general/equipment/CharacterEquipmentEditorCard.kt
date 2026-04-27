package ie.setu.questledger.ui.components.general.equipment

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
import ie.setu.questledger.models.inventory.InventoryItemType

@Composable
fun CharacterEquipmentEditorCard(
    inventory: CharacterInventory,
    onEquipItem: (String) -> Unit,
    onUnequipWeapon: () -> Unit,
    onUnequipArmour: () -> Unit,
    onUnequipOffhand: () -> Unit,
    onUnequipSpellFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weapons = inventory.items.filter { it.type == InventoryItemType.WEAPON }
    val armours = inventory.items.filter { it.type == InventoryItemType.ARMOUR }
    val offhands = inventory.items.filter { it.type == InventoryItemType.SHIELD }
    val focuses = inventory.items.filter { it.type == InventoryItemType.SPELL_FOCUS }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Edit Equipment",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            EquipmentSection(
                title = "Weapons",
                items = weapons,
                onEquipItem = onEquipItem
            )
            OutlinedButton(
                onClick = onUnequipWeapon,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unequip Weapon")
            }

            Spacer(Modifier.height(12.dp))

            EquipmentSection(
                title = "Armour",
                items = armours,
                onEquipItem = onEquipItem
            )
            OutlinedButton(
                onClick = onUnequipArmour,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unequip Armour")
            }

            Spacer(Modifier.height(12.dp))

            EquipmentSection(
                title = "Offhand / Shields",
                items = offhands,
                onEquipItem = onEquipItem
            )
            OutlinedButton(
                onClick = onUnequipOffhand,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unequip Offhand")
            }

            Spacer(Modifier.height(12.dp))

            EquipmentSection(
                title = "Spell Focus",
                items = focuses,
                onEquipItem = onEquipItem
            )
            OutlinedButton(
                onClick = onUnequipSpellFocus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unequip Spell Focus")
            }
        }
    }
}

@Composable
private fun EquipmentSection(
    title: String,
    items: List<InventoryItemModel>,
    onEquipItem: (String) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall
    )
    Spacer(Modifier.height(6.dp))

    if (items.isEmpty()) {
        Text("None")
    } else {
        items.forEach { item ->
            Button(
                onClick = { onEquipItem(item.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Equip ${item.name}")
            }
            Spacer(Modifier.height(6.dp))
        }
    }

    Spacer(Modifier.height(6.dp))
}