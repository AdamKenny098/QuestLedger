package ie.setu.questledger.ui.screens.create

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import javax.inject.Inject

@HiltViewModel
class CompendiumPreviewViewModel @Inject constructor(
    private val compendiumService: CompendiumService
) : ViewModel() {

    fun getRaces(): List<RaceDefinition> = compendiumService.getRaces()

    fun getClasses(): List<ClassDefinition> = compendiumService.getClasses()
}