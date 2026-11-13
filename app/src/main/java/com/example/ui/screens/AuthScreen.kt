package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.GoogleSignInButton
import com.example.data.firebase.attemptAutoSignIn
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SterlingSilver

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope,
    )
  }

  Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
    Image(
      painter = painterResource(id = R.drawable.img_hero_bridal),
      contentDescription = "Raza Ethnic Wear Royal Bridal Couture",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Luxury multi-stop obsidian scrim
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colorStops =
                arrayOf(
                  0.0f to ObsidianBlack.copy(alpha = 0.55f),
                  0.45f to ObsidianBlack.copy(alpha = 0.45f),
                  0.78f to ObsidianBlack.copy(alpha = 0.92f),
                  1.0f to ObsidianBlack,
                )
            )
          )
    )

    Column(
      modifier =
        Modifier.fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Couture Crest
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
          modifier =
            Modifier.size(72.dp)
              .clip(CircleShape)
              .background(ObsidianBlack.copy(alpha = 0.78f))
              .border(1.5.dp, RoyalGold, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "R",
            style = MaterialTheme.typography.displayLarge,
            color = ChampagneGold,
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "RAZA ETHNIC WEAR",
          style = MaterialTheme.typography.headlineMedium,
          letterSpacing = 3.sp,
          color = ChampagneGold,
          textAlign = TextAlign.Center,
        )
        Text(
          text = "ROYAL INDIAN COUTURE ATELIER",
          style = MaterialTheme.typography.labelSmall,
          letterSpacing = 2.5.sp,
          color = SterlingSilver,
          textAlign = TextAlign.Center,
        )
      }

      // Bottom Sign-In Card
      Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianBlack.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "Timeless Heritage, Tailored for You",
            style = MaterialTheme.typography.headlineMedium,
            color = PlatinumWhite,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text =
              "Discover Bridal Lehengas, Lucknawi Ghararas, Pakistani Couture & Pure Banarasi Sarees with bespoke stitching & WhatsApp concierge.",
            style = MaterialTheme.typography.bodyMedium,
            color = SterlingSilver.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            HighlightPill(Icons.Filled.AutoAwesome, "Zardozi Craft")
            HighlightPill(Icons.Filled.Checkroom, "Custom Fit")
            HighlightPill(Icons.Filled.LocalShipping, "Insured Delivery")
          }

          if (!errorMsg.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              color = CrimsonVelvet.copy(alpha = 0.18f),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, CrimsonVelvet.copy(alpha = 0.5f)),
            ) {
              Text(
                text = errorMsg ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFB4BE),
                modifier = Modifier.padding(10.dp),
                textAlign = TextAlign.Center,
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))
          GoogleSignInButton(
            onAuthSuccess = {
              errorMsg = null
              onAuthSuccess()
            },
            onAuthError = { msg -> errorMsg = msg },
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Secure sign-in syncs your wishlist, bag & live order tracking",
            style = MaterialTheme.typography.labelSmall,
            color = SterlingSilver.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun HighlightPill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = RoyalGold,
      modifier = Modifier.size(14.dp),
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = PlatinumWhite,
      fontWeight = FontWeight.Medium,
    )
  }
}
