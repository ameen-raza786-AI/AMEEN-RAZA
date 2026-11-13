package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val phone: String = "",
  val address: String = "",
  val city: String = "",
  val postalCode: String = "",
  val photoUrl: String = "",
  val totalOrders: Long = 0L,
  val totalSpent: Double = 0.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Category(
  @DocumentId val id: String = "",
  val name: String = "",
  val subtitle: String = "",
  val imageKey: String = "",
  val sortOrder: Long = 0L,
  val isActive: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Product(
  @DocumentId val id: String = "",
  val name: String = "",
  val description: String = "",
  val category: String = "",
  val collections: List<String> = emptyList(),
  val images: List<String> = emptyList(),
  val price: Double = 0.0,
  val discountPrice: Double = 0.0,
  val isSpecialOffer: Boolean = false,
  val offerBadge: String = "",
  val sizes: List<String> = emptyList(),
  val colors: List<String> = emptyList(),
  val fabric: String = "",
  val customStitchingAvailable: Boolean = true,
  val madeToOrder: Boolean = false,
  val estimatedDelivery: String = "",
  val stockCount: Long = 0L,
  val rating: Double = 4.8,
  val reviewCount: Long = 0L,
  val isPublished: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val effectivePrice: Double
    get() = if (discountPrice in 1.0..price) discountPrice else price

  val discountPercent: Int
    get() = if (price > 0 && discountPrice > 0 && discountPrice < price) {
      (((price - discountPrice) / price) * 100.0).toInt()
    } else {
      0
    }
}

@IgnoreExtraProperties
data class Offer(
  @DocumentId val id: String = "",
  val title: String = "",
  val subtitle: String = "",
  val promoCode: String = "",
  val discountPercent: Long = 10L,
  val minOrderAmount: Double = 0.0,
  val isActive: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CartItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val productId: String = "",
  val productName: String = "",
  val productImage: String = "",
  val category: String = "",
  val price: Double = 0.0,
  val discountPrice: Double = 0.0,
  val selectedSize: String = "M",
  val selectedColor: String = "",
  val customStitching: Boolean = false,
  val madeToOrder: Boolean = false,
  val customMeasurements: String = "",
  val estimatedDelivery: String = "",
  val quantity: Long = 1L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val unitPrice: Double
    get() = if (discountPrice in 1.0..price) discountPrice else price
}

@IgnoreExtraProperties
data class WishlistItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val productId: String = "",
  val productName: String = "",
  val productImage: String = "",
  val category: String = "",
  val price: Double = 0.0,
  val discountPrice: Double = 0.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  val effectivePrice: Double
    get() = if (discountPrice in 1.0..price) discountPrice else price
}

@IgnoreExtraProperties
data class OrderLineItem(
  val productId: String = "",
  val productName: String = "",
  val productImage: String = "",
  val selectedSize: String = "",
  val selectedColor: String = "",
  val customStitching: Boolean = false,
  val madeToOrder: Boolean = false,
  val customMeasurements: String = "",
  val quantity: Long = 1L,
  val unitPrice: Double = 0.0,
)

@IgnoreExtraProperties
data class Order(
  @DocumentId val id: String = "",
  val userId: String = "",
  val orderNumber: String = "",
  val customerName: String = "",
  val customerEmail: String = "",
  val customerPhone: String = "",
  val shippingAddress: String = "",
  val items: List<OrderLineItem> = emptyList(),
  val subtotal: Double = 0.0,
  val discountAmount: Double = 0.0,
  val shippingFee: Double = 0.0,
  val totalAmount: Double = 0.0,
  val promoCode: String = "",
  val paymentMethod: String = "UPI / Online",
  val paymentStatus: String = "Paid",
  val transactionId: String = "",
  val status: String = "Placed",
  val trackingNote: String = "",
  val estimatedDelivery: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Review(
  @DocumentId val id: String = "",
  val productId: String = "",
  val userId: String = "",
  val reviewerName: String = "",
  val rating: Long = 5L,
  val comment: String = "",
  val isApproved: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Inquiry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "",
  val email: String = "",
  val phone: String = "",
  val subject: String = "",
  val message: String = "",
  val status: String = "Open",
  @ServerTimestamp val createdAt: Timestamp? = null,
)
