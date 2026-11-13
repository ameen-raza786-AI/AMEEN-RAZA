package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureProfileAndCart_authenticatedUser_createsAndObservesCart() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = StoreRepository(firestore, auth)

    val profileRes =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.ensureUserProfile(uid, "Alice Raza", ALICE_EMAIL, "")
      }
    assertTrue(profileRes.isSuccess)

    val sampleProduct =
      Product(
        id = "prod_test_lehenga",
        name = "Test Bridal Lehenga",
        category = "Bridal Lehenga",
        images = listOf("drawable:img_hero_bridal"),
        price = 40000.0,
        discountPrice = 34000.0,
        estimatedDelivery = "10 days",
      )

    val addCartRes =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.addToCart(
          product = sampleProduct,
          selectedSize = "M",
          selectedColor = "Royal Crimson",
          customStitching = true,
          madeToOrder = false,
          customMeasurements = "Bust 34, Waist 28",
          quantity = 1L,
        )
      }
    assertTrue(addCartRes.isSuccess)

    val cartItems =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeCart(uid).first { it.any { item -> item.productId == sampleProduct.id } }
      }
    assertEquals(1, cartItems.size)
    assertEquals("Royal Crimson", cartItems.first().selectedColor)
  }

  @Test
  fun placeOrder_authenticatedUser_createsOrderAndAllowsOwnerList() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = StoreRepository(firestore, auth)

    val cartItem =
      CartItem(
        id = "",
        userId = uid,
        productId = "prod_test_saree",
        productName = "Banarasi Silk Saree",
        productImage = "drawable:img_saree_obsidian",
        category = "Sarees",
        price = 22000.0,
        discountPrice = 18000.0,
        selectedSize = "Free Size",
        selectedColor = "Obsidian Gold",
        customStitching = false,
        madeToOrder = false,
        estimatedDelivery = "5 days",
        quantity = 1L,
      )

    val orderRes =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.placeOrder(
          customerName = "Alice",
          customerEmail = ALICE_EMAIL,
          customerPhone = "+919999999999",
          shippingAddress = "10 Heritage Lane, Delhi",
          items = listOf(cartItem),
          subtotal = 18000.0,
          discountAmount = 0.0,
          shippingFee = 0.0,
          totalAmount = 18000.0,
          promoCode = "",
          paymentMethod = "Cash on Delivery",
          paymentStatus = "Pending on Delivery",
          transactionId = "",
          estimatedDelivery = "5 days",
          clearCart = false,
        )
      }
    assertTrue(orderRes.isSuccess)
    val orderId = orderRes.getOrThrow()

    val orders =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeUserOrders(uid).first { list -> list.any { it.id == orderId } }
      }
    assertTrue(orders.any { it.id == orderId && it.status == "Placed" })
  }

  @Test
  fun observeCart_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = StoreRepository(firestore, auth)

    var caught: Exception? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeCart("some_uid").first() }
    } catch (e: Exception) {
      caught = e
    }
    assertNotNull(caught)
    val firestoreException = caught!!.unwrapFirestoreException()
    assertNotNull(firestoreException)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException!!.code)
  }

  @Test
  fun saveProductAsAdmin_nonAdminCustomer_failsWithPermissionDenied() = runBlocking {
    signInTestUser(BOB_EMAIL)
    val repo = StoreRepository(firestore, auth)

    val res =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.saveProductAsAdmin(
          Product(
            name = "Unauthorized Product",
            description = "Should fail security rules",
            category = "Sarees",
            images = listOf("drawable:img_saree_obsidian"),
            price = 10000.0,
            discountPrice = 9000.0,
            sizes = listOf("M"),
            colors = listOf("Black"),
            fabric = "Silk",
            estimatedDelivery = "5 days",
            stockCount = 5L,
          )
        )
      }
    assertTrue(res.isFailure)
    val ex = res.exceptionOrNull() as? FirebaseFirestoreException
    assertNotNull(ex)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex!!.code)
  }

  private companion object {
    const val ALICE_EMAIL = "alice_patron@test.com"
    const val BOB_EMAIL = "bob_patron@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
