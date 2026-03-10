package ie.setu.questledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ie.setu.questledger.ui.components.DropDownMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarProvider(
    currentScreenLabel: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    showDeleteAll: Boolean,
    onHelp: () -> Unit,
    onDeleteAll: () -> Unit
) {
    TopAppBar(
        title = { Text(currentScreenLabel) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            DropDownMenu(
                onHelp = onHelp,
                onDeleteAll = onDeleteAll,
                showDeleteAll = showDeleteAll
            )
        }
    )
}
