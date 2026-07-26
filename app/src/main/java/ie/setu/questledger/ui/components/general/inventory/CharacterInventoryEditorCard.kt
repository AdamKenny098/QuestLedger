package ie.setu.questledger.ui.components.general.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.compendium.EquipmentCatalogueItem
import ie.setu.questledger.data.compendium.EquipmentPackDefinition
import ie.setu.questledger.data.rules.CurrencyRules
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.inventory.CurrencyDenomination
import ie.setu.questledger.models.inventory.InventoryItemModel
import ie.setu.questledger.ui.components.general.CompendiumDropdown
import ie.setu.questledger.ui.components.general.CompendiumOption

@Composable
fun CharacterInventoryEditorCard(
    character: CharacterModel,
    catalogueItems: List<EquipmentCatalogueItem>,
    equipmentPacks: List<EquipmentPackDefinition>,
    onRemoveItem: (String) -> Unit,
    onChangeQuantity: (String, Int) -> Unit,
    onAddCatalogueItem: (String) -> Unit,
    onAddEquipmentPack: (String) -> Unit,
    onAdjustCurrency: (CurrencyDenomination, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val inventory = character.inventory
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

    val categoryOptions = remember(catalogueItems) {
        listOf("All") + catalogueItems.map { it.categoryLabel }.distinct().sorted()
    }
    val filteredCatalogue = remember(search, selectedCategory, catalogueItems) {
        catalogueItems.filter { entry ->
            val categoryMatches =
                selectedCategory == "All" || entry.categoryLabel == selectedCategory
            val searchMatches =
                search.isBlank() ||
                    entry.name.contains(search, ignoreCase = true) ||
                    entry.categoryLabel.contains(search, ignoreCase = true) ||
                    entry.properties.any { it.contains(search, ignoreCase = true) }
            categoryMatches && searchMatches
        }.take(24)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Inventory", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            CurrencyEditor(
                character = character,
                onAdjustCurrency = onAdjustCurrency
            )

            Spacer(Modifier.height(16.dp))
            Text("Equipment Packs", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))

            equipmentPacks.forEach { pack ->
                OutlinedButton(
                    onClick = { onAddEquipmentPack(pack.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Add ${pack.name} • ${CurrencyRules.formatCost(pack.costCp)}"
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(12.dp))
            Text("Equipment Catalogue", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search equipment") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            CompendiumDropdown(
                label = "Category",
                options = categoryOptions.map { CompendiumOption(it, it) },
                selectedId = selectedCategory,
                onSelected = { selectedCategory = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (filteredCatalogue.isEmpty()) {
                Text("No matching equipment.")
            } else {
                filteredCatalogue.forEach { item ->
                    OutlinedButton(
                        onClick = { onAddCatalogueItem(item.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quantity = if (item.defaultQuantity > 1) {
                            " x${item.defaultQuantity}"
                        } else {
                            ""
                        }
                        Text(
                            "Add ${item.name}$quantity • " +
                                CurrencyRules.formatCost(item.costCp)
                        )
                    }
                    if (item.description.isNotBlank()) {
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (filteredCatalogue.size == 24) {
                    Text(
                        "Showing the first 24 matches. Narrow the search to find a specific item.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Carried Items", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            if (inventory.items.isEmpty()) {
                Text("No carried items.")
            } else {
                inventory.items.forEach { item ->
                    InventoryItemEditorRow(
                        item = item,
                        onRemoveItem = onRemoveItem,
                        onChangeQuantity = onChangeQuantity
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CurrencyEditor(
    character: CharacterModel,
    onAdjustCurrency: (CurrencyDenomination, Int) -> Unit
) {
    Text("Currency", style = MaterialTheme.typography.titleSmall)
    Text(
        CurrencyRules.formatWallet(character),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(6.dp))

    CurrencyDenomination.entries.forEach { denomination ->
        val amount = when (denomination) {
            CurrencyDenomination.COPPER -> character.copperPieces
            CurrencyDenomination.SILVER -> character.silverPieces
            CurrencyDenomination.ELECTRUM -> character.electrumPieces
            CurrencyDenomination.GOLD -> character.goldPieces
            CurrencyDenomination.PLATINUM -> character.platinumPieces
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${denomination.label}: $amount ${denomination.abbreviation}")
            Row {
                OutlinedButton(
                    onClick = { onAdjustCurrency(denomination, -1) }
                ) {
                    Text("-")
                }
                OutlinedButton(
                    onClick = { onAdjustCurrency(denomination, 1) }
                ) {
                    Text("+")
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun InventoryItemEditorRow(
    item: InventoryItemModel,
    onRemoveItem: (String) -> Unit,
    onChangeQuantity: (String, Int) -> Unit
) {
    Text(
        "${item.name} x${item.quantity}",
        style = MaterialTheme.typography.bodyMedium
    )
    if (item.categoryLabel.isNotBlank()) {
        Text(item.categoryLabel, style = MaterialTheme.typography.bodySmall)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedButton(
            onClick = { onChangeQuantity(item.id, -1) },
            modifier = Modifier.weight(1f)
        ) {
            Text("-1")
        }
        OutlinedButton(
            onClick = { onChangeQuantity(item.id, 1) },
            modifier = Modifier.weight(1f)
        ) {
            Text("+1")
        }
        Button(
            onClick = { onRemoveItem(item.id) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Remove")
        }
    }
}
