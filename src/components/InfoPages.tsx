import React, { useEffect, useState } from "react";
import {
  Instagram,
  Mail,
  MapPin,
  MessageCircle,
  Phone,
  RotateCcw,
  ShieldCheck,
  Sparkles,
  Truck,
} from "lucide-react";
import {
  BOUTIQUE_INSTAGRAM_URL,
  BOUTIQUE_WHATSAPP_NUMBER,
} from "../data/catalog";
import { openWhatsAppConcierge } from "../utils/whatsapp";

export type InfoSubTab =
  | "about"
  | "contact"
  | "shipping"
  | "returns"
  | "privacy";

interface InfoPagesProps {
  initialTab?: InfoSubTab;
}

export const InfoPages: React.FC<InfoPagesProps> = ({
  initialTab = "about",
}) => {
  const [tab, setTab] = useState<InfoSubTab>(initialTab);

  useEffect(() => {
    setTab(initialTab);
  }, [initialTab]);
  const [name, setName] = useState("");
  const [message, setMessage] = useState("");

  const sendDirectEnquiry = (e: React.FormEvent) => {
    e.preventDefault();
    const composed = `Hello RAZA ETHNIC WEAR!${
      name.trim() ? ` My name is ${name.trim()}.` : ""
    } ${message.trim() || "I would like styling and bridal enquiry assistance."}`;
    window.open(
      `https://wa.me/${BOUTIQUE_WHATSAPP_NUMBER}?text=${encodeURIComponent(
        composed
      )}`,
      "_blank",
      "noopener,noreferrer"
    );
  };

  return (
    <section className="section-block container">
      <div className="section-header">
        <div>
          <span className="gold-pill">RAZA ETHNIC WEAR ATELIER</span>
          <h2 className="section-title" style={{ marginTop: "10px" }}>
            Heritage, Concierge &amp; Store Policies
          </h2>
        </div>
      </div>

      <div className="chip-row" style={{ marginBottom: "18px" }}>
        {(
          [
            ["about", "About Us"],
            ["contact", "Contact Us"],
            ["shipping", "Shipping Policy"],
            ["returns", "Return & Refund"],
            ["privacy", "Privacy Policy"],
          ] as [InfoSubTab, string][]
        ).map(([key, label]) => (
          <button
            key={key}
            type="button"
            className={`filter-chip ${tab === key ? "active" : ""}`}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </div>

      <div
        style={{
          background: "var(--bg-charcoal)",
          border: "1px solid var(--border-gold)",
          borderRadius: "20px",
          padding: "24px",
        }}
      >
        {tab === "about" && (
          <div style={{ display: "grid", gap: "14px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <Sparkles size={22} color="#d4af37" />
              <h3 className="font-serif" style={{ margin: 0, fontSize: "1.65rem" }}>
                About RAZA ETHNIC WEAR
              </h3>
            </div>
            <p style={{ margin: 0, color: "var(--silver-sterling)", lineHeight: 1.7 }}>
              Founded on royal Mughal &amp; Lucknawi craftsmanship, RAZA ETHNIC
              WEAR curates heirloom Bridal Lehengas, hand-embroidered Ghararas,
              Pakistani silk ensembles, floor-sweeping Anarkalis, sculpted
              Designer Gowns, and pure Banarasi Katan Silk Sarees.
            </p>
            <p style={{ margin: 0, color: "var(--silver-muted)", lineHeight: 1.7 }}>
              Every silhouette blends obsidian midnight elegance, sterling silver
              kasab, and antique royal gold zardozi — crafted with bespoke sizing,
              custom blouse tailoring, and personal WhatsApp concierge guidance
              with no sign-in required.
            </p>
          </div>
        )}

        {tab === "contact" && (
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
              gap: "20px",
            }}
          >
            <div style={{ display: "grid", gap: "12px" }}>
              <h3 className="font-serif" style={{ margin: 0, fontSize: "1.65rem" }}>
                Contact Our Bridal Atelier
              </h3>
              <p style={{ margin: 0, color: "var(--silver-muted)", lineHeight: 1.6 }}>
                Connect directly with our stylists for video consultations,
                custom measurement assistance, Cash-on-Delivery confirmation,
                and bridal trousseau appointments.
              </p>
              <div style={{ display: "grid", gap: "10px", fontSize: "0.9rem" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                  <Phone size={17} color="#d4af37" />
                  <span>WhatsApp Concierge: +91 98765 43210</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                  <Mail size={17} color="#d4af37" />
                  <span>Email: atelier@razaethnicwear.com</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                  <MapPin size={17} color="#d4af37" />
                  <span>Flagship Atelier: Hazratganj &amp; South Delhi</span>
                </div>
              </div>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", marginTop: "6px" }}>
                <button
                  type="button"
                  className="btn-whatsapp"
                  onClick={openWhatsAppConcierge}
                >
                  <MessageCircle size={17} />
                  Chat on WhatsApp
                </button>
                <a
                  href={BOUTIQUE_INSTAGRAM_URL}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn-outline-gold"
                >
                  <Instagram size={17} />
                  @razaethnicwear
                </a>
              </div>
            </div>

            <form
              onSubmit={sendDirectEnquiry}
              style={{
                background: "var(--bg-onyx)",
                padding: "16px",
                borderRadius: "14px",
                border: "1px solid var(--border-subtle)",
                display: "grid",
                gap: "10px",
              }}
            >
              <div style={{ fontWeight: 700, color: "var(--gold-champagne)" }}>
                Send a Styling Note via WhatsApp
              </div>
              <input
                type="text"
                className="lux-input"
                placeholder="Your Full Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
              <textarea
                className="lux-input"
                rows={4}
                placeholder="Share the outfit name, occasion date, or measurement query..."
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                style={{ minHeight: "96px", resize: "vertical" }}
              />
              <button type="submit" className="btn-gold">
                <MessageCircle size={17} />
                Open WhatsApp Enquiry
              </button>
            </form>
          </div>
        )}

        {tab === "shipping" && (
          <div style={{ display: "grid", gap: "12px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <Truck size={22} color="#d4af37" />
              <h3 className="font-serif" style={{ margin: 0, fontSize: "1.65rem" }}>
                Shipping Policy
              </h3>
            </div>
            <p style={{ margin: 0, color: "var(--silver-sterling)", lineHeight: 1.7 }}>
              • Ready-to-ship designer suits and Banarasi sarees dispatch within
              4–7 business days via insured courier across India.
            </p>
            <p style={{ margin: 0, color: "var(--silver-sterling)", lineHeight: 1.7 }}>
              • Hand-embroidered Bridal Lehengas and Made-to-Order Ghararas
              require 10–18 business days for artisan zardozi finishing and
              custom tailoring.
            </p>
            <p style={{ margin: 0, color: "var(--silver-muted)", lineHeight: 1.7 }}>
              • Complimentary insured shipping on orders above ₹10,000. Cash on
              Delivery (COD) enquiries are confirmed directly over WhatsApp.
            </p>
          </div>
        )}

        {tab === "returns" && (
          <div style={{ display: "grid", gap: "12px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <RotateCcw size={22} color="#d4af37" />
              <h3 className="font-serif" style={{ margin: 0, fontSize: "1.65rem" }}>
                Return &amp; Refund Policy
              </h3>
            </div>
            <p style={{ margin: 0, color: "var(--silver-sterling)", lineHeight: 1.7 }}>
              • Unworn, unaltered ready-size outfits with original atelier tags
              may be exchanged or returned within 7 days of delivery.
            </p>
            <p style={{ margin: 0, color: "var(--silver-sterling)", lineHeight: 1.7 }}>
              • Custom-stitched and made-to-order bridal garments include
              complimentary alteration support if any fit adjustment is needed.
            </p>
            <p style={{ margin: 0, color: "var(--silver-muted)", lineHeight: 1.7 }}>
              • Initiate any size exchange or alteration request directly via our
              WhatsApp Couture Concierge with your order summary.
            </p>
          </div>
        )}

        {tab === "privacy" && (
          <div style={{ display: "grid", gap: "12px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <ShieldCheck size={22} color="#d4af37" />
              <h3 className="font-serif" style={{ margin: 0, fontSize: "1.65rem" }}>
                Privacy Policy
              </h3>
            </div>
            <p style={{ margin: 0, color: "var(--silver-sterling)", lineHeight: 1.7 }}>
              • RAZA ETHNIC WEAR Static Boutique requires no account creation,
              no Google sign-in, and stores your shopping bag and wishlist
              strictly in your browser&apos;s local storage.
            </p>
            <p style={{ margin: 0, color: "var(--silver-muted)", lineHeight: 1.7 }}>
              • Enquiry details you choose to share are sent only when you tap
              to open WhatsApp with your selected garments and notes.
            </p>
          </div>
        )}
      </div>
    </section>
  );
};
