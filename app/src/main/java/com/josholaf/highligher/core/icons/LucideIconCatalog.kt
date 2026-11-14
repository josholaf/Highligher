package com.josholaf.highligher.core.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class LucideIcon(val name: String, val keywords: String, val vector: ImageVector)

/**
 * Searchable icons using Lucide open-source names (https://lucide.dev, ISC).
 * Vectors map to Material Icons Extended so the app stays offline.
 */
object LucideIconCatalog {
    val all: List<LucideIcon> by lazy {
        listOf(
            i("folder", "folder directory", Icons.Default.Folder),
            i("folder-open", "folder open", Icons.Default.FolderOpen),
            i("inbox", "inbox mail", Icons.Default.Inbox),
            i("file-text", "file document note", Icons.Default.Description),
            i("book", "book read", Icons.Default.Book),
            i("book-open", "book open read", Icons.AutoMirrored.Filled.MenuBook),
            i("library", "library books", Icons.Default.AutoStories),
            i("notebook", "notebook note", Icons.AutoMirrored.Filled.Note),
            i("home", "home house", Icons.Default.Home),
            i("star", "star favorite", Icons.Default.Star),
            i("heart", "heart love", Icons.Default.Favorite),
            i("user", "user person", Icons.Default.Person),
            i("users", "users group team", Icons.Default.Groups),
            i("calendar", "calendar date", Icons.Default.CalendarMonth),
            i("map", "map location", Icons.Default.Map),
            i("globe", "globe world public", Icons.Default.Public),
            i("languages", "language translate", Icons.Default.Language),
            i("translate", "translate language", Icons.Default.Translate),
            i("code", "code programming", Icons.Default.Code),
            i("terminal", "terminal cli", Icons.Default.Terminal),
            i("cpu", "cpu chip memory", Icons.Default.Memory),
            i("monitor", "computer screen", Icons.Default.Computer),
            i("settings", "settings gear", Icons.Default.Settings),
            i("wrench", "wrench tool", Icons.Default.Build),
            i("hammer", "hammer tool", Icons.Default.Handyman),
            i("lightbulb", "idea light", Icons.Default.Lightbulb),
            i("palette", "palette art color", Icons.Default.Palette),
            i("paintbrush", "brush paint", Icons.Default.Brush),
            i("image", "image photo", Icons.Default.Image),
            i("camera", "camera photo", Icons.Default.CameraAlt),
            i("music", "music note", Icons.Default.MusicNote),
            i("school", "school education", Icons.Default.School),
            i("graduation-cap", "graduation university", Icons.Default.School),
            i("briefcase", "work briefcase", Icons.Default.Work),
            i("building", "building business", Icons.Default.Business),
            i("landmark", "landmark bank", Icons.Default.AccountBalance),
            i("gavel", "gavel law legal", Icons.Default.Gavel),
            i("scale", "scale measure", Icons.Default.Straighten),
            i("stethoscope", "stethoscope medical doctor", Icons.Default.MedicalServices),
            i("hospital", "hospital medical", Icons.Default.LocalHospital),
            i("heart-pulse", "heart pulse monitor", Icons.Default.MonitorHeart),
            i("pill", "pill medication pharmacy", Icons.Default.Medication),
            i("syringe", "syringe vaccine", Icons.Default.Vaccines),
            i("shield-plus", "shield health safety", Icons.Default.HealthAndSafety),
            i("baby", "baby child pediatric", Icons.Default.ChildCare),
            i("activity", "activity fitness", Icons.Default.FitnessCenter),
            i("brain", "brain psychology mind", Icons.Default.Psychology),
            i("sparkles", "sparkles wellness", Icons.Default.SelfImprovement),
            i("accessibility", "accessibility", Icons.Default.Accessibility),
            i("flask-conical", "flask science lab", Icons.Default.Science),
            i("microscope", "microscope biotech", Icons.Default.Biotech),
            i("leaf", "leaf eco nature", Icons.Default.Eco),
            i("droplets", "water drop fluid electrolyte", Icons.Default.WaterDrop),
            i("tree-pine", "tree park nature", Icons.Default.Park),
            i("paw-print", "pet animal", Icons.Default.Pets),
            i("utensils", "food restaurant", Icons.Default.Restaurant),
            i("shopping-bag", "shopping bag", Icons.Default.ShoppingBag),
            i("car", "car vehicle", Icons.Default.DirectionsCar),
            i("plane", "plane flight", Icons.Default.Flight),
            i("check-circle", "check done", Icons.Default.CheckCircle),
            i("layers", "layers category", Icons.Default.Category),
            i("palette-art", "color art design", Icons.Default.ColorLens),
            i("architecture", "architecture design", Icons.Default.Architecture),
            i("engineering", "engineering", Icons.Default.Engineering),
            i("pharmacy", "pharmacy", Icons.Default.LocalPharmacy),
            i("airplane", "airplane travel", Icons.Default.AirplanemodeActive),
        )
    }

    private fun i(name: String, keywords: String, vector: ImageVector) =
        LucideIcon(name, keywords, vector)

    fun search(query: String): List<LucideIcon> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return all
        return all.filter { it.name.contains(q) || it.keywords.contains(q) }
    }

    fun vectorOrDefault(name: String?): ImageVector {
        if (name.isNullOrBlank()) return Icons.Default.Folder
        return all.firstOrNull { it.name.equals(name, ignoreCase = true) }?.vector
            ?: Icons.Default.Folder
    }
}

@Composable
fun rememberFolderIcon(iconName: String?): ImageVector =
    LucideIconCatalog.vectorOrDefault(iconName)
