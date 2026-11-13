package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Offer
import com.example.data.model.Product
import com.example.ui.components.GoldShimmerPill
import com.example.ui.components.LuxuryProductCard
import com.example.ui.components.LuxurySectionHeader
import com.example.ui.components.RazaProductImage
import com.example.ui.theme.AntiqueBronze
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SterlingSilver
import com.example.ui.util.BoutiqueActions
import com.example.ui.viewmodel.StoreUiState
import kotlinx.coroutines.delay

private data class HeroBannerSlide(
  val eyebrow: String,
  val title: String,
  val subtitle: String,
  val imageKey: String,
  val targetCategory: String,
)

@Composable
fun HomeScreen(
  uiState: StoreUiState,
  onOpenSearch: () -> Unit,
  onOpenWishlist: () -> Unit,
  onOpenCart: () -> Unit,
  onOpenCategory: (String) -> Unit,
  onOpenSection: (String) -> Unit,
  onOpenProduct: (String) -> Unit,
  onToggleWishlist: (Product) -> Unit,
  onCopyPromoCode: (String) -> Unit,
  onOpenInfoPage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val slides =
    listOf(
      HeroBannerSlide(
        eyebrow = "BRIDAL TROUSSEAU 2026",
        title = "The Royal Zardozi Edit",
        subtitle = "Handcrafted Crimson Bridal Lehengas & Heritage Silks",
        imageKey = "drawable:img_hero_bridal",
        targetCategory = "Bridal Lehenga",
      ),
      HeroBannerSlide(
        eyebrow = "SANGEET & COCKTAIL",
        title = "Emerald Mirrorwork",
        subtitle = "Opulent Velvet Lehengas & Draped Shararas",
        imageKey = "drawable:img_lehenga_emerald",
        targetCategory = "Party Wear Lehenga",
      ),
      HeroBannerSlide(
        eyebrow = "NAWABI COUTURE",
        title = "Ivory Gharara & Suits",
        subtitle = "Lucknawi Kamdani, Organza & Pakistani Ensembles",
        imageKey = "drawable:img_suit_ivory",
        targetCategory = "Gharara Suits",
      ),
      HeroBannerSlide(
        eyebrow = "BANARASI ARCHIVE",
        title = "Obsidian Gold Drapes",
        subtitle = "Pure Katan Silk Sarees & Sculpted Reception Gowns",
        imageKey = "drawable:img_saree_obsidian",
        targetCategory = "Sarees",
      ),
    )

  LazyColumn(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    // 1. Luxury Top Brand Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "RAZA ETHNIC WEAR",
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "COUTURE HOUSE • EST. HERITAGE",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onOpenSearch,
            modifier = Modifier.size(48.dp).testTag("home_search_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search ethnic wear",
              tint = MaterialTheme.colorScheme.onBackground,
            )
          }
          IconButton(
            onClick = onOpenWishlist,
            modifier = Modifier.size(48.dp).testTag("home_wishlist_button"),
          ) {
            BadgedBox(
              badge = {
                if (uiState.wishlistItems.isNotEmpty()) {
                  Badge(containerColor = RoyalGold, contentColor = ObsidianBlack) {
                    Text("${uiState.wishlistItems.size}")
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Wishlist",
                tint = MaterialTheme.colorScheme.onBackground,
              )
            }
          }
          IconButton(
            onClick = onOpenCart,
            modifier = Modifier.size(48.dp).testTag("home_cart_button"),
          ) {
            BadgedBox(
              badge = {
                if (uiState.cartCount > 0) {
                  Badge(containerColor = RoyalGold, contentColor = ObsidianBlack) {
                    Text("${uiState.cartCount}")
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = "Shopping Bag",
                tint = MaterialTheme.colorScheme.onBackground,
              )
            }
          }
        }
      }
    }

    // 2. Premium Fashion Banner Carousel
    item {
      HeroBannerCarousel(
        slides = slides,
        onExploreCategory = onOpenCategory,
      )
    }

    // 3. All 9 Categories Strip
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LuxurySectionHeader(
          title = "Curated Categories",
          subtitle = "9 royal silhouettes tailored to perfection",
          actionLabel = "All",
          onActionClick = { onOpenCategory("All") },
        )
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(uiState.categories, key = { it.id }) { category ->
            CategoryCircleTile(category = category, onClick = { onOpenCategory(category.name) })
          }
        }
      }
    }

    // 4. Special Offers Promo Banner Strip
    if (uiState.offers.isNotEmpty()) {
      item {
        SpecialOfferBannerRow(
          offers = uiState.offers,
          onCopyCode = { code ->
            BoutiqueActions.copyToClipboard(context, "Promo Code", code)
            onCopyPromoCode(code)
          },
        )
      }
    }

    // 5. New Arrivals
    val newArrivals =
      uiState.products.filter { it.collections.contains("New Arrivals") }.ifEmpty {
        uiState.products.take(4)
      }
    item {
      ProductShelfSection(
        title = "New Arrivals",
        subtitle = "Fresh from the Raza embroidery atelier",
        products = newArrivals,
        wishlistIds = uiState.wishlistProductIds,
        onViewAll = { onOpenSection("New Arrivals") },
        onProductClick = onOpenProduct,
        onToggleWishlist = onToggleWishlist,
      )
    }

    // 6. Best Selling Collection
    val bestSellers =
      uiState.products.filter { it.collections.contains("Best Selling") }.ifEmpty {
        uiState.products.take(4)
      }
    item {
      ProductShelfSection(
        title = "Best Selling Collection",
        subtitle = "Most coveted bridal & festive heirlooms",
        products = bestSellers,
        wishlistIds = uiState.wishlistProductIds,
        onViewAll = { onOpenSection("Best Selling") },
        onProductClick = onOpenProduct,
        onToggleWishlist = onToggleWishlist,
      )
    }

    // 7. Trending Lehenga Collection
    val trendingLehengas =
      uiState.products.filter {
        it.collections.contains("Trending Lehenga") || it.category.contains("Lehenga", true)
      }
    if (trendingLehengas.isNotEmpty()) {
      item {
        ProductShelfSection(
          title = "Trending Lehenga Collection",
          subtitle = "Bridal, Party Wear & Designer Lehengas",
          products = trendingLehengas,
          wishlistIds = uiState.wishlistProductIds,
          onViewAll = { onOpenSection("Trending Lehenga") },
          onProductClick = onOpenProduct,
          onToggleWishlist = onToggleWishlist,
        )
      }
    }

    // 8. Exclusive Gharara Collection
    val ghararas =
      uiState.products.filter {
        it.collections.contains("Exclusive Gharara") || it.category.contains("Gharara", true)
      }
    if (ghararas.isNotEmpty()) {
      item {
        ProductShelfSection(
          title = "Exclusive Gharara Collection",
          subtitle = "Awadhi Nawabi craft & regal flares",
          products = ghararas,
          wishlistIds = uiState.wishlistProductIds,
          onViewAll = { onOpenSection("Exclusive Gharara") },
          onProductClick = onOpenProduct,
          onToggleWishlist = onToggleWishlist,
        )
      }
    }

    // 9. Designer Suits
    val designerSuits =
      uiState.products.filter {
        it.collections.contains("Designer Suits") || it.category.contains("Suit", true)
      }
    if (designerSuits.isNotEmpty()) {
      item {
        ProductShelfSection(
          title = "Designer Suits",
          subtitle = "Pakistani, Anarkali & Sharara ensembles",
          products = designerSuits,
          wishlistIds = uiState.wishlistProductIds,
          onViewAll = { onOpenSection("Designer Suits") },
          onProductClick = onOpenProduct,
          onToggleWishlist = onToggleWishlist,
        )
      }
    }

    // 10. Special Offers Products
    val specialOffers = uiState.products.filter { it.isSpecialOffer || it.discountPercent > 0 }
    if (specialOffers.isNotEmpty()) {
      item {
        ProductShelfSection(
          title = "Special Offers",
          subtitle = "Limited-edition festive atelier privileges",
          products = specialOffers,
          wishlistIds = uiState.wishlistProductIds,
          onViewAll = { onOpenSection("Special Offers") },
          onProductClick = onOpenProduct,
          onToggleWishlist = onToggleWishlist,
        )
      }
    }

    // 11. Customer Reviews Spotlight
    if (uiState.reviews.isNotEmpty()) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          LuxurySectionHeader(
            title = "Patron Reviews",
            subtitle = "Words from brides & couture collectors",
            actionLabel = null,
          )
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            items(uiState.reviews.take(5), key = { it.id }) { rev ->
              Card(
                modifier = Modifier.width(275.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(rev.rating.toInt().coerceIn(1, 5)) {
                      Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(14.dp),
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "\"${rev.comment}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "— ${rev.reviewerName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }
          }
        }
      }
    }

    // 12. Boutique Concierge & Quick Links Footer
    item {
      Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "RAZA ATELIER CONCIERGE",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text =
              "Need bespoke bridal measurements, fabric swatches, or international express shipping? Connect directly with our master stylists.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { BoutiqueActions.launchWhatsAppConcierge(context) },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = RoyalGold,
                  contentColor = ObsidianBlack,
                ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(48.dp),
            ) {
              Text("WhatsApp Stylist", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
              onClick = { BoutiqueActions.launchInstagram(context) },
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(48.dp),
            ) {
              Text(
                "Instagram",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            FooterLink("About Us") { onOpenInfoPage("about") }
            FooterLink("Contact") { onOpenInfoPage("contact") }
            FooterLink("Shipping") { onOpenInfoPage("shipping") }
            FooterLink("Returns") { onOpenInfoPage("returns") }
            FooterLink("Privacy") { onOpenInfoPage("privacy") }
          }
        }
      }
    }
  }
}

@Composable
private fun FooterLink(label: String, onClick: () -> Unit) {
  Text(
    text = label,
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.clickable { onClick() }.padding(vertical = 8.dp, horizontal = 4.dp),
  )
}

@Composable
private fun HeroBannerCarousel(
  slides: List<HeroBannerSlide>,
  onExploreCategory: (String) -> Unit,
) {
  val pagerState = rememberPagerState(pageCount = { slides.size })
  LaunchedEffect(pagerState) {
    while (true) {
      delay(4500L)
      val next = (pagerState.currentPage + 1) % slides.size
      pagerState.animateScrollToPage(next)
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    HorizontalPager(
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      pageSpacing = 12.dp,
    ) { page ->
      val slide = slides[page]
      Card(
        modifier =
          Modifier.fillMaxWidth()
            .height(250.dp)
            .clickable { onExploreCategory(slide.targetCategory) },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          RazaProductImage(
            imageSource = slide.imageKey,
            contentDescription = slide.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.horizontalGradient(
                    listOf(
                      ObsidianBlack.copy(alpha = 0.90f),
                      ObsidianBlack.copy(alpha = 0.52f),
                      Color.Transparent,
                    )
                  )
                )
          )
          Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(18.dp).fillMaxWidth(0.78f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            GoldShimmerPill(text = slide.eyebrow)
            Text(
              text = slide.title,
              style = MaterialTheme.typography.displayMedium,
              color = PlatinumWhite,
              maxLines = 2,
            )
            Text(
              text = slide.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = SterlingSilver,
              maxLines = 2,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
              color = RoyalGold,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.clickable { onExploreCategory(slide.targetCategory) },
            ) {
              Text(
                text = "Explore Collection",
                style = MaterialTheme.typography.labelLarge,
                color = ObsidianBlack,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              )
            }
          }
        }
      }
    }

    // Indicator dots
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      repeat(slides.size) { index ->
        val active = pagerState.currentPage == index
        Box(
          modifier =
            Modifier.padding(horizontal = 3.dp)
              .height(6.dp)
              .width(if (active) 20.dp else 6.dp)
              .clip(CircleShape)
              .background(if (active) RoyalGold else SterlingSilver.copy(alpha = 0.35f))
        )
      }
    }
  }
}

@Composable
private fun CategoryCircleTile(category: Category, onClick: () -> Unit) {
  Column(
    modifier =
      Modifier.width(86.dp)
        .clip(RoundedCornerShape(12.dp))
        .clickable { onClick() }
        .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier.size(72.dp)
          .clip(CircleShape)
          .border(1.5.dp, Brush.linearGradient(listOf(RoyalGold, SterlingSilver)), CircleShape)
          .padding(3.dp)
    ) {
      RazaProductImage(
        imageSource = category.imageKey,
        contentDescription = category.name,
        modifier = Modifier.fillMaxSize().clip(CircleShape),
        contentScale = ContentScale.Crop,
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = category.name,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun SpecialOfferBannerRow(
  offers: List<Offer>,
  onCopyCode: (String) -> Unit,
) {
  LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(offers, key = { it.id }) { offer ->
      Surface(
        modifier =
          Modifier.width(290.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCopyCode(offer.promoCode) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.LocalOffer,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${offer.discountPercent}% PRIVILEGE",
                style = MaterialTheme.typography.labelSmall,
                color = ChampagneGold,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = offer.title,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = offer.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Surface(
            color = Brush.linearGradient(listOf(AntiqueBronze, RoyalGold)).let { RoyalGold },
            shape = RoundedCornerShape(8.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = offer.promoCode,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ObsidianBlack,
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy promo code",
                tint = ObsidianBlack,
                modifier = Modifier.size(13.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProductShelfSection(
  title: String,
  subtitle: String,
  products: List<Product>,
  wishlistIds: Set<String>,
  onViewAll: () -> Unit,
  onProductClick: (String) -> Unit,
  onToggleWishlist: (Product) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    LuxurySectionHeader(
      title = title,
      subtitle = subtitle,
      actionLabel = "View All",
      onActionClick = onViewAll,
    )
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(products, key = { it.id }) { product ->
        LuxuryProductCard(
          product = product,
          isWishlisted = wishlistIds.contains(product.id),
          onProductClick = { onProductClick(product.id) },
          onToggleWishlist = { onToggleWishlist(product) },
          modifier = Modifier.width(178.dp),
        )
      }
    }
  }
}
