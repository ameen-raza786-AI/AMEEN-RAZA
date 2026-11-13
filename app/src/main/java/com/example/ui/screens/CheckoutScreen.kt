package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.util.BoutiqueActions
import com.example.ui.viewmodel.StoreUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
  uiState: StoreUiState,
  itemsToCheckout: List<CartItem>,
  promoCode: String,
  discountAmount: Double,
  isDirectBuy: Boolean,
  onBack: () -> Unit,
  onConfirmOrder: (
    customerName: String,
    phone: String,
    address: String,
    paymentMethod: String,
    paymentStatus: String,
    transactionId: String,
  ) -> Unit,
) {
  BackHandler { onBack() }
  val context = LocalContext.current
  val profile = uiState.userProfile

  var fullName by remember(profile?.displayName) { mutableStateOf(profile?.displayName.orEmpty()) }
  var phone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
  var address by remember(profile?.address) { mutableStateOf(profile?.address.orEmpty()) }
  var city by remember(profile?.city) { mutableStateOf(profile?.city.orEmpty()) }
  var postalCode by remember(profile?.postalCode) { mutableStateOf(profile?.postalCode.orEmpty()) }

  var paymentMethod by remember { mutableStateOf("UPI / Online") }
  var upiId by remember { mutableStateOf("") }
  var cardNumber by remember { mutableStateOf("") }
  var cardExpiry by remember { mutableStateOf("") }
  var cardCvv by remember { mutableStateOf("") }
  var validationError by remember { mutableStateOf<String?>(null) }

  val subtotal = remember(itemsToCheckout) { itemsToCheckout.sumOf { it.unitPrice * it.quantity } }
  val shippingFee = if (subtotal >= 10000.0 || subtotal == 0.0) 0.0 else 499.0
  val total = (subtotal - discountAmount + shippingFee).coerceAtLeast(0.0)

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = { Text("Couture Checkout", style = MaterialTheme.typography.headlineMedium) },
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
      modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Shipping Address Section
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Shipping & Delivery Details", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Recipient Full Name") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("checkout_name_input"),
            )
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Mobile / WhatsApp Number") },
              placeholder = { Text("+91 98765 43210") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input"),
            )
            OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("House / Street / Landmark") },
              minLines = 2,
              modifier = Modifier.fillMaxWidth().testTag("checkout_address_input"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("PIN Code") },
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      // Payment Method Section
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Select Payment Method", style = MaterialTheme.typography.titleLarge)

            PaymentOptionRow(
              selected = paymentMethod == "UPI / Online",
              icon = Icons.Filled.AccountBalanceWallet,
              title = "UPI / Online Payment",
              subtitle = "Google Pay, PhonePe, Paytm & Instant UPI",
              onClick = { paymentMethod = "UPI / Online" },
            )

            AnimatedVisibility(visible = paymentMethod == "UPI / Online") {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = upiId,
                  onValueChange = { upiId = it },
                  label = { Text("Your UPI ID (e.g. name@okicici)") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                  onClick = {
                    BoutiqueActions.tryLaunchUpiApp(context, total, "Raza Ethnic Wear Order")
                  },
                  border = BorderStroke(1.dp, RoyalGold),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text("Open UPI App (${BoutiqueActions.formatInr(total)})", color = RoyalGold)
                }
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            PaymentOptionRow(
              selected = paymentMethod == "Card / NetBanking",
              icon = Icons.Filled.CreditCard,
              title = "Card / NetBanking",
              subtitle = "Visa, Mastercard, RuPay & Indian Banks",
              onClick = { paymentMethod = "Card / NetBanking" },
            )

            AnimatedVisibility(visible = paymentMethod == "Card / NetBanking") {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = cardNumber,
                  onValueChange = { if (it.length <= 19) cardNumber = it },
                  label = { Text("Card Number") },
                  placeholder = { Text("4111 2222 3333 4444") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { if (it.length <= 5) cardExpiry = it },
                    label = { Text("MM/YY") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                  )
                  OutlinedTextField(
                    value = cardCvv,
                    onValueChange = { if (it.length <= 4) cardCvv = it },
                    label = { Text("CVV") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            PaymentOptionRow(
              selected = paymentMethod == "Cash on Delivery",
              icon = Icons.Filled.LocalShipping,
              title = "Cash on Delivery (COD)",
              subtitle = "Pay via cash or UPI at your doorstep",
              onClick = { paymentMethod = "Cash on Delivery" },
            )
          }
        }
      }

      // Order Summary & Confirm
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order Summary (${itemsToCheckout.sumOf { it.quantity }} items)", style = MaterialTheme.typography.titleLarge)
            itemsToCheckout.forEach { item ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "${item.quantity}x ${item.productName} (${item.selectedSize})",
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.weight(1f),
                )
                Text(
                  text = BoutiqueActions.formatInr(item.unitPrice * item.quantity),
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(
                text = BoutiqueActions.formatInr(total),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }

            if (!validationError.isNullOrBlank()) {
              Text(
                text = validationError ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonVelvet,
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Button(
              enabled = !uiState.isBusy && itemsToCheckout.isNotEmpty(),
              onClick = {
                val combinedAddress = listOf(address.trim(), city.trim(), postalCode.trim()).filter { it.isNotBlank() }.joinToString(", ")
                when {
                  fullName.trim().length < 2 -> validationError = "Please enter recipient full name"
                  phone.trim().length < 7 -> validationError = "Please enter a valid phone number"
                  combinedAddress.length < 6 -> validationError = "Please enter complete delivery address"
                  paymentMethod == "UPI / Online" && upiId.trim().isNotBlank() && !upiId.contains("@") ->
                    validationError = "Please enter a valid UPI ID (e.g. name@bank)"
                  paymentMethod == "Card / NetBanking" &&
                    (cardNumber.filter { it.isDigit() }.length < 12 || cardExpiry.length < 4 || cardCvv.length < 3) ->
                    validationError = "Please enter valid card number, expiry, and CVV"
                  else -> {
                    validationError = null
                    val payStatus = if (paymentMethod == "Cash on Delivery") "Pending on Delivery" else "Paid"
                    val txnId =
                      when (paymentMethod) {
                        "UPI / Online" -> "UPI-${System.currentTimeMillis().toString().takeLast(8)}"
                        "Card / NetBanking" -> "CARD-${System.currentTimeMillis().toString().takeLast(8)}"
                        else -> ""
                      }
                    onConfirmOrder(fullName, phone, combinedAddress, paymentMethod, payStatus, txnId)
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().height(54.dp).testTag("place_order_button"),
            ) {
              if (uiState.isBusy) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = ObsidianBlack, strokeWidth = 2.dp)
              } else {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (paymentMethod == "Cash on Delivery") "Place COD Order" else "Pay & Place Order",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
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
private fun PaymentOptionRow(
  selected: Boolean,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    RadioButton(
      selected = selected,
      onClick = onClick,
      colors = RadioButtonDefaults.colors(selectedColor = RoyalGold),
    )
    Icon(icon, contentDescription = null, tint = RoyalGold)
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
