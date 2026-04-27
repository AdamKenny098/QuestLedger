package ie.setu.questledger.ui.components.general.equipment

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
import ie.setu.questledger.models.characters.CharacterModel

@Composable
fun CharacterEquipmentCard(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    val inventory = character.inventory

    val equippedWeapon = InventoryEngine.findEquippedWeapon(inventory)
    val equippedArmour = InventoryEngine.findEquippedArmour(inventory)
    val equippedOffhand = InventoryEngine.findEquippedOffhand(inventory)
    val spellFocus = InventoryEngine.findSpellFocus(inventory)

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Equipment",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text("Weapon: ${equippedWeapon?.name ?: "None"}")
            if (equippedWeapon != null) {
                if (equippedWeapon.damageDice.isNotBlank()) {
                    Text("Damage: ${equippedWeapon.damageDice}")
                }
                if (equippedWeapon.attackBonus != 0) {
                    Text("Attack Bonus: ${formatSigned(equippedWeapon.attackBonus)}")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text("Armour: ${equippedArmour?.name ?: "None"}")
            if (equippedArmour != null) {
                if (equippedArmour.armourBonus != 0) {
                    Text("Armour Bonus: +${equippedArmour.armourBonus}")
                }
                if (equippedArmour.movementPenalty != 0) {
                    Text("Movement Penalty: -${equippedArmour.movementPenalty}")
                }
                if (equippedArmour.spellcastingBlocked) {
                    Text("Spellcasting Restriction: Yes")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text("Offhand / Shield: ${equippedOffhand?.name ?: "None"}")
            if (equippedOffhand != null && equippedOffhand.shieldBonus != 0) {
                Text("Shield Bonus: +${equippedOffhand.shieldBonus}")
            }

            Spacer(Modifier.height(8.dp))

            Text("Spell Focus / Tool: ${spellFocus?.name ?: "None"}")
        }
    }
}

private fun formatSigned(value: Int): String {
    return if (value >= 0) "+$value" else value.toString()
}