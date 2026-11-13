package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.util.BoutiqueActions
import com.example.ui.viewmodel.StoreUiState

@Composable
fun ProfileScreen(
  uiState: StoreUiState,
  onSaveProfile: (name: String, phone: String, address: String, city: String, postal: String) -> Unit,
  onToggleTheme: () -> Unit,
  onOpenWishlist: () -> Unit,
  onOpenOrders: () -> Unit,
  onOpenAdminDashboard: () -> Unit,
  onOpenInfoPage: (String) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val profile = uiState.userProfile

  var name by remember(profile?.displayName) { mutableStateOf(profile?.displayName.orEmpty()) }
  var phone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
  var address by remember(profile?.address) { mutableStateOf(profile?.address.orEmpty()) }
  var city by remember(profile?.city) { mutableStateOf(profile?.city.orEmpty()) }
  var postal by remember(profile?.postalCode) { mutableStateOf(profile?.postalCode.orEmpty()) }

  LazyColumn(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Header
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier =
              Modifier.size(58.dp)
                .clip(CircleShape)
                .background(RoyalGold),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = (profile?.displayName?.firstOrNull() ?: 'R').uppercase(),
              style = MaterialTheme.typography.headlineLarge,
              color = ObsidianBlack,
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = profile?.displayName?.ifBlank { "Valued Patron" } ?: "Valued Patron",
              style = MaterialTheme.typography.headlineMedium,
            )
            Text(
              text = profile?.email.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.isAdmin) {
              Text(
                text = "ATELIER OWNER • ADMIN",
                style = MaterialTheme.typography.labelSmall,
                color = RoyalGold,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    // Admin Entry
    if (uiState.isAdmin) {
      item {
        Button(
          onClick = onOpenAdminDashboard,
          colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("open_admin_dashboard_button"),
        ) {
          Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Open Admin Studio Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Quick Shortcuts
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Column {
          ProfileMenuRow(Icons.Filled.ReceiptLong, "Order History & Tracking", "${uiState.orders.size} orders") {
            onOpenOrders()
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          ProfileMenuRow(Icons.Filled.Favorite, "Saved Wishlist", "${uiState.wishlistItems.size} items") {
            onOpenWishlist()
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Icon(Icons.Filled.DarkMode, contentDescription = null, tint = RoyalGold)
              Column {
                Text("Royal Noir Theme", style = MaterialTheme.typography.titleSmall)
                Text("Switch between Obsidian Noir & Ivory Atelier", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Switch(
              checked = uiState.darkTheme,
              onCheckedChange = { onToggleTheme() },
              colors = SwitchDefaults.colors(checkedTrackColor = RoyalGold),
            )
          }
        }
      }
    }

    // Saved Shipping Profile Form
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Shipping Profile", style = MaterialTheme.typography.titleLarge)
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone / WhatsApp") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = postal, onValueChange = { postal = it }, label = { Text("PIN Code") }, modifier = Modifier.weight(1f))
          }
          Button(
            onClick = { onSaveProfile(name, phone, address, city, postal) },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth().height(48.dp),
          ) {
            Text("Save Shipping Details", fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // Boutique & Policies Section
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Column {
          ProfileMenuRow(Icons.Filled.Info, "About Raza Ethnic Wear", "Our craft & heritage") { onOpenInfoPage("about") }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          ProfileMenuRow(Icons.Filled.SupportAgent, "Contact Us & Concierge", "Ask a stylist") { onOpenInfoPage("contact") }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          ProfileMenuRow(Icons.Filled.LocalShipping, "Shipping Policy", "Domestic & global timelines") { onOpenInfoPage("shipping") }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          ProfileMenuRow(Icons.Filled.Policy, "Return & Refund Policy", "Ready-to-wear & custom terms") { onOpenInfoPage("returns") }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          ProfileMenuRow(Icons.Filled.Policy, "Privacy Policy", "Data protection & security") { onOpenInfoPage("privacy") }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          ProfileMenuRow(Icons.Filled.Info, "Instagram @razaethnicwear", "Latest bridal lookbooks") {
            BoutiqueActions.launchInstagram(context)
          }
        }
      }
    }

    // Sign Out
    item {
      OutlinedButton(
        onClick = onSignOut,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out", fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun ProfileMenuRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Icon(icon, contentDescription = null, tint = RoyalGold)
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueInfoScreen(
  pageType: String,
  uiState: StoreUiState,
  onSubmitInquiry: (name: String, phone: String, subject: String, message: String) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler { onBack() }
  val context = LocalContext.current

  val title =
    when (pageType) {
      "about" -> "About Raza Ethnic Wear"
      "contact" -> "Contact Us"
      "shipping" -> "Shipping Policy"
      "returns" -> "Return & Refund Policy"
      else -> "Privacy Policy"
    }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      )
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      when (pageType) {
        "about" -> {
          item {
            InfoSectionCard(
              heading = "Our Heritage & Craftsmanship",
              body =
                "RAZA ETHNIC WEAR is a luxury women's ethnic fashion house dedicated to preserving authentic Indian and Mughal bridal artistry. Every bridal lehenga, Lucknawi gharara, Pakistani ensemble, and pure Banarasi silk saree is crafted by third-generation karigars using hand-worked zardozi, dabka, resham, and gota patti.",
            )
          }
          item {
            InfoSectionCard(
              heading = "Bespoke Atelier Promise",
              body =
                "From personalized video consultations via WhatsApp to custom blouse and lehenga tailoring, we combine timeless royal aesthetics with modern online convenience and insured worldwide delivery.",
            )
          }
        }
        "contact" -> {
          item {
            InfoSectionCard(
              heading = "Flagship Couture Studio",
              body =
                "Raza Ethnic Wear Atelier\n42 Royal Heritage Plaza, Johari Bazaar\nJaipur & New Delhi, India\nConcierge Hours: Mon–Sun, 10:30 AM – 8:30 PM IST\nWhatsApp & Phone: +91 98765 43210\nEmail: concierge@razaethnicwear.com\nInstagram: @razaethnicwear",
            )
          }
          item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = { BoutiqueActions.launchWhatsAppConcierge(context) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
                modifier = Modifier.weight(1f).height(48.dp),
              ) {
                Text("Chat on WhatsApp")
              }
              OutlinedButton(
                onClick = { BoutiqueActions.launchInstagram(context) },
                border = BorderStroke(1.dp, RoyalGold),
                modifier = Modifier.weight(1f).height(48.dp),
              ) {
                Text("Instagram", color = RoyalGold)
              }
            }
          }
          item {
            var name by remember { mutableStateOf(uiState.userProfile?.displayName.orEmpty()) }
            var phone by remember { mutableStateOf(uiState.userProfile?.phone.orEmpty()) }
            var subject by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("") }

            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Send an Atelier Inquiry", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone / WhatsApp") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject (e.g. Bridal Trial / Custom Sizing)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Button(
                  enabled = subject.trim().length >= 2 && message.trim().length >= 5,
                  onClick = {
                    onSubmitInquiry(name, phone, subject, message)
                    subject = ""
                    message = ""
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
                  modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                  Text("Submit Inquiry", fontWeight = FontWeight.SemiBold)
                }
              }
            }
          }
        }
        "shipping" -> {
          item {
            InfoSectionCard(
              heading = "Processing & Dispatch Timelines",
              body =
                "• Ready-to-Ship Suits & Sarees: Dispatched within 48 hours; delivered in 4–7 business days across India.\n• Custom-Stitched & Made-to-Order Couture: Handcrafted over 10–18 business days with quality inspection prior to dispatch.\n• Complimentary Insured Shipping: Free across India on orders ₹10,000 and above (₹499 flat fee below ₹10,000).",
            )
          }
        }
        "returns" -> {
          item {
            InfoSectionCard(
              heading = "Returns, Exchanges & Refunds",
              body =
                "• Ready-to-Wear Ensembles: Eligible for 7-day size exchange or store credit if unworn with original tags and packaging intact.\n• Custom-Stitched & Bridal Made-to-Order: Because bespoke garments are tailored exclusively to your measurements, they are non-returnable; complimentary minor alterations are provided.\n• Damaged or Incorrect Item: Full replacement or refund to original payment method within 5–7 business days upon unboxing verification.",
            )
          }
        }
        else -> {
          item {
            InfoSectionCard(
              heading = "Privacy & Data Protection",
              body =
                "RAZA ETHNIC WEAR collects only the information needed to authenticate your account, tailor your bespoke garments, fulfill shipping, and provide order tracking. Your profile, measurements, and shipping details are encrypted and isolated per customer account. We never sell personal data to third parties.",
            )
          }
        }
      }
    }
  }
}

@Composable
private fun InfoSectionCard(heading: String, body: String) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(heading, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}
