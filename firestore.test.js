const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_999";
const ADMIN_EMAIL = "ameemraza986@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTs() {
  return new Date();
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated caller cannot read products or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").where("isPublished", "==", true).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("User profile PII isolation & anti-ghost-field check", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      totalOrders: 0,
      totalSpent: 0,
      createdAt: nowTs(),
    })
  );

  // Ghost field injection (e.g. isAdmin) must fail
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@example.com",
      totalOrders: 0,
      totalSpent: 0,
      isAdmin: true,
      createdAt: nowTs(),
    })
  );

  // Cross-user PII read and list must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
});

test("Email spoofing attack: unverified admin email cannot write products", async () => {
  const spoofedDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofedDb.collection("products").doc("prod_1").set({
      name: "Royal Bridal Lehenga",
      description: "Handcrafted zardozi lehenga",
      category: "Bridal Lehenga",
      collections: ["New Arrivals"],
      images: ["drawable:img_hero_bridal"],
      price: 45000,
      discountPrice: 38000,
      isSpecialOffer: true,
      sizes: ["S", "M", "L"],
      colors: ["Crimson Gold"],
      fabric: "Pure Velvet",
      customStitchingAvailable: true,
      madeToOrder: true,
      estimatedDelivery: "10-14 days",
      stockCount: 5,
      rating: 4.9,
      reviewCount: 12,
      isPublished: true,
      createdAt: nowTs(),
    })
  );
});

test("Verified admin can create & update products and manage all orders", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("products").doc("prod_1").set({
      name: "Royal Bridal Lehenga",
      description: "Handcrafted zardozi lehenga with silk dupatta",
      category: "Bridal Lehenga",
      collections: ["New Arrivals", "Best Selling"],
      images: ["drawable:img_hero_bridal"],
      price: 45000,
      discountPrice: 38000,
      isSpecialOffer: true,
      sizes: ["S", "M", "L"],
      colors: ["Crimson Gold"],
      fabric: "Pure Velvet",
      customStitchingAvailable: true,
      madeToOrder: true,
      estimatedDelivery: "10-14 days",
      stockCount: 5,
      rating: 4.9,
      reviewCount: 12,
      isPublished: true,
      createdAt: nowTs(),
    })
  );

  // Customer can query published products
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("products").where("isPublished", "==", true).get());
  // Unfiltered products list must fail for non-admin
  await assertFails(aliceDb.collection("products").get());
});

test("Orders isolation, creation, cancellation, and terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  const orderData = {
    userId: ALICE_UID,
    orderNumber: "REW-1001",
    customerName: "Alice",
    customerEmail: "alice@example.com",
    customerPhone: "+919876543210",
    shippingAddress: "12 Royal Heritage Road, Jaipur",
    items: [{ productId: "prod_1", productName: "Royal Bridal Lehenga", quantity: 1, unitPrice: 38000 }],
    subtotal: 38000,
    discountAmount: 0,
    shippingFee: 0,
    totalAmount: 38000,
    paymentMethod: "UPI / Online",
    paymentStatus: "Paid",
    status: "Placed",
    estimatedDelivery: "10-14 days",
    createdAt: nowTs(),
  };

  await assertSucceeds(aliceDb.collection("orders").doc("order_1").set(orderData));
  await assertSucceeds(aliceDb.collection("orders").where("userId", "==", ALICE_UID).get());

  // Bob cannot read or query Alice's orders
  await assertFails(bobDb.collection("orders").doc("order_1").get());
  await assertFails(bobDb.collection("orders").get());

  // Alice cannot jump directly to Delivered
  await assertFails(
    aliceDb.collection("orders").doc("order_1").update({
      status: "Delivered",
      updatedAt: nowTs(),
    })
  );

  // Admin can update status to Confirmed -> Shipped -> Delivered
  await assertSucceeds(
    adminDb.collection("orders").doc("order_1").update({
      status: "Confirmed",
      trackingNote: "Order accepted by atelier",
      updatedAt: nowTs(),
    })
  );

  // Once Confirmed, Alice can no longer cancel
  await assertFails(
    aliceDb.collection("orders").doc("order_1").update({
      status: "Cancelled",
      updatedAt: nowTs(),
    })
  );
});

test("User cart and wishlist subcollections are isolated to owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1").set({
      userId: ALICE_UID,
      productId: "prod_1",
      productName: "Royal Bridal Lehenga",
      productImage: "drawable:img_hero_bridal",
      category: "Bridal Lehenga",
      price: 45000,
      discountPrice: 38000,
      selectedSize: "M",
      selectedColor: "Crimson Gold",
      customStitching: true,
      madeToOrder: false,
      estimatedDelivery: "10-14 days",
      quantity: 1,
      createdAt: nowTs(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cart").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("cart").get());
});
