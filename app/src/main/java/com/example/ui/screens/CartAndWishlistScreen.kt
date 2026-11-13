package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.WishlistItem
import com.example.ui.components.RazaProductImage
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.EmeraldSilk
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.util.BoutiqueActions
import com.example.ui.viewmodel.StoreUiState

@Composable
fun CartAndWishlistScreen(
  uiState: StoreUiState,
  initialTab: Int = 0,
  onUpdateQuantity: (String, Long) -> Unit,
  onRemoveCartItem: (String) -> Unit,
  onProceedToCheckout: (promoCode: String, discountAmount: Double) -> Unit,
  onMoveWishlistToBag: (WishlistItem) -> Unit,
  onRemoveWishlistItem: (Product) -> Unit,
  onOpenProduct: (String) -> Unit,
  onExploreStore: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
  var promoInput by remember { mutableStateOf("") }
  var appliedPromo by remember { mutableStateOf<String?>(null) }

  val subtotal = remember(uiState.cartItems) {
    uiState.cartItems.sumOf { it.unitPrice * it.quantity }
  }
  val matchedOffer =
    remember(appliedPromo, uiState.offers, subtotal) {
      uiState.offers.firstOrNull {
        it.promoCode.equals(appliedPromo, ignoreCase = true) && subtotal >= it.minOrderAmount
      }
    }
  val discountAmount =
    remember(matchedOffer, subtotal) {
      if (matchedOffer != null) (subtotal * matchedOffer.discountPercent) / 100.0 else 0.0
    }
  val shippingFee = if (subtotal >= 10000.0 || subtotal == 0.0) 0.0 else 499.0
  val total = (subtotal - discountAmount + shippingFee).coerceAtLeast(0.0)

  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
  ) {
    Text(
      text = "Your Atelier Bag",
      style = MaterialTheme.typography.headlineLarge,
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )

    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = RoyalGold,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = RoyalGold,
        )
      },
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Shopping Bag (${uiState.cartCount})") },
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Wishlist (${uiState.wishlistItems.size})") },
      )
    }

    if (selectedTab == 0) {
      if (uiState.cartItems.isEmpty()) {
        EmptyStateView(
          icon = Icons.Outlined.ShoppingBag,
          title = "Your Shopping Bag is Empty",
          subtitle = "Explore our curated bridal lehengas, ghararas, and pure silk sarees.",
          ctaLabel = "Explore Couture",
          onCta = onExploreStore,
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(uiState.cartItems, key = { it.id }) { item ->
            CartItemCard(
              item = item,
              onOpenProduct = { onOpenProduct(item.productId) },
              onIncrement = { onUpdateQuantity(item.id, item.quantity + 1L) },
              onDecrement = { onUpdateQuantity(item.id, item.quantity - 1L) },
              onRemove = { onRemoveCartItem(item.id) },
            )
          }

          // Promo Code Box
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text("Privilege & Promo Codes", style = MaterialTheme.typography.titleSmall)
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  OutlinedTextField(
                    value = promoInput,
                    onValueChange = { promoInput = it.uppercase() },
                    placeholder = { Text("Enter code e.g. RAZA15") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                  )
                  Button(
                    onClick = { appliedPromo = promoInput.trim() },
                    colors =
                      ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianBlack,
                      ),
                    modifier = Modifier.height(52.dp),
                  ) {
                    Text("Apply")
                  }
                }
                if (uiState.offers.isNotEmpty()) {
                  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.offers, key = { it.id }) { offer ->
                      Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                        modifier =
                          Modifier.clickable {
                            promoInput = offer.promoCode
                            appliedPromo = offer.promoCode
                          },
                      ) {
                        Text(
                          text = "${offer.promoCode} (${offer.discountPercent}% off)",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                      }
                    }
                  }
                }
                if (matchedOffer != null) {
                  Text(
                    text = "Applied ${matchedOffer.promoCode}: saving ${BoutiqueActions.formatInr(discountAmount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldSilk,
                  )
                }
              }
            }
          }

          // Summary Card
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                PriceLine("Subtotal", BoutiqueActions.formatInr(subtotal))
                if (discountAmount > 0) {
                  PriceLine(
                    "Offer Discount",
                    "-${BoutiqueActions.formatInr(discountAmount)}",
                    valueColor = EmeraldSilk,
                  )
                }
                PriceLine(
                  "Insured Delivery",
                  if (shippingFee == 0.0) "Complimentary" else BoutiqueActions.formatInr(shippingFee),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = "Total Payable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = BoutiqueActions.formatInr(total),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                  onClick = {
                    onProceedToCheckout(matchedOffer?.promoCode.orEmpty(), discountAmount)
                  },
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = RoyalGold,
                      contentColor = ObsidianBlack,
                    ),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth().height(52.dp).testTag("proceed_checkout_button"),
                ) {
                  Text(
                    text = "Proceed to Checkout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }
    } else {
      if (uiState.wishlistItems.isEmpty()) {
        EmptyStateView(
          icon = Icons.Outlined.FavoriteBorder,
          title = "Your Wishlist is Empty",
          subtitle = "Tap the heart icon on any lehenga, suit, or saree to save it here.",
          ctaLabel = "Discover Couture",
          onCta = onExploreStore,
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(uiState.wishlistItems, key = { it.id }) { wish ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                RazaProductImage(
                  imageSource = wish.productImage,
                  contentDescription = wish.productName,
                  modifier =
                    Modifier.size(84.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { onOpenProduct(wish.productId) },
                  contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(
                    text = wish.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Text(
                    text = wish.productName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = BoutiqueActions.formatInr(wish.effectivePrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                  )
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                      onClick = { onMoveWishlistToBag(wish) },
                      border = BorderStroke(1.dp, RoyalGold),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                      modifier = Modifier.height(36.dp),
                    ) {
                      Text("Move to Bag", style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(
                      onClick = {
                        onRemoveWishlistItem(
                          Product(id = wish.productId, name = wish.productName)
                        )
                      },
                      modifier = Modifier.size(36.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Remove from wishlist",
                        tint = CrimsonVelvet,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CartItemCard(
  item: CartItem,
  onOpenProduct: () -> Unit,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  onRemove: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      RazaProductImage(
        imageSource = item.productImage,
        contentDescription = item.productName,
        modifier =
          Modifier.size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenProduct() },
        contentScale = ContentScale.Crop,
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Text(
            text = item.productName,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Remove item",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Text(
          text = "Size: ${item.selectedSize} • ${item.selectedColor}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (item.customStitching || item.madeToOrder) {
          Text(
            text =
              listOfNotNull(
                if (item.customStitching) "Custom Stitching" else null,
                if (item.madeToOrder) "Made-to-Order" else null,
              ).joinToString(" • "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        if (item.customMeasurements.isNotBlank()) {
          Text(
            text = "Notes: ${item.customMeasurements}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = BoutiqueActions.formatInr(item.unitPrice * item.quantity),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.size(34.dp).clickable { onDecrement() },
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity", modifier = Modifier.size(16.dp))
              }
            }
            Text(
              text = "${item.quantity}",
              style = MaterialTheme.typography.titleSmall,
              modifier = Modifier.padding(horizontal = 8.dp),
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = RoyalGold,
              modifier = Modifier.size(34.dp).clickable { onIncrement() },
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Filled.Add,
                  contentDescription = "Increase quantity",
                  tint = ObsidianBlack,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PriceLine(
  label: String,
  value: String,
  valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
  }
}

@Composable
fun EmptyStateView(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  ctaLabel: String,
  onCta: () -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = RoyalGold.copy(alpha = 0.7f),
        modifier = Modifier.size(64.dp),
      )
      Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
      Text(
        subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Button(
        onClick = onCta,
        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(ctaLabel, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}
