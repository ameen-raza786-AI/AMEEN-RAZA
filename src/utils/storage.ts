export interface CartEntry {
  cartKey: string;
  productId: string;
  productName: string;
  productImage: string;
  category: string;
  unitPrice: number;
  mrpPrice: number;
  selectedSize: string;
  selectedColor: string;
  customStitching: boolean;
  madeToOrder: boolean;
  customMeasurements: string;
  estimatedDelivery: string;
  quantity: number;
}

const CART_KEY = "raza_ethnic_wear_cart_v1";
const WISHLIST_KEY = "raza_ethnic_wear_wishlist_v1";

export function loadCartFromStorage(): CartEntry[] {
  try {
    const raw = window.localStorage.getItem(CART_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveCartToStorage(items: CartEntry[]): void {
  try {
    window.localStorage.setItem(CART_KEY, JSON.stringify(items));
  } catch {
    // Ignore storage quota errors
  }
}

export function loadWishlistFromStorage(): string[] {
  try {
    const raw = window.localStorage.getItem(WISHLIST_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveWishlistToStorage(ids: string[]): void {
  try {
    window.localStorage.setItem(WISHLIST_KEY, JSON.stringify(ids));
  } catch {
    // Ignore storage quota errors
  }
}
