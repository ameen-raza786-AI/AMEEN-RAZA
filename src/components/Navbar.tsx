import React, { useState } from "react";
import {
  Heart,
  Home,
  Info,
  Menu,
  Search,
  ShoppingBag,
  Sparkles,
  X,
} from "lucide-react";

export type MainSection = "home" | "catalogue" | "wishlist" | "info";

interface NavbarProps {
  activeSection: MainSection;
  activeCategory: string;
  cartCount: number;
  wishlistCount: number;
  onNavigate: (section: MainSection, categoryFilter?: string) => void;
  onOpenCart: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeSection,
  activeCategory,
  cartCount,
  wishlistCount,
  onNavigate,
  onOpenCart,
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleNavClick = (section: MainSection, categoryFilter?: string) => {
    setMobileMenuOpen(false);
    onNavigate(section, categoryFilter);
  };

  return (
    <>
      <header className="site-header">
        <div className="container header-inner">
          <button
            type="button"
            className="brand-lockup"
            onClick={() => handleNavClick("home", "All")}
            aria-label="RAZA ETHNIC WEAR Home"
          >
            <img
              src="./images/img_app_icon_fg.jpg"
              alt="RAZA ETHNIC WEAR Crest"
              className="brand-crest"
            />
            <div>
              <p className="brand-title">RAZA ETHNIC WEAR</p>
              <span className="brand-subtitle">Royal Indian Couture</span>
            </div>
          </button>

          <nav className="desktop-nav" aria-label="Primary Navigation">
            <button
              type="button"
              className={`nav-link ${activeSection === "home" ? "active" : ""}`}
              onClick={() => handleNavClick("home", "All")}
            >
              Home
            </button>
            <button
              type="button"
              className={`nav-link ${
                activeSection === "catalogue" && activeCategory === "All"
                  ? "active"
                  : ""
              }`}
              onClick={() => handleNavClick("catalogue", "All")}
            >
              Catalogue
            </button>
            <button
              type="button"
              className={`nav-link ${
                activeSection === "catalogue" &&
                activeCategory.includes("Lehenga")
                  ? "active"
                  : ""
              }`}
              onClick={() => handleNavClick("catalogue", "Bridal Lehenga")}
            >
              Lehengas
            </button>
            <button
              type="button"
              className={`nav-link ${
                activeSection === "catalogue" &&
                (activeCategory === "Gharara Suits" ||
                  activeCategory === "Pakistani Suits")
                  ? "active"
                  : ""
              }`}
              onClick={() => handleNavClick("catalogue", "Gharara Suits")}
            >
              Suits &amp; Ghararas
            </button>
            <button
              type="button"
              className={`nav-link ${
                activeSection === "catalogue" && activeCategory === "Sarees"
                  ? "active"
                  : ""
              }`}
              onClick={() => handleNavClick("catalogue", "Sarees")}
            >
              Gowns &amp; Sarees
            </button>
            <button
              type="button"
              className={`nav-link ${activeSection === "info" ? "active" : ""}`}
              onClick={() => handleNavClick("info")}
            >
              About &amp; Policies
            </button>
          </nav>

          <div className="header-actions">
            <button
              type="button"
              className="icon-btn"
              onClick={() => handleNavClick("catalogue", "All")}
              aria-label="Search Catalogue"
              title="Search Catalogue"
            >
              <Search size={19} />
            </button>
            <button
              type="button"
              className="icon-btn"
              onClick={() => handleNavClick("wishlist")}
              aria-label={`Wishlist (${wishlistCount} items)`}
              title="Wishlist"
            >
              <Heart size={19} />
              {wishlistCount > 0 && (
                <span className="badge-count">{wishlistCount}</span>
              )}
            </button>
            <button
              type="button"
              className="icon-btn"
              onClick={onOpenCart}
              aria-label={`Shopping Bag (${cartCount} items)`}
              title="Shopping Bag"
            >
              <ShoppingBag size={19} />
              {cartCount > 0 && <span className="badge-count">{cartCount}</span>}
            </button>
            <button
              type="button"
              className="icon-btn mobile-menu-btn"
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              aria-label={mobileMenuOpen ? "Close Menu" : "Open Menu"}
            >
              {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {mobileMenuOpen && (
          <div className="mobile-drawer" role="dialog" aria-label="Mobile Menu">
            <button
              type="button"
              onClick={() => handleNavClick("home", "All")}
            >
              Home Showcase
            </button>
            <button
              type="button"
              onClick={() => handleNavClick("catalogue", "All")}
            >
              Full Couture Catalogue
            </button>
            <button
              type="button"
              onClick={() => handleNavClick("catalogue", "Bridal Lehenga")}
            >
              Bridal &amp; Party Lehengas
            </button>
            <button
              type="button"
              onClick={() => handleNavClick("catalogue", "Gharara Suits")}
            >
              Exclusive Gharara &amp; Suits
            </button>
            <button
              type="button"
              onClick={() => handleNavClick("catalogue", "Designer Gowns")}
            >
              Designer Gowns &amp; Sarees
            </button>
            <button
              type="button"
              onClick={() => handleNavClick("wishlist")}
            >
              Saved Wishlist ({wishlistCount})
            </button>
            <button
              type="button"
              onClick={() => handleNavClick("info")}
            >
              About, Contact &amp; Policies
            </button>
          </div>
        )}
      </header>

      <nav className="mobile-bottom-nav" aria-label="Mobile Quick Navigation">
        <button
          type="button"
          className={`mobile-bottom-item ${activeSection === "home" ? "active" : ""}`}
          onClick={() => handleNavClick("home", "All")}
        >
          <Home size={19} />
          <span>Home</span>
        </button>
        <button
          type="button"
          className={`mobile-bottom-item ${
            activeSection === "catalogue" ? "active" : ""
          }`}
          onClick={() => handleNavClick("catalogue", "All")}
        >
          <Sparkles size={19} />
          <span>Shop</span>
        </button>
        <button
          type="button"
          className={`mobile-bottom-item ${
            activeSection === "wishlist" ? "active" : ""
          }`}
          onClick={() => handleNavClick("wishlist")}
        >
          <Heart size={19} />
          <span>Wishlist{wishlistCount > 0 ? ` (${wishlistCount})` : ""}</span>
        </button>
        <button
          type="button"
          className="mobile-bottom-item"
          onClick={onOpenCart}
        >
          <ShoppingBag size={19} />
          <span>Bag{cartCount > 0 ? ` (${cartCount})` : ""}</span>
        </button>
        <button
          type="button"
          className={`mobile-bottom-item ${activeSection === "info" ? "active" : ""}`}
          onClick={() => handleNavClick("info")}
        >
          <Info size={19} />
          <span>Atelier</span>
        </button>
      </nav>
    </>
  );
};
