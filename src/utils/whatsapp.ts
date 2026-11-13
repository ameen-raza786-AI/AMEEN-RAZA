import { BOUTIQUE_WHATSAPP_NUMBER, ProductItem, formatInr, getEffectivePrice } from "../data/catalog";
import { CartEntry } from "./storage";

export function openWhatsAppProductEnquiry(
  product: ProductItem,
  options?: {
    size?: string;
    color?: string;
    customStitching?: boolean;
    madeToOrder?: boolean;
    measurements?: string;
  }
): void {
  const size = options?.size || product.sizes[0] || "M";
  const color = options?.color || product.colors[0] || "Signature";
  const customStitching = options?.customStitching ?? false;
  const madeToOrder = options?.madeToOrder ?? product.madeToOrder;
  const measurements = options?.measurements?.trim() || "";
  const effectivePrice = getEffectivePrice(product);

  const lines = [
    "Hello *RAZA ETHNIC WEAR* Couture Concierge,",
    "I would like to enquire / order:",
    `• *Product:* ${product.name}`,
    `• *Category:* ${product.category}`,
    `• *Price:* ${formatInr(effectivePrice)} (MRP ${formatInr(product.price)})`,
    `• *Size:* ${size}`,
    `• *Colour:* ${color}`,
    `• *Custom Stitching:* ${customStitching ? "Yes" : "Standard Fit"}`,
    `• *Made-to-Order:* ${madeToOrder ? "Yes" : "Ready Collection"}`,
  ];

  if (measurements) {
    lines.push(`• *Measurements / Notes:* ${measurements}`);
  }
  lines.push(`• *Image Reference:* ${window.location.origin}/${product.images[0].replace(/^\.\//, "")}`);
  lines.push("Please confirm availability and delivery timeline.");

  const url = `https://wa.me/${BOUTIQUE_WHATSAPP_NUMBER}?text=${encodeURIComponent(lines.join("\n"))}`;
  window.open(url, "_blank", "noopener,noreferrer");
}

export function openWhatsAppCartEnquiry(
  items: CartEntry[],
  subtotal: number,
  discount: number,
  promoCode: string,
  total: number,
  customerName: string,
  customerPhone: string,
  shippingAddress: string
): void {
  const lines = [
    "Hello *RAZA ETHNIC WEAR* Atelier,",
    "I would like to place a boutique enquiry / Cash-on-Delivery request:",
    "",
    ...items.map(
      (item, idx) =>
        `${idx + 1}. *${item.productName}* (${item.category})\n   Size: ${item.selectedSize} | Colour: ${item.selectedColor} | Qty: ${item.quantity} | ${formatInr(item.unitPrice * item.quantity)}${item.customStitching ? "\n   Custom Stitching: Yes" : ""}${item.customMeasurements ? `\n   Measurements: ${item.customMeasurements}` : ""}`
    ),
    "",
    `• *Subtotal:* ${formatInr(subtotal)}`,
  ];
  if (discount > 0) {
    lines.push(`• *Promo (${promoCode}):* -${formatInr(discount)}`);
  }
  lines.push(`• *Estimated Total:* ${formatInr(total)}`);
  if (customerName || customerPhone || shippingAddress) {
    lines.push("");
    if (customerName) lines.push(`• *Name:* ${customerName}`);
    if (customerPhone) lines.push(`• *Phone:* ${customerPhone}`);
    if (shippingAddress) lines.push(`• *City / Address:* ${shippingAddress}`);
  }
  lines.push("\nPlease confirm availability & delivery schedule.");

  const url = `https://wa.me/${BOUTIQUE_WHATSAPP_NUMBER}?text=${encodeURIComponent(lines.join("\n"))}`;
  window.open(url, "_blank", "noopener,noreferrer");
}

export function openWhatsAppConcierge(): void {
  const text = "Hello RAZA ETHNIC WEAR! I would like bridal couture & styling assistance.";
  window.open(
    `https://wa.me/${BOUTIQUE_WHATSAPP_NUMBER}?text=${encodeURIComponent(text)}`,
    "_blank",
    "noopener,noreferrer"
  );
}
