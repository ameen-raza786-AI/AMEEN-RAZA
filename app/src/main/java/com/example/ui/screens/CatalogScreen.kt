package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.firebase.StoreCatalogSeeder
import com.example.data.model.Product
import com.example.ui.components.LuxuryProductCard
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.viewmodel.ProductSortOption
import com.example.ui.viewmodel.StoreUiState

@Composable
fun CatalogScreen(
  uiState: StoreUiState,
  onSearchQueryChange: (String) -> Unit,
  onCategorySelect: (String) -> Unit,
  onSectionSelect: (String) -> Unit,
  onSortSelect: (ProductSortOption) -> Unit,
  onClearFilters: () -> Unit,
  onProductClick: (String) -> Unit,
  onToggleWishlist: (Product) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showSortMenu by remember { mutableStateOf(false) }
  val categoryChips = remember(uiState.categories) {
    listOf("All") + uiState.categories.map { it.name }
  }
  val sectionChips = remember { listOf("All") + StoreCatalogSeeder.homepageSections }
  val results = uiState.filteredProducts

  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
  ) {
    // Header & Search Bar
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Atelier Catalog",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "${results.size} curated pieces",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Box {
          Surface(
            onClick = { showSortMenu = true },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.height(48.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Sort,
                contentDescription = "Sort products",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = uiState.filter.sortOption.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
          DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
            ProductSortOption.entries.forEach { option ->
              DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                  onSortSelect(option)
                  showSortMenu = false
                },
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = uiState.filter.searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search lehenga, gharara, Banarasi silk, zardozi...") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
        },
        trailingIcon = {
          if (uiState.filter.searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Filled.Close, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RoyalGold,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          ),
        modifier = Modifier.fillMaxWidth().testTag("catalog_search_input"),
      )
    }

    // 9 Category Filter Chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(categoryChips) { category ->
        val selected = uiState.filter.selectedCategory.equals(category, ignoreCase = true)
        FilterChip(
          selected = selected,
          onClick = { onCategorySelect(category) },
          label = { Text(category, style = MaterialTheme.typography.labelMedium) },
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = RoyalGold,
              selectedLabelColor = ObsidianBlack,
              containerColor = MaterialTheme.colorScheme.surface,
              labelColor = MaterialTheme.colorScheme.onSurface,
            ),
          border =
            FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = selected,
              borderColor = MaterialTheme.colorScheme.outline,
              selectedBorderColor = RoyalGold,
            ),
        )
      }
    }

    // Collection Section Filter Chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(sectionChips) { sec ->
        val selected = uiState.filter.selectedSection.equals(sec, ignoreCase = true)
        FilterChip(
          selected = selected,
          onClick = { onSectionSelect(sec) },
          label = {
            Text(
              if (sec == "All") "All Collections" else sec,
              style = MaterialTheme.typography.labelSmall,
            )
          },
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary,
            ),
        )
      }
    }

    if (results.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Outlined.Checkroom,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(56.dp),
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No matching couture found",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Try clearing your search or switching category filters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(14.dp))
          TextButton(onClick = onClearFilters) {
            Text("Reset Filters", color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    } else {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 680.dp) 3 else 2
        LazyVerticalGrid(
          columns = GridCells.Fixed(columns),
          contentPadding = PaddingValues(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          items(results, key = { it.id }) { product ->
            LuxuryProductCard(
              product = product,
              isWishlisted = uiState.wishlistProductIds.contains(product.id),
              onProductClick = { onProductClick(product.id) },
              onToggleWishlist = { onToggleWishlist(product) },
            )
          }
        }
      }
    }
  }
}
